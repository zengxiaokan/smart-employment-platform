package com.itzk.SmartEmploymentPlatform.pojo.entryDTO;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class UpdataStatusDTO {

    /** 修改表的id */
    private Long id;

    /** 关联的申请ID（面试状态更新时需要） */
    private Long applicationId;

    /** 修改什么状态 */
    private Byte status;

    /** HR备注 */
    @JsonProperty("hrRemark")
    private String hrRemark;
    @JsonProperty("candidateRemark")
    private String userRemark;

}
