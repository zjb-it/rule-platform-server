package com.zjb.ruleplatform.service;

import com.founder.ego.common.request.PageRequest;
import com.founder.ego.common.response.PageResult;
import com.founder.ego.vo.ruleengine.*;

/**
 * @author yuzhiji
 */
public interface RuleEngineVariableService {

    Boolean deleteVariable(CommonIdParam commonIdParam);

    Boolean validateUniqName(CommonNameParam commonNameParam);

    GetRuleEngineVariableResponse getVariable(CommonIdParam commonIdParam);

    Boolean updateVariable(RuleEngineVariableRequest ruleEngineVariableRequest);

    PageResult<ListRuleEngineVariableResponse> listVariable(PageRequest<ListRuleEngineVariableRequest> pageRequest);

    GetRuleEngineVariableResponse addVariable(RuleEngineVariableRequest param);

    CountVo countVariable(Long variableId);

    RuleEngineVariableList getVariableList(CommonIdListParam commonIdListParam);


}
