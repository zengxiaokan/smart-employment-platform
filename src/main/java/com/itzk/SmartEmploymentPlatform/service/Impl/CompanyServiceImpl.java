package com.itzk.SmartEmploymentPlatform.service.Impl;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.Page;
import com.itzk.SmartEmploymentPlatform.mapper.CompanyMapper;
import com.itzk.SmartEmploymentPlatform.mapper.JobsMapper;
import com.itzk.SmartEmploymentPlatform.pojo.PageResult;
import com.itzk.SmartEmploymentPlatform.pojo.Result;
import com.itzk.SmartEmploymentPlatform.pojo.entry.Company;
import com.itzk.SmartEmploymentPlatform.pojo.entry.Job;
import com.itzk.SmartEmploymentPlatform.pojo.entryDTO.CompanyQueryDTO;
import com.itzk.SmartEmploymentPlatform.pojo.entryDTO.QueryJobsByCompanyIdDTO;
import com.itzk.SmartEmploymentPlatform.pojo.vo.CompanyJobsVo;
import com.itzk.SmartEmploymentPlatform.pojo.vo.CompanyVO;
import com.itzk.SmartEmploymentPlatform.pojo.vo.SimpleJobDetailVo;
import com.itzk.SmartEmploymentPlatform.service.CompanyService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@Slf4j
public class CompanyServiceImpl implements CompanyService {

    @Autowired
    private CompanyMapper companyMapper;

    @Autowired
    private JobsMapper jobsMapper;




    @Override
    public Map<String, List<String>> getFilterItems() {
        // 1. 查询所有不重复的行业
        List<String> industries = companyMapper.getDistinctIndustries();
        
        // 2. 查询所有不重复的规模
        List<String> sizes = companyMapper.getDistinctSizes();
        
        // 3. 封装成你要的 MAP
        Map<String, List<String>> result = new HashMap<>();
        result.put("industries", industries);
        result.put("sizes", sizes);
        
        return result;
    }

    @Override
    public Result<PageResult> getCompanyList(CompanyQueryDTO queryDTO) {
        PageHelper.startPage(queryDTO.getPage(), queryDTO.getSize());

        List<CompanyVO> list = companyMapper.selectCompanyList(queryDTO);

        Page<CompanyVO> page = (Page<CompanyVO>) list;
        PageResult pageResult = new PageResult(page.getTotal(), list);

        return Result.success(pageResult);
    }

    @Override
    @Transactional
    public Result getCompanyJobsDetail(Long id) {

        //查询公司信息
        Company company = companyMapper.getById(id);
        CompanyVO companyVO = new CompanyVO();
        BeanUtils.copyProperties(company, companyVO);
        //获取公司所有发布职位===还得分页查询
        int start = 0;
        int total = 10;
        List<Job> list = jobsMapper.getJobsByCompanyId(id,start,total);
        List<SimpleJobDetailVo> jobList = new ArrayList<>();
        CompanyJobsVo vo  = new CompanyJobsVo();
        vo.setCompanyVO(companyVO);
        if (list == null|| list.isEmpty()){
            vo.setJobList(jobList);
            return Result.success(vo);
        }


        for (Job job : list) {
            SimpleJobDetailVo simpleJobDetailVo = new SimpleJobDetailVo();
            BeanUtils.copyProperties(job, simpleJobDetailVo);
            jobList.add(simpleJobDetailVo);
        }

        //封装对象并返回


        vo.setJobList(jobList);

        return Result.success(vo);
    }

    @Override
    public Result PageQueryJobsByCompanyId(Long companyId, QueryJobsByCompanyIdDTO query) {
        int i = query.getPage() - 1;
        int page = i*10;
        //分页查询岗位信息
        List<Job> jobList = jobsMapper.getJobsByCompanyId(companyId, page, query.getPageSize());


        return Result.success(jobList);
    }

    /**
     * hr相关接口，获取注册公司状态
     * @param companyId
     * @return
     */

    @Override
    @Transactional
    public Result getCompanyStatus(Long companyId) {
        if (companyId == 0){
            return Result.error("请重新登录获取状态");
        }


        Company company = companyMapper.getById(companyId);


        return Result.success(company);
    }

    /**
     * 更新公司信息
     * @param company
     * @return
     */
    @Override
    public Result updata(Company company) {
        companyMapper.updataById(company);
        return Result.success();
    }

    /**
     * 模糊匹配公司名
     * @param keyword
     * @return
     */




}