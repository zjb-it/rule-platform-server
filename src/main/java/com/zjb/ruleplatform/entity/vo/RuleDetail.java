package com.zjb.ruleplatform.entity.vo;

import com.zjb.ruleplatform.manager.RuleEngineVariableParamManager;
import lombok.Data;

import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author 赵静波
 * @date 2021-02-07 15:40:21
 */
@Data
public class RuleDetail extends RuleInfo implements CollectorValue{

    private LeftBean action;

    private List<ConditionGroupDetail> conditionGroups;

    private Collection<Long> paramIds;

    @Override
    public Collection<Long> collectorElement(RuleEngineVariableParamManager variableParamManager) {
        final Collection<Long> actionElementIds = action.collectorElement(variableParamManager);

        final Set<Long> collect = conditionGroups.stream().flatMap(group ->
             group.getConditions()
                    .stream()
                    .flatMap(param -> param.collectorElement(variableParamManager).stream())
                    .collect(Collectors.toList())
                    .stream()
        ).collect(Collectors.toSet());
        collect.addAll(actionElementIds);
        return collect;
    }

    @Override
    public Collection<Long> collectorVariable(RuleEngineVariableParamManager variableParamManager) {
        final Collection<Long> actionElementIds = action.collectorVariable(variableParamManager);

        final Set<Long> collect = conditionGroups.stream().flatMap(group ->
                group.getConditions()
                        .stream()
                        .flatMap(param -> param.collectorVariable(variableParamManager).stream())
                        .collect(Collectors.toList())
                        .stream()
        ).collect(Collectors.toSet());
        collect.addAll(actionElementIds);
        return collect;
    }
}
