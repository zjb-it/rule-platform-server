package com.zjb.ruleplatform.entity.vo;

import cn.hutool.core.collection.CollUtil;
import com.google.common.collect.Sets;
import com.zjb.ruleplatform.entity.RuleEngineVariableParam;
import com.zjb.ruleplatform.manager.RuleEngineVariableParamManager;
import lombok.*;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @author 赵静波
 * @date 2021-02-07 17:10:33
 */
@Data
public class LeftBean implements CollectorValue {
    /**
     * type : 0
     * value : 1
     * value_name : 1
     */
    public static final String ELEMENT = "ELEMENT";
    public static final String VARIABLE = "VARIABLE";

    private String valueDataType;
    private String value;
    private String valueName;
    private String valueType;

    public LeftBean() {
    }

    public LeftBean(String valueDataType, String value, String valueName, String valueType) {
        this.valueDataType = valueDataType;
        this.value = value;
        this.valueName = valueName;
        this.valueType = valueType;
    }

    @Override
    public Collection<Long> collectorElement(RuleEngineVariableParamManager variableParamManager) {
        if (Objects.equals(ELEMENT, valueType)) {
            return Sets.newHashSet(Long.valueOf(value));
        }
        if (Objects.equals(VARIABLE, valueType)) {
            return recursionElementId(Long.valueOf(value),variableParamManager);
        }
        return Collections.emptySet();
    }

    @Override
    public Collection<Long> collectorVariable(RuleEngineVariableParamManager variableParamManager) {
        if (Objects.equals(VARIABLE, valueType)) {
            final Long varId = Long.valueOf(value);
            return recursionVariableId(varId,variableParamManager);
        }
        return Collections.emptySet();
    }

    private Collection<Long> recursionVariableId(Long varId,RuleEngineVariableParamManager variableParamManager) {
        final HashSet<Long> result = Sets.newHashSet(varId);
        final List<RuleEngineVariableParam> params = variableParamManager.lambdaQuery().eq(RuleEngineVariableParam::getVariableId, varId).list();
        if (CollUtil.isNotEmpty(params)) {
            params.stream()
                    .filter(param -> Objects.equals(param.getFunctionParamValueType(), VARIABLE))
                    .mapToLong(param -> Long.valueOf(param.getFunctionParamValue()))
                    .forEach(id -> result.addAll(recursionVariableId(id,variableParamManager)));
        }
        return result;

    }

    private Collection<Long> recursionElementId(Long varId,RuleEngineVariableParamManager variableParamManager) {
        final HashSet<Long> result = Sets.newHashSet();
        final List<RuleEngineVariableParam> params = variableParamManager.lambdaQuery().eq(RuleEngineVariableParam::getVariableId, varId).list();
        if (CollUtil.isNotEmpty(params)) {
            final Map<String, List<RuleEngineVariableParam>> paramMap = params.stream().collect(Collectors.groupingBy(RuleEngineVariableParam::getFunctionParamValueType));
            if (paramMap.containsKey(ELEMENT)) {
                final Set<Long> eleIds = paramMap.get(ELEMENT)
                        .stream()
                        .mapToLong(param -> Long.valueOf(param.getFunctionParamValue()))
                        .boxed()
                        .collect(Collectors.toSet());
                result.addAll(eleIds);
            }
            if (paramMap.containsKey(VARIABLE)) {
                paramMap.get(VARIABLE)
                        .stream()
                        .mapToLong(param -> Long.valueOf(param.getFunctionParamValue()))
                        .boxed()
                        .forEach(paramVarId -> result.addAll(recursionElementId(paramVarId,variableParamManager)));
            }
        }
        return result;

    }

}
