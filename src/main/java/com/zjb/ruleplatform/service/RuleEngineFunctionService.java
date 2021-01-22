package com.zjb.ruleplatform.service;


import com.zjb.ruleplatform.entity.common.PageRequest;
import com.zjb.ruleplatform.entity.common.PageResult;
import com.zjb.ruleplatform.entity.vo.FunctionDetailVo;

/**
 * @author yuzhiji
 */
public interface RuleEngineFunctionService {

    PageResult<FunctionDetailVo> functionLookUp(PageRequest<String> pageRequest);
}
