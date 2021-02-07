package com.zjb.ruleplatform.service;

import com.zjb.ruleplatform.entity.common.PageRequest;
import com.zjb.ruleplatform.entity.common.PageResult;
import com.zjb.ruleplatform.entity.dto.AddRuleRequest;
import com.zjb.ruleplatform.entity.vo.RuleInfo;

/**
 * @author 赵静波 <wb_zhaojingbo@kuaishou.com>
 * Created on 2021-01-30
 */
public interface RuleService {

    boolean addRule(AddRuleRequest addRuleRequest);

    boolean updateRule(AddRuleRequest addRuleRequest);

    boolean delRule(Long id);

    PageResult<RuleInfo> pageRule(PageRequest<String> pageRequest);
}
