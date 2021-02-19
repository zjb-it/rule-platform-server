package com.zjb.ruleplatform.entity.vo;

import com.zjb.ruleplatform.entity.dto.ConditionParam;
import lombok.Data;

import java.util.List;

/**
 * @author 赵静波
 * @date 2021-02-07 17:09:37
 */
@Data
public class ConditionGroupDetail {
    private List<ConditionParam> conditions;
    private Integer order;
}