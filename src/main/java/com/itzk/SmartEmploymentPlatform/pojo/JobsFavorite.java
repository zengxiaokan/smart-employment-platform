package com.itzk.SmartEmploymentPlatform.pojo;

import com.itzk.SmartEmploymentPlatform.pojo.entry.Job;
import com.itzk.SmartEmploymentPlatform.pojo.vo.SimpleCompanyVo;
import lombok.Data;

@Data
public class JobsFavorite {

    private Job job;

    private SimpleCompanyVo company;

    private boolean favorite;

    private boolean submitted;
}
