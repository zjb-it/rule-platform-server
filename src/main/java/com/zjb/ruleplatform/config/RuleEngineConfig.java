package com.zjb.ruleplatform.config;

import com.zjb.ruleengine.core.DefaultRuleEngine;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

/**
 * @author 赵静波
 * Created on 2021-01-18
 */
@Component
public class RuleEngineConfig {
    @Bean
    public DefaultRuleEngine ruleEngine() {
        return new DefaultRuleEngine();
    }
}
