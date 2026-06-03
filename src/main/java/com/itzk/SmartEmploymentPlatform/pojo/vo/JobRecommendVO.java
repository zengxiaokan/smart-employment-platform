package com.itzk.SmartEmploymentPlatform.pojo.vo;

import com.itzk.SmartEmploymentPlatform.pojo.entry.Job;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class JobRecommendVO {
    // 完整的职位对象（你要返回给前端的所有属性）
    private Job job;
    
    // 匹配分数（0-100分）
    private Integer matchScore;
}