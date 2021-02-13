package com.zjb.ruleplatform.entity.vo;

import com.zjb.ruleplatform.entity.dto.ConditionParam;
import lombok.Data;

import java.util.List;

@Data
public class ConditionGroupDetail {
    private List<ConditionParam> conditions;
    private Integer order;
}