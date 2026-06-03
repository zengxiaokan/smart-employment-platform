package com.itzk.SmartEmploymentPlatform.controller.recruitment;

import com.itzk.SmartEmploymentPlatform.pojo.Result;
import com.itzk.SmartEmploymentPlatform.pojo.entry.FavorResumDTO;
import com.itzk.SmartEmploymentPlatform.pojo.entryDTO.QueryResumeDTO;
import com.itzk.SmartEmploymentPlatform.service.ResumeService;
import com.itzk.SmartEmploymentPlatform.service.SearchGoodPeopleService;
import com.itzk.SmartEmploymentPlatform.utils.UserHolder;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import java.time.LocalDateTime;

@RestController
@Slf4j
@RequestMapping("/hr/talent")
public class SearchGoodPeopleController {


    @Autowired
    private SearchGoodPeopleService searchGoodPeopleService;

    @GetMapping("/search")
    public Result searchGoodPeople( QueryResumeDTO dto) {
        log.info("接收到的参数:{}",dto);
        //进行分页查询
        return searchGoodPeopleService.getAllIntention(dto);
    }

    @GetMapping("/resume/{resumeId}")
    public Result getResumeById(@PathVariable Long resumeId) {
        log.info("获取简历{}",resumeId);
        return searchGoodPeopleService.getResumeById(resumeId);
    }

    //收藏用户的简历
    @PostMapping("/favorite")
    public Result favorite(@RequestBody FavorResumDTO dto) {
        Long companyId = UserHolder.getCompanyId();
        dto.setCompanyId(companyId);
        dto.setCreateAt(LocalDateTime.now());
        return searchGoodPeopleService.saveCompanyResume(dto);

    }
    //收藏用户的简历
    @DeleteMapping("/favorite/{resumeId}")
    public Result CancelFavorite(@PathVariable Long resumeId) {

        return searchGoodPeopleService.cancelCompanyResume(resumeId);

    }

    /**
     * 获取公司全部收藏对象
     * @return
     */

    @GetMapping("favorites")
    public Result getFavorite() {
        return searchGoodPeopleService.getFavorites();
    }

    /**
     * 修改备注
     * @param resumeId
     * @return
     */
    @PutMapping("favorite/reason")
    public Result updateFavorite(Long resumeId,String reason) {
        return searchGoodPeopleService.updataRemark(resumeId,reason);
    }



}
