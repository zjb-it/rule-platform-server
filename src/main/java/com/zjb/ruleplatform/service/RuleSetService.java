package com.zjb.ruleplatform.service;


import com.zjb.ruleplatform.entity.common.PageRequest;
import com.zjb.ruleplatform.entity.common.PageResult;
import com.zjb.ruleplatform.entity.dto.RuleSetDef;
import com.zjb.ruleplatform.entity.vo.RuleSetConfig;


public interface RuleSetService {


    Long addDef(RuleSetDef def);

    RuleSetDef getDef(Long id);

    Boolean updateDef(RuleSetDef def);


    Boolean deleteRuleSet(Long id);


    PageResult<RuleSetDef> pageRuleSet(PageRequest<String> pageRequest);

    //
    RuleSetConfig getRuleSetConfig(Long id);

    //
    Boolean addRuleSetConfig(RuleSetConfig ruleSetConfig);

    //
    Boolean updateRuleSetConfig(RuleSetConfig ruleSetConfig);
}
