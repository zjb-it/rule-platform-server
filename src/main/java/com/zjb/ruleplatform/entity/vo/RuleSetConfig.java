package com.zjb.ruleplatform.entity.vo;

import lombok.Data;

import java.util.List;

/**
 * @author 赵静波
 * Created on 2021-03-07
 */
@Data
public class RuleSetConfig {

    private Long ruleSetId;

    private List<RuleInfo> rules;


    private RuleInfo defaultRule;


}
