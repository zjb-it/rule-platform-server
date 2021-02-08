package com.zjb.ruleplatform.service;

import com.zjb.ruleplatform.entity.common.PageRequest;
import com.zjb.ruleplatform.entity.common.PageResult;
import com.zjb.ruleplatform.entity.dto.AddRuleRequest;
import com.zjb.ruleplatform.entity.vo.RuleDetail;
import com.zjb.ruleplatform.entity.vo.RuleInfo;

/**
 * @author 赵静波 <zhaojingbo>
 * Created on 2021-01-30
 */
public interface RuleService {

    Long addRule(AddRuleRequest addRuleRequest);

    boolean updateRule(AddRuleRequest addRuleRequest);

    boolean delRule(Long id);

    PageResult<RuleInfo> pageRule(PageRequest<String> pageRequest);

    RuleDetail getRule(Long id);

}
