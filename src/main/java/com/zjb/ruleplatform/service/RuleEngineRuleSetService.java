package com.zjb.ruleplatform.service;

import com.founder.ego.common.request.PageRequest;
import com.founder.ego.common.response.PageResult;
import com.founder.ego.vo.ruleengine.*;

/**
 * @author yuzhiji
 */
public interface RuleEngineRuleSetService {

    Boolean switches(RuleSetSwitchRequest ruleSetSwitchRequest);

    Boolean deleteRuleSet(CommonIdParam commonIdParam);

    /**
     * code验重接口
     *
     * @param commonCodeParam 规则、决策表code
     * @return false时可用
     */
    Boolean validateUniqCode(CommonCodeParam commonCodeParam);

    /**
     * name验重接口
     *
     * @param commonNameParam 规则、决策表name
     * @return false时可用
     */
    Boolean validateUniqName(CommonNameParam commonNameParam);

    PageResult<RuleSetResponse> listRuleSet(PageRequest<RuleQueryParam> pageRequest);

    RuleSetResult getRuleSetById(CommonIdParam commonIdParam);

    RuleDefResult getDefById(CommonIdParam commonIdParam);

    CommonIdResult addRuleSet(RuleSetEditRequest ruleSetEditRequest);

    Boolean updateRuleSet(RuleSetEditRequest ruleSetEditRequest);

    Boolean saveDraft(RuleSetSaveDraftRequest saveDraftRequest);

    /**
     * 获取规则草稿
     *
     * @param ruleSetId 规则id
     * @return data
     */
    RuleSetSaveDraftRequest getDraft(Long ruleSetId);

    /**
     * 获取规则集，如果草稿的update_Time >ruleSet的update_time,则返回草稿，否则反之
     *
     * @param ruleSetId
     * @return
     */
    RuleSetConfig getRuleSet(Long ruleSetId);


}
