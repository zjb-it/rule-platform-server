package com.zjb.ruleplatform.service.impl;

import com.zjb.ruleengine.core.BaseContextImpl;
import com.zjb.ruleengine.core.RuleEngine;
import com.zjb.ruleplatform.entity.dto.RuleExecuteParam;
import com.zjb.ruleplatform.service.RuleExecuteService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @author 赵静波
 * @date 2021-02-10 20:10:23
 */
@Service
public class RuleExecuteServiceImpl implements RuleExecuteService {

    @Autowired
    private RuleEngine ruleEngine;

    @Override
    public Object execute(RuleExecuteParam param) {
        final BaseContextImpl context = new BaseContextImpl();
        context.putAll(param.getRuleParam());

        return ruleEngine.execute(param.getRuleCode(), context);

    }
}
