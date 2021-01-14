package com.zjb.ruleplatform.service;

import com.founder.ego.common.request.PageRequest;
import com.founder.ego.common.response.PageResult;
import com.founder.ego.vo.ruleengine.CommonFunctionParam;
import com.founder.ego.vo.ruleengine.RuleEngineFunctionResponse;

/**
 * @author yuzhiji
 */
public interface RuleEngineFunctionService {

    PageResult<RuleEngineFunctionResponse> functionLookUp(PageRequest<CommonFunctionParam> pageRequest);
}
