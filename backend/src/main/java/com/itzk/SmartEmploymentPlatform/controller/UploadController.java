package com.itzk.SmartEmploymentPlatform.controller;


import com.itzk.SmartEmploymentPlatform.mapper.UserMapper;
import com.itzk.SmartEmploymentPlatform.pojo.Result;
import com.itzk.SmartEmploymentPlatform.utils.AliyunOSSOperator;
import com.itzk.SmartEmploymentPlatform.utils.UserHolder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@Slf4j
@RestController
@RequestMapping("/upload")
public class UploadController {

    @Autowired
    private AliyunOSSOperator aliyunOSSOperator;

    @Autowired
    private UserMapper userMapper;

    /**
     * 上传个人头像
     * @param file
     * @return
     * @throws Exception
     */
    @PostMapping("/avatar")
    public Result<String> upload(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            return Result.error("文件不能为空");
        }
        log.info("文件上传：{}", file.getOriginalFilename());

        String avatarUrl;
        try {
            avatarUrl = aliyunOSSOperator.uplocad(file, file.getOriginalFilename());
        } catch (Exception e) {
            log.error("文件上传OSS失败", e);
            return Result.error("文件上传失败，请稍后重试");
        }
        log.info("文件上传OSS,url:{}", avatarUrl);
        Long userId = UserHolder.getUserId();
        userMapper.uodataAvatar(userId,avatarUrl);

        //将文件交给OSS存储
        return Result.success(avatarUrl);
    }


    /**
     * 上传简历图片
     * @param file
     * @return
     * @throws Exception
     */
    @PostMapping("/resume")
    public Result<String> uploadResume(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            return Result.error("文件不能为空");
        }
        log.info("文件上传：{}", file.getOriginalFilename());

        String characterAvatar;
        try {
            characterAvatar = aliyunOSSOperator.uplocad(file, file.getOriginalFilename());
        } catch (Exception e) {
            log.error("文件上传OSS失败", e);
            return Result.error("文件上传失败，请稍后重试");
        }
        log.info("文件上传OSS,url:{}", characterAvatar);

        //将文件交给OSS存储
        return Result.success(characterAvatar);
    }

    /**
     * 上传营业执照
     * @param file
     * @return
     * @throws Exception
     */
    @PostMapping("/company")
    public Result<String> uploadCompany(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            return Result.error("文件不能为空");
        }
        log.info("文件上传：{}", file.getOriginalFilename());

        String characterAvatar;
        try {
            characterAvatar = aliyunOSSOperator.uplocad(file, file.getOriginalFilename());
        } catch (Exception e) {
            log.error("文件上传OSS失败", e);
            return Result.error("文件上传失败，请稍后重试");
        }
        log.info("文件上传OSS,url:{}", characterAvatar);

        //将文件交给OSS存储
        return Result.success(characterAvatar);
    }


}
