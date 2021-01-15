package com.zjb.ruleplatform.service;


import com.zjb.ruleplatform.entity.common.PageRequest;
import com.zjb.ruleplatform.entity.common.PageResult;
import com.zjb.ruleplatform.entity.dto.ListRuleEngineVariableRequest;
import com.zjb.ruleplatform.entity.dto.RuleEngineVariableRequest;
import com.zjb.ruleplatform.entity.vo.GetRuleEngineVariableResponse;
import com.zjb.ruleplatform.entity.vo.ListRuleEngineVariableResponse;

/**
 * @author yuzhiji
 */
public interface RuleEngineVariableService {


    GetRuleEngineVariableResponse getVariable(Long variableId);


    PageResult<ListRuleEngineVariableResponse> listVariable(PageRequest<ListRuleEngineVariableRequest> pageRequest);

    GetRuleEngineVariableResponse addVariable(RuleEngineVariableRequest param);





}
