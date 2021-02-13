package com.zjb.ruleplatform.service;

import com.zjb.ruleplatform.entity.common.PageRequest;
import com.zjb.ruleplatform.entity.common.PageResult;
import com.zjb.ruleplatform.entity.dto.AddRuleRequest;
import com.zjb.ruleplatform.entity.dto.RuleExecuteParam;
import com.zjb.ruleplatform.entity.dto.RuleTest;
import com.zjb.ruleplatform.entity.vo.RuleDetail;
import com.zjb.ruleplatform.entity.vo.RuleInfo;

/**
 * @author 赵静波 <zhaojingbo>
 * Created on 2021-01-30
 */
public interface RuleExecuteService {

    Object execute(RuleExecuteParam param);



}
