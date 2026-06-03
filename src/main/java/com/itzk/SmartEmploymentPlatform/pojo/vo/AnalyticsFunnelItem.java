package com.itzk.SmartEmploymentPlatform.pojo.vo;

import lombok.Data;

@Data
public class AnalyticsFunnelItem {
    private String position;
    private int applications;
    private int interviews;
    private int hired;
}
