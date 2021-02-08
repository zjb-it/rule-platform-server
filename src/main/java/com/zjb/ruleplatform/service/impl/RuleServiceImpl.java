package com.zjb.ruleplatform.service.impl;

import cn.hutool.core.collection.CollUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.google.common.collect.Lists;
import com.zjb.ruleplatform.entity.RuleEngineConditionGroup;
import com.zjb.ruleplatform.entity.RuleEngineRule;
import com.zjb.ruleplatform.entity.common.PageRequest;
import com.zjb.ruleplatform.entity.common.PageResult;
import com.zjb.ruleplatform.entity.dto.AddRuleRequest;
import com.zjb.ruleplatform.entity.vo.LeftBean;
import com.zjb.ruleplatform.entity.vo.RuleDetail;
import com.zjb.ruleplatform.entity.vo.RuleInfo;
import com.zjb.ruleplatform.manager.RuleEngineConditionGroupManager;
import com.zjb.ruleplatform.manager.RuleEngineRuleManager;
import com.zjb.ruleplatform.mapper.CustomRuleMapper;
import com.zjb.ruleplatform.service.RuleService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author 赵静波 <zhaojingbo>
 * Created on 2021-01-30
 */
@Service
public class RuleServiceImpl implements RuleService {
    @Autowired
    private RuleEngineRuleManager ruleManager;
    @Autowired
    private RuleEngineConditionGroupManager conditionGroupManager;
    @Autowired
    private CustomRuleMapper ruleMapper;

    @Override
    public Long addRule(AddRuleRequest addRuleRequest) {
        final RuleEngineRule rule = convertToDbRule(addRuleRequest);
        ruleManager.save(rule);

        this.saveConditionGroups(addRuleRequest.getConditionGroups(), rule.getId());
        return rule.getId();

    }


    private RuleEngineRule convertToDbRule(AddRuleRequest addRuleRequest) {
        final RuleEngineRule rule = new RuleEngineRule();
        BeanUtils.copyProperties(addRuleRequest, rule);
        final LeftBean action = addRuleRequest.getAction();
        rule.setActionValueType(action.getValueType());
        rule.setActionValue(action.getValue());
        rule.setActionValueDataType(action.getValueDataType());
        rule.setCodeName(rule.getCode() + rule.getName());
        return rule;
    }

    private boolean saveConditionGroups(List<AddRuleRequest.ConditionGroup> conditionGroups, Long ruleId) {
        final ArrayList<RuleEngineConditionGroup> dbConditionGroups = Lists.newArrayList();
        final List<AddRuleRequest.ConditionGroup> requestConditionGroups = conditionGroups;

        for (int i = 0; i < requestConditionGroups.size(); i++) {
            final AddRuleRequest.ConditionGroup conditionGroup1 = requestConditionGroups.get(i);
            final List<Long> conditionIds = conditionGroup1.getConditionIds();
            for (int j = 0; j < conditionIds.size(); j++) {
                RuleEngineConditionGroup conditionGroup = new RuleEngineConditionGroup();
                conditionGroup.setRuleId(ruleId);
                conditionGroup.setConditionId(conditionIds.get(j));
                conditionGroup.setConditionOrder(j);
                conditionGroup.setConditionGroupOrder(conditionGroup1.getOrder());
                dbConditionGroups.add(conditionGroup);
            }
        }
        return conditionGroupManager.saveBatch(dbConditionGroups);
    }

    @Override
    public boolean updateRule(AddRuleRequest addRuleRequest) {
        ruleManager.updateById(convertToDbRule(addRuleRequest));
        removeConditionGroups(addRuleRequest.getId());
        saveConditionGroups(addRuleRequest.getConditionGroups(), addRuleRequest.getId());
        return false;
    }

    @Override
    public boolean delRule(Long id) {
        return ruleManager.removeById(id) && removeConditionGroups(id);
    }

    private Boolean removeConditionGroups(Long ruleId) {
        QueryWrapper<RuleEngineConditionGroup> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda().eq(RuleEngineConditionGroup::getRuleId, ruleId);
        return conditionGroupManager.remove(queryWrapper);
    }

    @Override
    public PageResult<RuleInfo> pageRule(PageRequest<String> pageRequest) {
        LambdaQueryWrapper<RuleEngineRule> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.orderByDesc(RuleEngineRule::getId);
        if (StringUtils.isNotBlank(pageRequest.getQuery())) {
            queryWrapper.like(RuleEngineRule::getCodeName, pageRequest.getQuery());
        }
        final Page<RuleEngineRule> page = ruleManager.page(new Page<>(pageRequest.getPage().getPageIndex(), pageRequest.getPage().getPageSize()), queryWrapper);
        final List<RuleEngineRule> records = page.getRecords();
        if (CollUtil.isEmpty(records)) {
            return new PageResult<>();
        }
        final List<RuleInfo> data = records.stream().map(record -> {
            RuleInfo ruleInfo = new RuleInfo();
            BeanUtils.copyProperties(record, ruleInfo);
            return ruleInfo;
        }).collect(Collectors.toList());
        final PageResult<RuleInfo> result = new PageResult<>();
        result.setTotal(page.getTotal());
        result.setData(data);
        return result;
    }

    @Override
    public RuleDetail getRule(Long id) {
        return ruleMapper.getRule(id);
    }
}
