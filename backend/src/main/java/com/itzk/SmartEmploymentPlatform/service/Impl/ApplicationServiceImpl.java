package com.itzk.SmartEmploymentPlatform.service.Impl;

import com.itzk.SmartEmploymentPlatform.mapper.ApplicationMapper;
import com.itzk.SmartEmploymentPlatform.mapper.JobsMapper;
import com.itzk.SmartEmploymentPlatform.mapper.ResumeMapper;
import com.itzk.SmartEmploymentPlatform.pojo.Result;
import com.itzk.SmartEmploymentPlatform.pojo.entry.Application;
import com.itzk.SmartEmploymentPlatform.pojo.entry.Job;
import com.itzk.SmartEmploymentPlatform.pojo.entry.Resume;
import com.itzk.SmartEmploymentPlatform.pojo.entryDTO.ApplicationDTO;
import com.itzk.SmartEmploymentPlatform.pojo.vo.ApplicationDitailVo;
import com.itzk.SmartEmploymentPlatform.pojo.vo.SimpleApplicationVo;
import com.itzk.SmartEmploymentPlatform.annotation.OperationLog;
import com.itzk.SmartEmploymentPlatform.service.ApplicationService;
import com.itzk.SmartEmploymentPlatform.utils.Constant;
import com.itzk.SmartEmploymentPlatform.utils.RedisKeyConstants;
import com.itzk.SmartEmploymentPlatform.utils.RedisUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Service
@Slf4j
public class ApplicationServiceImpl implements ApplicationService {


    @Autowired
    private ApplicationMapper applicationMapper;

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    @Autowired
    private ResumeMapper resumeMapper;

    @Autowired
    private JobsMapper jobsMapper;

    @Override
    public Result deleteByUSerIdAndId(long userId, Long applicationId) {

        byte status = Constant.ApplicationStatus.APPLY_CANCER;


        int row = applicationMapper.updataById(userId,applicationId,status);
        if(row==0){
            return Result.error("数据修改错误");
        }
        //一天之内不允许再次投递
        if (!RedisUtil.skipRedis()) {
            try {
                String lockKey = RedisKeyConstants.APPLY_LOCK_PREFIX + userId + ":" + applicationId;
                stringRedisTemplate.opsForValue().set(lockKey, "1", 24, TimeUnit.HOURS);
            } catch (Exception e) {
                log.warn("Redis 设置投递冷却锁失败: {}", e.getMessage());
                RedisUtil.markFailed();
            }
        }

        return Result.success();
    }

    /**
     * 提交简历
     * @param applicationDTO
     * @return
     */
    @OperationLog(action = "JOB_APPLY", targetType = "job", targetId = "#applicationDTO.jobId")
    @Override
    @Transactional
    public Result commitApplication(ApplicationDTO applicationDTO) {
        //验证数据，通过jobs表查询公司id验证公司是否存在这个岗位
        Long userId = applicationDTO.getUserId();
        Long resumeId = applicationDTO.getResumeId();
        Long jobId = applicationDTO.getJobId();
        Long companyId = applicationDTO.getCompanyId();

        Job j = jobsMapper.getCompanyIdByjobId(jobId);
        if (j == null || j.getCompanyId() == null) {
            return Result.error("无效的职位");
        }
        if (!j.getCompanyId().equals(companyId)) {
            return Result.error("该公司并没有设立此职位");
        }
        if (j.getHasCount() <= 0) {
            jobsMapper.forceOffline(jobId);
            return Result.error("岗位不存在");
        }
        //验证简历正确性
        int row = resumeMapper.getByuserId(userId);
        if (row == 0) {
            return Result.error("无效的简历");
        }

        //创建简历实体，将数据封装，设置申请时间

        Application application = new Application();
        BeanUtils.copyProperties(applicationDTO, application);

        application.setStatus(Byte.valueOf("0"));
        LocalDateTime now = LocalDateTime.now();
        application.setAppliedAt(now);
        application.setUpdatedAt(now);
        application.setOperatorId(userId);

        //执行插入操作
        applicationMapper.insertApply(application);

        //设置已投递锁，除非取消投递，否则锁存在，让用户不会多次投递
        if (!RedisUtil.skipRedis()) {
            try {
                stringRedisTemplate.opsForSet().add(RedisKeyConstants.JOB_VIEW_SUBMITTED_PREFIX + userId, String.valueOf(jobId));
            } catch (Exception e) {
                log.warn("Redis 设置已投递锁失败: {}", e.getMessage());
                RedisUtil.markFailed();
            }
        }

        //将jobs表中apply_count数量加1
        int cow = jobsMapper.addApplyCountById(jobId);
        if (cow == 0) {
            return Result.error("数据错误");
        }

        return Result.success();
    }

    @Override
    public List<SimpleApplicationVo> getByUserId(Long userId) {


        return applicationMapper.getByUserId(userId);
    }

    /**
     * 再次投递
     * @param userId
     * @param applicationId
     * @return
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result replyApplication(Long userId, Long applicationId) {
        // 防重复：已是再次投递状态则不允许重复操作
        Application exist = applicationMapper.getById(applicationId);
        if (exist != null && exist.getStatus() != null
                && exist.getStatus() == Constant.ApplicationStatus.APPLY_Double) {
            return Result.error("已经再次投递，请不要重新投递");
        }

        // 冷却锁检查（Redis不可用时跳过，不影响核心流程）
        String lockKey = RedisKeyConstants.APPLY_LOCK_PREFIX + userId + ":" + applicationId;
        if (!RedisUtil.skipRedis()) {
            try {
                if (Boolean.TRUE.equals(stringRedisTemplate.hasKey(lockKey))) {
                    return Result.error("您已取消该职位投递，需等待24小时后才能再次投递");
                }
                RedisUtil.markSuccess();
            } catch (Exception e) {
                log.warn("Redis 查询冷却锁失败，放行: {}", e.getMessage());
                RedisUtil.markFailed();
            }
        }

        byte status = Constant.ApplicationStatus.APPLY_Double;
        int i = applicationMapper.updataById(userId, applicationId, status);
        if (i == 0) {
            return Result.error("数据异常");
        }

        // 重新投递也计入投递次数
        if (exist != null) {
            jobsMapper.addApplyCountById(exist.getJobId());
        }

        if (!RedisUtil.skipRedis()) {
            try {
                stringRedisTemplate.opsForValue().set(lockKey, "1", 24, TimeUnit.HOURS);
            } catch (Exception e) {
                log.warn("Redis 设置冷却锁失败: {}", e.getMessage());
            }
        }
        return Result.success();
    }

    @Override
    public Result<ApplicationDitailVo> JobandConpanyInfo(Long applicationId) {
        ApplicationDitailVo vo =applicationMapper.getJobAndCompanyInfo(applicationId);

        return Result.success(vo);
    }
}
