package com.itzk.SmartEmploymentPlatform.service.Impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.RandomUtil;
import com.alibaba.fastjson2.JSON;
import com.itzk.SmartEmploymentPlatform.mapper.ApplicationMapper;
import com.itzk.SmartEmploymentPlatform.mapper.CompanyMapper;
import com.itzk.SmartEmploymentPlatform.mapper.FavorsMapper;
import com.itzk.SmartEmploymentPlatform.mapper.InterviewMapper;
import com.itzk.SmartEmploymentPlatform.mapper.JobsMapper;
import com.itzk.SmartEmploymentPlatform.mapper.UserMapper;
import com.itzk.SmartEmploymentPlatform.pojo.Result;
import com.itzk.SmartEmploymentPlatform.pojo.entry.User;
import com.itzk.SmartEmploymentPlatform.pojo.entryDTO.ChangePwdDTO;
import com.itzk.SmartEmploymentPlatform.pojo.entryDTO.ForgotPwdCodeDTO;
import com.itzk.SmartEmploymentPlatform.pojo.entryDTO.LoginFromDTO;
import com.itzk.SmartEmploymentPlatform.pojo.entryDTO.LoginPhoneDTO;
import com.itzk.SmartEmploymentPlatform.pojo.entryDTO.RegisterFromDTO;
import com.itzk.SmartEmploymentPlatform.pojo.entryDTO.ResetPwdDTO;
import com.itzk.SmartEmploymentPlatform.pojo.vo.LoginInfo;
import com.itzk.SmartEmploymentPlatform.pojo.vo.UserInfo;
import com.itzk.SmartEmploymentPlatform.pojo.vo.UserInfoVo;
import com.itzk.SmartEmploymentPlatform.pojo.entry.Conversation;
import com.itzk.SmartEmploymentPlatform.pojo.entryDTO.SendMessageDTO;
import com.itzk.SmartEmploymentPlatform.service.ChatService;
import com.itzk.SmartEmploymentPlatform.annotation.OperationLog;
import com.itzk.SmartEmploymentPlatform.service.UserService;
import com.itzk.SmartEmploymentPlatform.utils.DesensitizeUtil;
import com.itzk.SmartEmploymentPlatform.utils.JwtUtil;
import com.itzk.SmartEmploymentPlatform.utils.RegexUtil;
import com.itzk.SmartEmploymentPlatform.utils.UserHolder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Service
@Slf4j
public class UserServiceImpl implements UserService {

    @Autowired
    private StringRedisTemplate redisTemplate;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private FavorsMapper favorsMapper;

    @Autowired
    private ApplicationMapper applicationMapper;

    @Autowired
    private InterviewMapper interviewMapper;

    @Autowired
    private CompanyMapper companyMapper;

    @Autowired
    private JobsMapper jobsMapper;

    @Autowired
    private ChatService chatService;

    @Autowired
    private com.itzk.SmartEmploymentPlatform.mapper.OperationLogMapper operationLogMapper;

    @Autowired(required = false)
    private SimpMessagingTemplate messagingTemplate;

    // Redis Key 前缀（统一命名规范）
    private static final String SMS_CODE_PREFIX = "sms:code:";
    private static final String SMS_LIMIT_PREFIX = "sms:limit:";

    // 验证码过期时间：5分钟
    private static final long CODE_EXPIRE = 5;
    // 发送频率限制：1分钟内只能发一次
    private static final long LIMIT_EXPIRE = 1;

    // 首页统计数据缓存
    private static final String HOME_STATS_KEY = "home:stats";
    private static final long HOME_STATS_TTL = 30;

    /**
     * 发送短信验证码
     *
     * 开发/演示环境下没有真实短信通道，验证码通过以下方式送达：
     *   1. 后台日志输出
     *   2. 通过聊天系统推送给管理员（管理员可在消息面板看到）
     *   3. WebSocket 实时推送（管理员在线时即时收到）
     */
    @Override
    public Result getCode(String phone) {
        if (!RegexUtil.isPhone(phone)) {
            return Result.error("手机号格式错误");
        }

        String limitKey = SMS_LIMIT_PREFIX + phone;
        try {
            if (Boolean.TRUE.equals(redisTemplate.hasKey(limitKey))) {
                return Result.error("验证码发送太频繁，请1分钟后再试");
            }
        } catch (Exception e) {
            log.warn("Redis 查询短信频率限制失败，放行: {}", e.getMessage());
        }

        String code = RandomUtil.randomNumbers(6);

        // 存入 Redis（5分钟有效）
        try {
            String codeKey = SMS_CODE_PREFIX + phone;
            redisTemplate.opsForValue().set(codeKey, code, CODE_EXPIRE, TimeUnit.MINUTES);
            redisTemplate.opsForValue().set(limitKey, "1", LIMIT_EXPIRE, TimeUnit.MINUTES);
        } catch (Exception e) {
            log.warn("Redis 存储验证码失败，仅控制台可用: {}", e.getMessage());
        }

        // 控制台日志（始终可用）
        log.info("验证码 —— 手机号: {}, 验证码: {}", phone, code);

        // 推送给所有管理员
        pushCodeToAdmins(phone, code);

        return Result.success();
    }

    /**
     * 将验证码推送给所有管理员
     * 1. 聊天消息（持久化，管理员刷新对话列表可见）
     * 2. WebSocket 实时推送（管理员在线时即时弹窗）
     */
    private static final String VERIFICATION_LOG_KEY = "verification:recent";

    private void pushCodeToAdmins(String phone, String code) {
        String maskedPhone = phone.replaceAll("(\\d{3})\\d{4}(\\d{4})", "$1****$2");
        String msgContent = "验证码通知：" + maskedPhone + " 验证码 " + code + "（5分钟有效）";

        // 0. 存入 Redis 最近记录（管理员可在后台查看）
        Map<String, Object> logEntry = new LinkedHashMap<>();
        logEntry.put("phone", maskedPhone);
        logEntry.put("code", code);
        logEntry.put("time", System.currentTimeMillis());
        try {
            redisTemplate.opsForList().leftPush(VERIFICATION_LOG_KEY, JSON.toJSONString(logEntry));
            redisTemplate.opsForList().trim(VERIFICATION_LOG_KEY, 0, 49);
            redisTemplate.expire(VERIFICATION_LOG_KEY, 1, TimeUnit.HOURS);
        } catch (Exception e) {
            log.warn("Redis 存储验证码日志失败: {}", e.getMessage());
        }

        try {
            List<Long> adminIds = userMapper.selectAdminIds();
            User phoneUser = userMapper.getByPhone(phone);

            for (Long adminId : adminIds) {
                // 1. 聊天消息推送（仅当用户存在时，管理员与用户之间的对话）
                if (phoneUser != null && !phoneUser.getId().equals(adminId)) {
                    try {
                        Conversation conv = chatService.getOrCreateConversation(adminId, phoneUser.getId());
                        SendMessageDTO dto = new SendMessageDTO();
                        dto.setReceiverId(adminId);
                        dto.setContent(msgContent);
                        dto.setMsgType(0);
                        chatService.saveMessage(conv.getId(), phoneUser.getId(), dto);
                    } catch (Exception e) {
                        log.warn("聊天推送验证码到管理员 {} 失败: {}", adminId, e.getMessage());
                    }
                }

                // 2. WebSocket 实时推送（管理员在线即可收到）
                if (messagingTemplate != null) {
                    try {
                        messagingTemplate.convertAndSendToUser(
                                adminId.toString(), "/queue/verification-code",
                                Map.of("phone", maskedPhone, "code", code));
                    } catch (Exception e) {
                        log.warn("WebSocket 推送验证码失败: {}", e.getMessage());
                    }
                }
            }
        } catch (Exception e) {
            log.warn("推送验证码到管理员失败: {}", e.getMessage());
        }
    }

    /**
     * 用户登录
     * @param loginFromDTO
     * @return
     */
    @Override
    public Result<LoginInfo> login(LoginFromDTO loginFromDTO) {
        String password = loginFromDTO.getPassword();
        String username = loginFromDTO.getUsername();

        if (!RegexUtil.isUsername(username)) {
            return Result.error("用户名仅支持字母、数字和下划线，长度3-30位");
        }

        User u = userMapper.getByusername(username);
        if (u == null) {
            log.info("用户不存在");
            return Result.error("用户不存在");
        }

        if (u.getStatus() != 0) {
            log.info("用户被禁用或者越权操作");
            return Result.error("账号已被禁用，请联系管理员");
        }

        password = DigestUtils.md5DigestAsHex(password.getBytes(StandardCharsets.UTF_8));

        if (!password.equals(u.getPassword())) {
            log.info("密码错误");
            return Result.error("密码错误");
        }

        Map<String, Object> map = new HashMap<>();
        map.put("userId", u.getId());
        map.put("role", u.getRole());
        map.put("username", u.getUsername());
        map.put("status", u.getStatus());
        map.put("companyId", u.getCompanyId());

        String token = jwtUtil.generateToken(map);

        log.info("登录成功:{}", u);
        LoginInfo loginInfo = new LoginInfo(u.getId(), u.getRole(), u.getUsername(),
                u.getNickname(), u.getCompanyId(), u.getAvatarUrl(), u.getCompanyName(), token);
        return Result.success(loginInfo);
    }

    @Override
    public Result<LoginInfo> loginByPhone(LoginPhoneDTO dto) {
        String phone = dto.getPhone();
        String code = dto.getCode();

        if (!verifySmsCode(phone, code)) {
            return Result.error("验证码错误或已过期");
        }

        User u = userMapper.getByPhone(phone);
        if (u == null) {
            return Result.error("手机号未注册");
        }
        if (u.getStatus() != 0) {
            return Result.error("账号已被禁用，请联系管理员");
        }

        Map<String, Object> map = new HashMap<>();
        map.put("userId", u.getId());
        map.put("role", u.getRole());
        map.put("username", u.getUsername());
        map.put("status", u.getStatus());
        map.put("companyId", u.getCompanyId());

        String token = jwtUtil.generateToken(map);
        LoginInfo loginInfo = new LoginInfo(u.getId(), u.getRole(), u.getUsername(),
                u.getNickname(), u.getCompanyId(), u.getAvatarUrl(), u.getCompanyName(), token);
        return Result.success(loginInfo);
    }

    /**
     * 用户注册
     * @param register
     * @return
     */
    @OperationLog(action = "USER_REGISTER", targetType = "user", targetId = "#result.data.userId",
                  targetName = "#register.username")
    @Override
    public Result<LoginInfo> register(RegisterFromDTO register) {
        String username = register.getUsername();
        String password = register.getPassword();
        String phone = register.getPhone();
        Byte role = register.getRole();

        if (!RegexUtil.isUsername(username)) {
            return Result.error("用户名仅支持字母、数字和下划线，长度3-30位");
        }

        if (!RegexUtil.isPhone(phone)) {
            log.info("手机格式不正确");
            return Result.error("手机号码格式不正确");
        }

        if (role < 0 || role > 2) {
            log.info("非法身份认证");
            return Result.error("角色类型不合法");
        }

        User u = userMapper.getByusername(username);
        if (u != null) {
            log.info("用户名已存在");
            return Result.error("用户名已被占用");
        }

        password = DigestUtils.md5DigestAsHex(password.getBytes(StandardCharsets.UTF_8));

        register.setPassword(password);
        User user = BeanUtil.copyProperties(register, User.class);

        String s = UUID.randomUUID().toString().replaceAll("-", "");
        user.setNickname(s);
        user.setCompanyId(0L);

        userMapper.insert(user);

        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", user.getId());
        claims.put("role", user.getRole());
        claims.put("username", user.getUsername());
        claims.put("status", 0);
        claims.put("companyId", user.getCompanyId());
        String token = jwtUtil.generateToken(claims);

        LoginInfo loginInfo = new LoginInfo(user.getId(), user.getRole(), user.getUsername(),
                user.getNickname(), 0L, user.getAvatarUrl(), user.getCompanyName(), token);
        return Result.success(loginInfo);
    }

    @Override
    public Result<UserInfoVo> getUserInfo(Long userId) {

        User u = userMapper.getUserById(userId);
        UserInfoVo userInfoVo = BeanUtil.copyProperties(u, UserInfoVo.class);
        //对信息进行隐私处理
        String phone = userInfoVo.getPhone();
        String realname = userInfoVo.getRealname();
        phone=DesensitizeUtil.phone(phone);
        realname = DesensitizeUtil.name(realname);
        userInfoVo.setPhone(phone);
        userInfoVo.setRealname(realname);
        userInfoVo.setUserId(userId);

        return Result.success(userInfoVo);
    }

    //更新用户基本信息
    @Override
    public Result updateByInfo(UserInfoVo userInfoVo) {
        String phone = userInfoVo.getPhone();
        if (phone != null && !phone.isEmpty()) {
            if (phone.contains("*")) {
                userInfoVo.setPhone("");
            } else if (!RegexUtil.isPhone(phone)) {
                return Result.error("手机号码格式错误！");
            }
        }
        String realname = userInfoVo.getRealname();
        if (realname != null && realname.contains("*")) {
            userInfoVo.setRealname("");
        }

        String email = userInfoVo.getEmail();
        if (email != null && !email.isEmpty() && !RegexUtil.isEmail(email)) {
            log.info("邮箱格式错误！");
            return Result.error("邮箱格式错误！");
        }

        userMapper.updateByInfo(userInfoVo);
        return Result.success();

    }

    @OperationLog(action = "USER_CHANGE_PWD", targetType = "user", targetId = "#userId",
                  targetName = "#username")
    @Override
    public Result changePwd(ChangePwdDTO changePwdDTO) {
        String oldPassword = changePwdDTO.getOldPassword();
        String newPassword = changePwdDTO.getNewPassword();
        Long userId = UserHolder.getUserId();
        User u = userMapper.getByuserId(userId);

        if (newPassword == null || !RegexUtil.isPassword(newPassword)) {
            return Result.error("新密码格式不正确，需6-20位字母/数字或指定特殊字符");
        }

        oldPassword = DigestUtils.md5DigestAsHex(oldPassword.getBytes(StandardCharsets.UTF_8));
        if (!oldPassword.equals(u.getPassword())) {
            return Result.error("旧密码输入错误");
        }

        newPassword = DigestUtils.md5DigestAsHex(newPassword.getBytes(StandardCharsets.UTF_8));
        if (newPassword.equals(oldPassword)) {
            return Result.error("新密码不能与旧密码相同");
        }

        User user = new User();
        user.setId(userId);
        user.setPassword(newPassword);
        userMapper.update(user);


        return Result.success();
    }

    /**
     * 忘记密码 — 发送验证码
     * 校验用户名是否存在、手机号是否与注册手机号一致，通过后发送 SMS 验证码。
     */
    @Override
    public Result sendForgotPwdCode(ForgotPwdCodeDTO dto) {
        String username = dto.getUsername();
        String phone = dto.getPhone();

        if (!RegexUtil.isUsername(username)) {
            return Result.error("用户名格式不正确");
        }
        if (!RegexUtil.isPhone(phone)) {
            return Result.error("手机号格式不正确");
        }

        User u = userMapper.getByusername(username);
        if (u == null) {
            return Result.error("用户不存在");
        }

        if (!phone.equals(u.getPhone())) {
            return Result.error("手机号与注册手机号不一致");
        }

        return getCode(phone);
    }

    private static final String RESET_TOKEN_PREFIX = "reset:pwd:";
    private static final long RESET_TOKEN_TTL = 5; // 分钟

    /**
     * 忘记密码 — 验证验证码
     * 验证通过后消费验证码，签发临时重置令牌（5分钟有效），防止直接调 reset 接口绕过验证。
     */
    @Override
    public Result verifyForgotPwdCode(ForgotPwdCodeDTO dto) {
        String username = dto.getUsername();
        String phone = dto.getPhone();
        String code = dto.getCode();

        User u = userMapper.getByusername(username);
        if (u == null) {
            return Result.error("用户不存在");
        }
        if (!phone.equals(u.getPhone())) {
            return Result.error("手机号与注册手机号不一致");
        }
        // 验证并消费验证码，码只能用一次
        if (!verifySmsCode(phone, code)) {
            return Result.error("验证码错误或已过期");
        }

        // 签发临时重置令牌
        String resetToken = UUID.randomUUID().toString().replaceAll("-", "");
        try {
            redisTemplate.opsForValue().set(RESET_TOKEN_PREFIX + username, resetToken, RESET_TOKEN_TTL, TimeUnit.MINUTES);
        } catch (Exception e) {
            log.warn("Redis 存储重置令牌失败: {}", e.getMessage());
        }

        return Result.success(resetToken);
    }

    /**
     * 忘记密码 — 重置密码
     * 校验临时重置令牌（来自 verify 步骤签发），验证通过后更新密码并删除令牌。
     */
    @Override
    public Result resetPassword(ResetPwdDTO dto) {
        String username = dto.getUsername();
        String newPassword = dto.getNewPassword();
        String confirmPassword = dto.getConfirmPassword();
        String resetToken = dto.getResetToken();

        User u = userMapper.getByusername(username);
        if (u == null) {
            return Result.error("用户不存在");
        }

        // 校验重置令牌
        String tokenKey = RESET_TOKEN_PREFIX + username;
        String storedToken;
        try {
            storedToken = redisTemplate.opsForValue().get(tokenKey);
        } catch (Exception e) {
            log.warn("Redis 查询重置令牌失败: {}", e.getMessage());
            return Result.error("系统繁忙，请重新验证");
        }
        if (storedToken == null || !storedToken.equals(resetToken)) {
            return Result.error("重置令牌无效或已过期，请重新验证");
        }

        // 校验密码格式
        if (!RegexUtil.isPassword(newPassword)) {
            return Result.error("新密码格式不正确，需6-20位字母/数字或指定特殊字符");
        }

        // 两次密码一致性
        if (!newPassword.equals(confirmPassword)) {
            return Result.error("两次输入的密码不一致");
        }

        // MD5 加密并更新
        String encryptedPwd = DigestUtils.md5DigestAsHex(newPassword.getBytes(StandardCharsets.UTF_8));
        User user = new User();
        user.setId(u.getId());
        user.setPassword(encryptedPwd);
        userMapper.update(user);

        // 删除已使用的令牌
        try {
            redisTemplate.delete(tokenKey);
        } catch (Exception e) {
            log.warn("Redis 删除重置令牌失败: {}", e.getMessage());
        }

        // 记录操作日志
        try {
            com.itzk.SmartEmploymentPlatform.pojo.entry.OperationLog opLog =
                    new com.itzk.SmartEmploymentPlatform.pojo.entry.OperationLog();
            opLog.setUserId(u.getId());
            opLog.setOperatorName(u.getUsername());
            opLog.setAction("USER_RESET_PWD");
            opLog.setTargetType("user");
            opLog.setTargetId(u.getId());
            opLog.setTargetName(u.getUsername());
            opLog.setIpAddress(getClientIp());
            operationLogMapper.insert(opLog);
        } catch (Exception e) {
            log.warn("写入操作日志失败: {}", e.getMessage());
        }

        log.info("用户 {} 通过忘记密码重置了密码", username);
        return Result.success();
    }

    private String getClientIp() {
        try {
            ServletRequestAttributes attrs = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (attrs != null) {
                String ip = attrs.getRequest().getHeader("X-Forwarded-For");
                if (ip != null && !ip.isEmpty() && !"unknown".equalsIgnoreCase(ip)) {
                    return ip.split(",")[0].trim();
                }
                ip = attrs.getRequest().getHeader("X-Real-IP");
                if (ip != null && !ip.isEmpty()) return ip;
                return attrs.getRequest().getRemoteAddr();
            }
        } catch (Exception ignored) {}
        return "unknown";
    }

    private static final String USER_STATS_PREFIX = "user:stats:";
    private static final long USER_STATS_TTL = 5; // 分钟

    @Override
    public Result<Map<String, Integer>> getUserStats(Long userId) {
        // 1. 查 Redis 缓存
        String cacheKey = USER_STATS_PREFIX + userId;
        try {
            String cached = redisTemplate.opsForValue().get(cacheKey);
            if (cached != null) {
                @SuppressWarnings("unchecked")
                Map<String, Integer> map = JSON.parseObject(cached, Map.class);
                return Result.success(map);
            }
        } catch (Exception e) {
            log.warn("Redis 读取用户统计缓存失败，降级查 DB: {}", e.getMessage());
        }

        // 2. 查 DB
        int favorites = favorsMapper.selectJobIdsByUserId(userId).size();
        int applications = applicationMapper.getByUserId(userId).size();
        int interviews = interviewMapper.countByUserId(userId);

        Map<String, Integer> stats = new LinkedHashMap<>();
        stats.put("favorites", favorites);
        stats.put("applications", applications);
        stats.put("interviews", interviews);

        // 3. 写 Redis 缓存
        try {
            redisTemplate.opsForValue().set(cacheKey, JSON.toJSONString(stats), USER_STATS_TTL, TimeUnit.MINUTES);
        } catch (Exception e) {
            log.warn("Redis 写入用户统计缓存失败: {}", e.getMessage());
        }

        return Result.success(stats);
    }


    /**
     * 仅校验验证码是否正确，不删除（供 pre-check 步骤使用）
     */
    private boolean checkSmsCode(String phone, String code) {
        if (!StringUtils.hasText(phone) || !StringUtils.hasText(code)) {
            return false;
        }
        String codeKey = SMS_CODE_PREFIX + phone;
        try {
            String redisCode = redisTemplate.opsForValue().get(codeKey);
            return redisCode != null && code.equalsIgnoreCase(redisCode);
        } catch (Exception e) {
            log.warn("Redis 查询验证码失败: {}", e.getMessage());
            return false;
        }
    }

    /**
     * 校验验证码是否正确（校验成功后删除，防止重复使用）
     * @param phone 手机号
     * @param code 用户输入的验证码
     * @return true=正确 false=错误
     */
    public boolean verifySmsCode(String phone, String code) {
        if (!StringUtils.hasText(phone) || !StringUtils.hasText(code)) {
            return false;
        }

        String codeKey = SMS_CODE_PREFIX + phone;
        String redisCode;
        try {
            redisCode = redisTemplate.opsForValue().get(codeKey);
        } catch (Exception e) {
            log.warn("Redis 查询验证码失败，校验不通过: {}", e.getMessage());
            return false;
        }

        if (redisCode == null) {
            return false;
        }

        boolean result = code.equalsIgnoreCase(redisCode);

        // 校验成功后立即删除验证码，防止重复使用
        if (result) {
            try {
                redisTemplate.delete(codeKey);
            } catch (Exception e) {
                log.warn("Redis 删除验证码失败: {}", e.getMessage());
            }
        }

        return result;
    }

    /**
     * 首页统计：在招岗位、入驻企业、求职者、成功匹配
     *
     * Redis 缓存 30 分钟，所有用户共享。
     * 数字变化不频繁，30 分钟延迟对首页展示无影响。
     */
    @Override
    public Result<Map<String, Object>> getHomeStats() {
        try {
            String cached = redisTemplate.opsForValue().get(HOME_STATS_KEY);
            if (cached != null) {
                return Result.success(JSON.parseObject(cached, Map.class));
            }
        } catch (Exception e) {
            log.warn("Redis 读取首页统计缓存失败，降级查 DB: {}", e.getMessage());
        }

        long jobs = jobsMapper.countActiveJobs();
        long companies = companyMapper.countAudited();
        long candidates = userMapper.countJobSeekers();
        long matches = applicationMapper.countHired();

        Map<String, Object> stats = new LinkedHashMap<>();
        stats.put("jobs", jobs);
        stats.put("companies", companies);
        stats.put("candidates", candidates);
        stats.put("matches", matches);

        try {
            redisTemplate.opsForValue().set(HOME_STATS_KEY, JSON.toJSONString(stats), HOME_STATS_TTL, TimeUnit.MINUTES);
        } catch (Exception e) {
            log.warn("Redis 写入首页统计缓存失败: {}", e.getMessage());
        }
        return Result.success(stats);
    }

}



