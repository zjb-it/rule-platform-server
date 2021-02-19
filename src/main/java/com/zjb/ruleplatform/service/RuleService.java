package com.zjb.ruleplatform.service;

import com.zjb.ruleplatform.entity.common.PageRequest;
import com.zjb.ruleplatform.entity.common.PageResult;
import com.zjb.ruleplatform.entity.dto.AddRuleRequest;
import com.zjb.ruleplatform.entity.dto.RuleTest;
import com.zjb.ruleplatform.entity.vo.RuleDetail;
import com.zjb.ruleplatform.entity.vo.RuleInfo;

/**
 * @author 赵静波
 * @date 2021-01-30 23:07:25
 */
public interface RuleService {

    Long addRule(AddRuleRequest addRuleRequest);

    Long updateRule(AddRuleRequest addRuleRequest);

    boolean delRule(Long id);

    PageResult<RuleInfo> pageRule(PageRequest<String> pageRequest);

    RuleDetail getRule(Long id);

    Object testRule(RuleTest ruleTest);

    Boolean publish(Long ruleId);

}
