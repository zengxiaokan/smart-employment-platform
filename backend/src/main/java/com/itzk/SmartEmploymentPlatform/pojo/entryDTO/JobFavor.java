package com.itzk.SmartEmploymentPlatform.pojo.entryDTO;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class JobFavor {

    Long id;

    Long userId;

    Long jobId;

    LocalDateTime createAt;

}
