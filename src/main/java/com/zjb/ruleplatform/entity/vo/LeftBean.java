package com.zjb.ruleplatform.entity.vo;

import cn.hutool.core.collection.CollUtil;
import com.baomidou.mybatisplus.extension.api.R;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.zjb.ruleengine.core.value.Element;
import com.zjb.ruleplatform.entity.RuleEngineVariableParam;
import com.zjb.ruleplatform.manager.RuleEngineElementManager;
import com.zjb.ruleplatform.manager.RuleEngineVariableManager;
import com.zjb.ruleplatform.manager.RuleEngineVariableParamManager;
import lombok.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

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
    public Collection<Long> collectorElement() {
        if (Objects.equals(ELEMENT, valueType)) {
            return Sets.newHashSet(Long.valueOf(value));
        }
        //if (Objects.equals(VARIABLE, valueType)) {
        //    return recursionElementId(Long.valueOf(value));
        //}
        return Collections.emptySet();
    }

    @Override
    public Collection<Long> collectorVariable() {
        if (Objects.equals(VARIABLE, valueType)) {
            final Long varId = Long.valueOf(value);
            return Sets.newHashSet(varId);
        }
        return Collections.emptySet();
    }

    //private Collection<Long> recursionVariableId(Long varId) {
    //    final HashSet<Long> result = Sets.newHashSet(varId);
    //    final List<RuleEngineVariableParam> params = variableParamManager.lambdaQuery().eq(RuleEngineVariableParam::getVariableId, varId).list();
    //    if (CollUtil.isNotEmpty(params)) {
    //        params.stream()
    //                .filter(param -> Objects.equals(param.getFunctionParamValueType(), VARIABLE))
    //                .mapToLong(param -> Long.valueOf(param.getFunctionParamValue()))
    //                .forEach(id -> result.addAll(recursionVariableId(id)));
    //    }
    //    return result;
    //
    //}
    //
    //private Collection<Long> recursionElementId(Long varId) {
    //    final HashSet<Long> result = Sets.newHashSet();
    //    final List<RuleEngineVariableParam> params = variableParamManager.lambdaQuery().eq(RuleEngineVariableParam::getVariableId, varId).list();
    //    if (CollUtil.isNotEmpty(params)) {
    //        final Map<String, List<RuleEngineVariableParam>> paramMap = params.stream().collect(Collectors.groupingBy(RuleEngineVariableParam::getFunctionParamValueType));
    //        if (paramMap.containsKey(ELEMENT)) {
    //            final Set<Long> eleIds = paramMap.get(ELEMENT)
    //                    .stream()
    //                    .mapToLong(param -> Long.valueOf(param.getFunctionParamValue()))
    //                    .boxed()
    //                    .collect(Collectors.toSet());
    //            result.addAll(eleIds);
    //        }
    //        if (paramMap.containsKey(VARIABLE)) {
    //            paramMap.get(VARIABLE)
    //                    .stream()
    //                    .mapToLong(param -> Long.valueOf(param.getFunctionParamValue()))
    //                    .boxed()
    //                    .forEach(paramVarId -> result.addAll(recursionElementId(varId)));
    //        }
    //    }
    //    return result;
    //
    //}

}
