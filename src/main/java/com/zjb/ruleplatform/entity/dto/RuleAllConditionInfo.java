/**
 * *****************************************************
 * Copyright (C) 2020 zjb.com. All Rights Reserved
 * This file is part of zjb zjb project.
 * Unauthorized copy of this file, via any medium is strictly prohibited.
 * Proprietary and Confidential.
 * ****************************************************
 * <p>
 * History:
 * <author>            <time>          <version>          <desc>
 * 作者姓名           修改时间           版本号            描述
 */
package com.zjb.ruleplatform.entity.dto;

import com.zjb.ruleplatform.entity.RuleEngineCondition;
import com.zjb.ruleplatform.entity.RuleEngineElement;
import com.zjb.ruleplatform.entity.RuleEngineRuleConditionGroup;
import com.zjb.ruleplatform.entity.RuleEngineVariable;
import lombok.Data;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 〈一句话功能简述〉<br>
 * 〈〉
 *
 * @author v-dingqianwen.ea
 * @create 2020/5/9
 * @since 1.0.0
 */
@Data
public class RuleAllConditionInfo {
    /**
     * 条件组
     */
    private Map<Long, List<RuleEngineRuleConditionGroup>> ruleEngineRuleConditionGroupMap = new HashMap<>();
    /**
     * 用到的条件
     */
    private Map<Long, RuleEngineCondition> conditionMap = new HashMap<>();

    /**
     * 条件中用到的变量
     */
    private HashMap<Long, RuleEngineVariable> variableMap = new HashMap<>();
    /**
     * 条件中用到的元素
     */
    private HashMap<Long, RuleEngineElement> elementMap = new HashMap<>();
}