package com.zjb.ruleplatform.entity.vo;

import com.zjb.ruleplatform.entity.dto.ConditionParam;
import com.zjb.ruleplatform.entity.dto.ConfigBean;
import lombok.Data;

import java.util.List;

/**
 * @author 赵静波 <zhaojingbo>
 * Created on 2021-02-07
 */
@Data
public class RuleDetail extends RuleInfo {

    private LeftBean action;

    private List<ConditionGroup> conditionGroups;

}
