package com.itzk.SmartEmploymentPlatform.pojo.vo;

import com.itzk.SmartEmploymentPlatform.pojo.entry.CompanyTodo;
import lombok.Data;

import java.util.List;

/**
 * 首页概览
 */
@Data
public class DashboardVO {

    /** 在招职位数 */
    private int jobCount;

    /** 本月收到简历数 */
    private int resumeCount;

    /** 本月面试安排数 */
    private int interviewCount;

    /** 本月入职数 */
    private int hireCount;

    /** 最新5条投递 */
    private List<ApplicationHrVO> latestApplications;

    /** 最近5条待办 */
    private List<CompanyTodo> todoList;
}
