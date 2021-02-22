package com.zjb.ruleplatform.entity.dto;

import lombok.Data;

import java.util.Map;

/**
 * @author 赵静波
 * @date 2021-02-10 20:06:24
 */
@Data
public class RuleExecuteParam {
    private String ruleCode;
    private Map<String,Object> ruleParam;
}
