package com.itzk.SmartEmploymentPlatform.pojo.entry;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class FavorResumDTO {

    private Long resumeId;

    private Long userId;

    private Long companyId;

    @JsonProperty("reason")
    private String remark;

    private LocalDateTime createAt;



}
