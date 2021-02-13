package com.zjb.ruleplatform.entity.vo;

import com.zjb.ruleplatform.manager.RuleEngineVariableParamManager;

import java.util.Collection;

/**
 * @author 赵静波
 * Created on 2021-02-09
 */
public interface CollectorValue {
    /**
     * 元素
     * @return 元素ID
     * @param variableParamManager
     */
    Collection<Long> collectorElement(RuleEngineVariableParamManager variableParamManager);

    /**
     * 变量
     * @return 变量id
     * @param variableParamManager
     */
    Collection<Long> collectorVariable(RuleEngineVariableParamManager variableParamManager);

}
