package com.zjb.ruleplatform.entity.vo;

import com.zjb.ruleplatform.entity.dto.ConditionParam;
import com.zjb.ruleplatform.entity.dto.ConfigBean;
import lombok.Data;

import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author 赵静波 <zhaojingbo>
 * Created on 2021-02-07
 */
@Data
public class RuleDetail extends RuleInfo implements CollectorValue{

    private LeftBean action;

    private List<ConditionGroup> conditionGroups;

    @Override
    public Collection<Long> collectorElement() {
        final Collection<Long> actionElementIds = action.collectorElement();

        final Set<Long> collect = conditionGroups.stream().flatMap(group ->
             group.getConditions()
                    .stream()
                    .flatMap(param -> param.collectorElement().stream())
                    .collect(Collectors.toList())
                    .stream()
        ).collect(Collectors.toSet());
        collect.addAll(actionElementIds);
        return collect;
    }

    @Override
    public Collection<Long> collectorVariable() {
        final Collection<Long> actionElementIds = action.collectorVariable();

        final Set<Long> collect = conditionGroups.stream().flatMap(group ->
                group.getConditions()
                        .stream()
                        .flatMap(param -> param.collectorVariable().stream())
                        .collect(Collectors.toList())
                        .stream()
        ).collect(Collectors.toSet());
        collect.addAll(actionElementIds);
        return collect;
    }
}
