package com.zjb.ruleplatform.service;

import com.founder.ego.vo.ruleengine.RulePublishRequest;

/**
 * 规则引擎加载规则集
 *
 * @author v-lixing.ea
 */
public interface RuleEnginePublishService {

    /**
     * 规则发布
     *
     * @param rulePublishRequest 规则id/版本/环境信息
     * @return true
     */
    Boolean publish(RulePublishRequest rulePublishRequest);
}
