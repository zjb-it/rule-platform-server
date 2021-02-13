package com.zjb.ruleplatform.entity.dto;

import lombok.Data;

import java.util.Map;

/**
 * @author 赵静波
 * Created on 2021-02-10
 */
@Data
public class RuleExecuteParam {
    private String ruleCode;
    private Map<String,Object> ruleParam;
}
