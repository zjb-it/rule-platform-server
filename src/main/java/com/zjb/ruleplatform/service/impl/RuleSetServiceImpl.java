package com.zjb.ruleplatform.service.impl;
import java.util.Date;

import cn.hutool.core.collection.CollUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.google.common.collect.Lists;
import com.zjb.ruleplatform.entity.RuleEngineRule;
import com.zjb.ruleplatform.entity.RuleEngineRuleSet;
import com.zjb.ruleplatform.entity.RuleEngineRuleSetRules;
import com.zjb.ruleplatform.entity.common.PageRequest;
import com.zjb.ruleplatform.entity.common.PageResult;
import com.zjb.ruleplatform.entity.dto.RuleSetDef;
import com.zjb.ruleplatform.entity.vo.RuleInfo;
import com.zjb.ruleplatform.entity.vo.RuleSetConfig;
import com.zjb.ruleplatform.manager.RuleEngineRuleManager;
import com.zjb.ruleplatform.manager.RuleEngineRuleSetManager;
import com.zjb.ruleplatform.manager.RuleEngineRuleSetRulesManager;
import com.zjb.ruleplatform.service.RuleSetService;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.ListUtils;
import org.apache.commons.collections4.SetUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * @author 赵静波
 * Created on 2021-03-07
 */
@Service
public class RuleSetServiceImpl implements RuleSetService {
    @Autowired
    private RuleEngineRuleSetManager ruleSetManager;
    @Autowired
    private RuleEngineRuleSetRulesManager ruleSetRulesManager;
    @Autowired
    private RuleEngineRuleManager ruleManager;

    @Override
    public Long addDef(RuleSetDef def) {
        final RuleEngineRuleSet ruleSet = new RuleEngineRuleSet();
        BeanUtils.copyProperties(def, ruleSet);
        ruleSetManager.save(ruleSet);
        return ruleSet.getId();
    }

    @Override
    public RuleSetDef getDef(Long id) {
        final RuleEngineRuleSet ruleSet = ruleSetManager.getById(id);
        RuleSetDef def = new RuleSetDef();
        BeanUtils.copyProperties(ruleSet, def);
        return def;
    }

    @Override
    public Boolean updateDef(RuleSetDef def) {
        return ruleSetManager.lambdaUpdate()
                .set(RuleEngineRuleSet::getName, def.getName())
                .set(RuleEngineRuleSet::getDescription, def.getDescription())
                .eq(RuleEngineRuleSet::getId, def.getId())
                .update();
    }

    @Override
    public Boolean deleteRuleSet(Long id) {
        return ruleSetManager.removeById(id);
    }

    @Override
    public PageResult<RuleSetDef> pageRuleSet(PageRequest<String> pageRequest) {

        final Page<RuleEngineRuleSet> page = ruleSetManager.lambdaQuery()
                .eq(RuleEngineRuleSet::getCodeName, pageRequest.getQuery())
                .page(new Page<>(pageRequest.getPage().getPageIndex(), pageRequest.getPage().getPageSize()));
        PageResult<RuleSetDef> result = new PageResult<>();
        result.setTotal(page.getTotal());
        final List<RuleEngineRuleSet> records = page.getRecords();
        if (CollectionUtils.isEmpty(records)) {
            return result;
        }
        final List<RuleSetDef> data = records.stream().map(ruleSet -> {
            RuleSetDef def = new RuleSetDef();
            BeanUtils.copyProperties(ruleSet, def);
            return def;
        }).collect(Collectors.toList());
        result.setData(data);
        return result;
    }

    @Override
    public RuleSetConfig getRuleSetConfig(Long id) {
        final RuleEngineRuleSet ruleSet = ruleSetManager.getById(id);
        final RuleSetConfig result = new RuleSetConfig();
        if (ruleSet == null) {
            return result;
        }
        final List<RuleEngineRuleSetRules> normalRules = ruleSetRulesManager.lambdaQuery().eq(RuleEngineRuleSetRules::getRuleSetId, id).list();
        Set<Long> ruleIds = SetUtils.hashSet();
        if (CollectionUtils.isNotEmpty(normalRules)) {
            ruleIds.addAll(normalRules.stream().map(RuleEngineRuleSetRules::getRuleId).collect(Collectors.toList()));
        }
        if (Objects.nonNull(ruleSet.getDefaultRuleId())) {
            ruleIds.add(ruleSet.getDefaultRuleId());
        }
        if (CollectionUtils.isEmpty(ruleIds)) {
            return result;
        }
        final List<RuleEngineRule> rules = ruleManager.listByIds(ruleIds);
        if (CollectionUtils.isEmpty(rules)) {
            return result;
        }
        final Map<Long, RuleInfo> collect = rules.stream().map(record -> {
            RuleInfo ruleInfo = new RuleInfo();
            BeanUtils.copyProperties(record, ruleInfo);
            return ruleInfo;
        }).collect(Collectors.toMap(RuleInfo::getId, Function.identity()));

        result.setRuleSetId(id);
        result.setDefaultRule(collect.remove(ruleSet.getDefaultRuleId()));
        //保证顺序
        final List<RuleInfo> ruleInfos = normalRules.stream()
                .sorted(Comparator.comparing(RuleEngineRuleSetRules::getRuleOrderNo))
                .map(rule -> collect.get(rule.getRuleId()))
                .collect(Collectors.toList());

        result.setRules(ruleInfos);
        return null;
    }

    @Override
    public Boolean addRuleSetConfig(RuleSetConfig ruleSetConfig) {
        updateDefaultRule(ruleSetConfig);
        addRuleSetNormalRules(ruleSetConfig);
        return Boolean.TRUE;
    }

    private void addRuleSetNormalRules(RuleSetConfig ruleSetConfig) {
        if (CollectionUtils.isNotEmpty(ruleSetConfig.getRules())) {
            final List<RuleInfo> rules = ruleSetConfig.getRules();
            final ArrayList<RuleEngineRuleSetRules> normalRules = Lists.newArrayListWithCapacity(rules.size());
            for (int i = 0; i < rules.size(); i++) {
                RuleEngineRuleSetRules normalRule = new RuleEngineRuleSetRules();
                normalRule.setRuleSetId(ruleSetConfig.getRuleSetId());
                normalRule.setRuleId(rules.get(i).getId());
                normalRule.setRuleOrderNo((long) i);
                normalRules.add(normalRule);
            }
            ruleSetRulesManager.saveBatch(normalRules);
        }
    }

    private void updateDefaultRule(RuleSetConfig ruleSetConfig) {
        if (ruleSetConfig.getDefaultRule() != null) {
            ruleSetManager.lambdaUpdate()
                    .set(RuleEngineRuleSet::getDefaultRuleId, ruleSetConfig.getDefaultRule())
                    .eq(RuleEngineRuleSet::getId, ruleSetConfig.getRuleSetId())
                    .update();
        }
    }

    @Override
    public Boolean updateRuleSetConfig(RuleSetConfig ruleSetConfig) {
        updateDefaultRule(ruleSetConfig);
        ruleSetRulesManager.remove(new LambdaQueryWrapper<RuleEngineRuleSetRules>().eq(RuleEngineRuleSetRules::getRuleSetId, ruleSetConfig.getRuleSetId()));
        addRuleSetNormalRules(ruleSetConfig);
        return Boolean.TRUE;
    }
}
