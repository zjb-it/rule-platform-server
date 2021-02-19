/**
 * *****************************************************
 * Copyright (C) 2019 zjb.com. All Rights Reserved
 * This file is part of zjb zjb project.
 * Unauthorized copy of this file, via any medium is strictly prohibited.
 * Proprietary and Confidential.
 * ****************************************************
 * <p>
 * History:
 * <author>            <time>          <version>          <desc>
 * 作者姓名           修改时间           版本号            描述
 */
package com.zjb.ruleplatform.entity.dto;

import com.google.common.collect.Sets;
import com.zjb.ruleplatform.entity.vo.CollectorValue;
import com.zjb.ruleplatform.manager.RuleEngineVariableParamManager;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.util.Collection;
import java.util.HashSet;

/**
 * @author 赵静波
 * @date 2021-01-14 15:54:11
 */
@NoArgsConstructor
@Data
public class ConditionParam implements CollectorValue {

    /**
     * name : 合同编号
     * description : 我是一个条件
     * config : {"left":{"type":2,"value_type":"STRING","value":1},"symbol":">","right":{"type":2,"value_type":"STRING","value":1}}
     */
    private Long id;
    @NotBlank(message = "条件名称不能为空")
    private String name;
    private String description;
    @NotNull(message = "条件配置不能为空")
    private ConfigBean config;

    @Override
    public Collection<Long> collectorElement(RuleEngineVariableParamManager variableParamManager) {
        final Collection<Long> left = config.getLeftVariable().collectorElement(variableParamManager);
        final Collection<Long> right = config.getRightVariable().collectorElement(variableParamManager);
        final HashSet<Long> longs = Sets.newHashSet(left);
        longs.addAll(right);
        return longs;
    }

    @Override
    public Collection<Long> collectorVariable(RuleEngineVariableParamManager variableParamManager) {
        final Collection<Long> left = config.getLeftVariable().collectorVariable(variableParamManager);
        final Collection<Long> right = config.getRightVariable().collectorVariable(variableParamManager);
        final HashSet<Long> longs = Sets.newHashSet(left);
        longs.addAll(right);
        return longs;
    }
}