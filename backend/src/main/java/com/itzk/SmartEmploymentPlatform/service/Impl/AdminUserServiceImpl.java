package com.itzk.SmartEmploymentPlatform.service.Impl;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.itzk.SmartEmploymentPlatform.exception.BusinessException;
import com.itzk.SmartEmploymentPlatform.mapper.UserMapper;
import com.itzk.SmartEmploymentPlatform.pojo.entry.User;
import com.itzk.SmartEmploymentPlatform.pojo.vo.AdminHrVO;
import com.itzk.SmartEmploymentPlatform.pojo.vo.AdminUserVO;
import com.itzk.SmartEmploymentPlatform.annotation.OperationLog;
import com.itzk.SmartEmploymentPlatform.mapper.OperationLogMapper;
import com.itzk.SmartEmploymentPlatform.service.AdminUserService;
import com.itzk.SmartEmploymentPlatform.utils.UserHolder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
public class AdminUserServiceImpl implements AdminUserService {

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private OperationLogMapper operationLogMapper;

    private static final Set<Integer> USER_ROLES = Set.of(0, 1, 2, 3);

    // ==================== 用户管理 ====================

    @Override
    public PageInfo<AdminUserVO> listUsers(int page, int size, String username, String keyword, Integer role, Integer status) {
        PageHelper.startPage(page, size);
        List<AdminUserVO> list = userMapper.selectAdminUsers(username, keyword, role, status);
        return new PageInfo<>(list);
    }

    @Override
    @Transactional
    public void createUser(Map<String, Object> body) {
        String username = (String) body.get("username");
        String password = (String) body.get("password");
        String phone = (String) body.get("phone");
        Integer role = body.get("role") != null ? ((Number) body.get("role")).intValue() : null;

        if (username == null || username.isBlank()) throw new BusinessException("用户名不能为空");
        if (password == null || password.length() < 6) throw new BusinessException("密码至少6位");
        if (role == null || !USER_ROLES.contains(role)) throw new BusinessException("角色不合法");
        if (role == 2 && UserHolder.getUserId() != 1L) throw new BusinessException("仅超级管理员可创建管理员账号");

        if (userMapper.countByUsername(username) > 0) throw new BusinessException("用户名已存在");
        if (phone != null && !phone.isBlank() && userMapper.countByPhone(phone) > 0)
            throw new BusinessException("手机号已被注册");

        User user = new User();
        user.setUsername(username);
        user.setPassword(password);
        user.setPhone(phone);
        user.setRealname((String) body.get("realname"));
        user.setNickname((String) body.get("nickname"));
        user.setCity((String) body.get("city"));
        user.setRole(role.byteValue());
        user.setStatus(body.get("status") != null ? ((Number) body.get("status")).byteValue() : (byte) 0);

        userMapper.insertAdmin(user);
    }

    @Override
    @Transactional
    public void updateUser(Long id, Map<String, Object> body) {
        Integer role = body.get("role") != null ? ((Number) body.get("role")).intValue() : null;
        if (role != null && !USER_ROLES.contains(role)) throw new BusinessException("角色不合法");

        User user = new User();
        user.setId(id);
        user.setPhone((String) body.get("phone"));
        user.setRealname((String) body.get("realname"));
        user.setNickname((String) body.get("nickname"));
        user.setCity((String) body.get("city"));
        if (role != null) user.setRole(role.byteValue());
        if (body.get("status") != null) user.setStatus(((Number) body.get("status")).byteValue());

        userMapper.update(user);

        if (body.get("status") != null) {
            int status = ((Number) body.get("status")).intValue();
            String action = status == 1 ? "BAN_USER" : "UNBAN_USER";
            com.itzk.SmartEmploymentPlatform.pojo.entry.OperationLog log =
                    new com.itzk.SmartEmploymentPlatform.pojo.entry.OperationLog();
            log.setUserId(UserHolder.getUserId());
            log.setOperatorName(UserHolder.getUsername());
            log.setAction(action);
            log.setTargetType("user");
            log.setTargetId(id);
            User target = userMapper.getUserById(id);
            if (target != null) log.setTargetName(target.getUsername());
            operationLogMapper.insert(log);
        }
    }

    @OperationLog(action = "DELETE_USER", targetType = "user", targetId = "#id")
    @Override
    @Transactional
    public void deleteUser(Long id) {
        if (UserHolder.getUserId() != 1L) throw new BusinessException("仅超级管理员可删除账号");
        userMapper.deleteById(id);
    }

    // ==================== HR 管理（role=1） ====================

    @Override
    public PageInfo<AdminHrVO> listHrs(int page, int size, String name, Integer auditStatus, Integer status) {
        PageHelper.startPage(page, size);
        List<AdminHrVO> list = userMapper.selectAdminHrs(name, auditStatus, status);
        return new PageInfo<>(list);
    }

    @Override
    @Transactional
    public void createHr(Map<String, Object> body) {
        String username = (String) body.get("username");
        String password = (String) body.get("password");
        String phone = (String) body.get("phone");

        if (username == null || username.isBlank()) throw new BusinessException("用户名不能为空");
        if (password == null || password.length() < 6) throw new BusinessException("密码至少6位");

        if (userMapper.countByUsername(username) > 0) throw new BusinessException("用户名已存在");
        if (phone != null && !phone.isBlank() && userMapper.countByPhone(phone) > 0)
            throw new BusinessException("手机号已被注册");

        User user = new User();
        user.setUsername(username);
        user.setPassword(password);
        user.setPhone(phone);
        user.setRealname((String) body.get("realname"));
        user.setNickname((String) body.get("nickname"));
        user.setCity((String) body.get("city"));
        user.setRole((byte) 1);
        user.setStatus(body.get("status") != null ? ((Number) body.get("status")).byteValue() : (byte) 0);

        userMapper.insertAdmin(user);
    }

    @Override
    @Transactional
    public void updateHr(Long id, Map<String, Object> body) {
        User user = new User();
        user.setId(id);
        user.setPhone((String) body.get("phone"));
        user.setRealname((String) body.get("realname"));
        user.setNickname((String) body.get("nickname"));
        user.setCity((String) body.get("city"));
        user.setRole((byte) 1);
        if (body.get("status") != null) user.setStatus(((Number) body.get("status")).byteValue());

        userMapper.update(user);

        if (body.get("status") != null) {
            int status = ((Number) body.get("status")).intValue();
            String action = status == 1 ? "BAN_USER" : "UNBAN_USER";
            com.itzk.SmartEmploymentPlatform.pojo.entry.OperationLog log =
                    new com.itzk.SmartEmploymentPlatform.pojo.entry.OperationLog();
            log.setUserId(UserHolder.getUserId());
            log.setOperatorName(UserHolder.getUsername());
            log.setAction(action);
            log.setTargetType("user");
            log.setTargetId(id);
            User target = userMapper.getUserById(id);
            if (target != null) log.setTargetName(target.getUsername());
            operationLogMapper.insert(log);
        }
    }

    @OperationLog(action = "DELETE_HR", targetType = "user", targetId = "#id")
    @Override
    @Transactional
    public void deleteHr(Long id) {
        if (UserHolder.getUserId() != 1L) throw new BusinessException("仅超级管理员可删除账号");
        userMapper.deleteById(id);
    }
}
