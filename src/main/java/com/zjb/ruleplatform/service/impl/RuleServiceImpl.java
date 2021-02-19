package com.zjb.ruleplatform.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.text.StrFormatter;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Lists;
import com.zjb.ruleengine.core.BaseContextImpl;
import com.zjb.ruleengine.core.RuleEngine;
import com.zjb.ruleengine.core.condition.AbstractCondition;
import com.zjb.ruleengine.core.condition.ConditionSet;
import com.zjb.ruleengine.core.condition.DefaultCondition;
import com.zjb.ruleengine.core.config.FunctionHolder;
import com.zjb.ruleengine.core.enums.DataTypeEnum;
import com.zjb.ruleengine.core.enums.Symbol;
import com.zjb.ruleengine.core.exception.RuleCompileException;
import com.zjb.ruleengine.core.rule.AbstractRule;
import com.zjb.ruleengine.core.rule.Rule;
import com.zjb.ruleengine.core.value.*;
import com.zjb.ruleplatform.entity.*;
import com.zjb.ruleplatform.entity.common.PageRequest;
import com.zjb.ruleplatform.entity.common.PageResult;
import com.zjb.ruleplatform.entity.dto.AddRuleRequest;
import com.zjb.ruleplatform.entity.dto.ConditionParam;
import com.zjb.ruleplatform.entity.dto.RuleTest;
import com.zjb.ruleplatform.entity.vo.ConditionGroupDetail;
import com.zjb.ruleplatform.entity.vo.LeftBean;
import com.zjb.ruleplatform.entity.vo.RuleDetail;
import com.zjb.ruleplatform.entity.vo.RuleInfo;
import com.zjb.ruleplatform.enums.RuleStatusEnum;
import com.zjb.ruleplatform.manager.*;
import com.zjb.ruleplatform.mapper.CustomRuleMapper;
import com.zjb.ruleplatform.service.RuleService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.validation.ValidationException;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.zjb.ruleplatform.entity.vo.LeftBean.ELEMENT;
import static com.zjb.ruleplatform.entity.vo.LeftBean.VARIABLE;

/**
 * @author 赵静波
 * @date 2021-01-30 23:16:24
 */
@Service
@Slf4j
public class RuleServiceImpl implements RuleService {
    @Autowired
    private RuleEngineRuleManager ruleManager;
    @Autowired
    private RuleEngineConditionGroupManager conditionGroupManager;
    @Autowired
    private CustomRuleMapper ruleMapper;
    @Autowired
    private RuleEngine ruleEngine;
    @Autowired
    private RuleEngineElementManager elementManager;
    @Autowired
    private RuleEngineVariableManager variableManager;
    @Autowired
    private RuleEngineVariableParamManager variableParamManager;
    @Autowired
    private FunctionHolder functionHolder;

    @Override
    public Long addRule(AddRuleRequest addRuleRequest) {
        final RuleEngineRule rule = convertToDbRule(addRuleRequest);
        rule.setStatus(RuleStatusEnum.unpublish.status);
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
    public Long updateRule(AddRuleRequest addRuleRequest) {
        final RuleEngineRule ruleEngineRule = convertToDbRule(addRuleRequest);
        ruleEngineRule.setStatus(RuleStatusEnum.unpublish.status);
        ruleManager.updateById(ruleEngineRule);
        removeConditionGroups(addRuleRequest.getId());
        saveConditionGroups(addRuleRequest.getConditionGroups(), addRuleRequest.getId());
        return addRuleRequest.getId();
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
        final RuleDetail rule = ruleMapper.getRule(id);
        rule.setParamIds(rule.collectorElement(variableParamManager));
        return rule ;
    }


    @Override
    public Object testRule(RuleTest ruleTest) {
        final RuleDetail rule = ruleMapper.getRule(ruleTest.getRuleId());
        loadRule(rule);

        final AbstractRule engineRule = ruleEngine.getRule(rule.getCode());
        final Collection<Element> elements = engineRule.collectParameter();
        final Map<String, Element> elementMap = elements.stream().collect(Collectors.toMap(Element::getCode, Function.identity()));
        BaseContextImpl context = new BaseContextImpl();
        ruleTest.getRuleParam().forEach((k,v)->{
            if (elementMap.containsKey(k)) {
                final Element element = elementMap.get(k);
                final Class eleClazz = element.getDataTypeEnum().getClazz();
                if (v.getClass().isAssignableFrom(eleClazz)) {
                    context.put(k, v);
                } else {
                    final ObjectMapper objectMapper = new ObjectMapper();
                    try {
                        context.put(k, objectMapper.readValue(objectMapper.writeValueAsString(v),eleClazz));
                    } catch (JsonProcessingException e) {
                        throw new ValidationException(StrFormatter.format("{}数据类型错误,不能转换为{}",element.getCode(),eleClazz));
                    }
                }
            } else {
                context.put(k, v);
            }
        });

        final Object result = ruleEngine.execute(rule.getCode(), context);
        ruleEngine.removeRule(rule.getCode());
        return result;
    }

    @Override
    public Boolean publish(Long ruleId) {
        ruleManager.lambdaUpdate()
                .set(RuleEngineRule::getStatus, RuleStatusEnum.published.status)
                .eq(RuleEngineRule::getId, ruleId)
                .update();

        final RuleDetail rule = ruleMapper.getRule(ruleId);
        loadRule(rule);
        return Boolean.TRUE;
    }

    private void loadRule(RuleDetail rule) {
        final Collection<Long> eleIds = rule.collectorElement(variableParamManager);
        final Map<Long, RuleEngineElement> elementMap = getElementMap(eleIds);
        final Collection<Long> varIds = rule.collectorVariable(variableParamManager);
        final Map<Long, RuleEngineVariable> varMap = getVarMap(varIds);
        final Map<Long, List<RuleEngineVariableParam>> varParamMap = getVarParamMap(varIds);

        Value action = getValue(rule.getAction(), elementMap, varMap, varParamMap);

        AbstractCondition condition = getConditonSet(rule.getConditionGroups(), elementMap, varMap, varParamMap);
        ruleEngine.addRule(new Rule(rule.getCode(), condition, action));
    }

    private Map<Long, RuleEngineElement> getElementMap(Collection<Long> eleIds) {
        Map<Long, RuleEngineElement> elementMap = Collections.EMPTY_MAP;

        if (CollUtil.isNotEmpty(eleIds)) {
            elementMap = elementManager.lambdaQuery()
                    .in(RuleEngineElement::getId, eleIds)
                    .list()
                    .stream()
                    .collect(Collectors.toMap(RuleEngineElement::getId, Function.identity()));
        }
        return elementMap;
    }

    private Map<Long, RuleEngineVariable> getVarMap(Collection<Long> varIds) {
        Map<Long, RuleEngineVariable> varMap = Collections.EMPTY_MAP;
        if (CollUtil.isNotEmpty(varIds)) {
            varMap = variableManager.lambdaQuery()
                    .in(RuleEngineVariable::getId, varIds)
                    .list()
                    .stream()
                    .collect(Collectors.toMap(RuleEngineVariable::getId, Function.identity()));
        }
        return varMap;
    }

    private Map<Long, List<RuleEngineVariableParam>> getVarParamMap(Collection<Long> varIds) {
        Map<Long, List<RuleEngineVariableParam>> varParamMap = Collections.EMPTY_MAP;
        if (CollUtil.isNotEmpty(varIds)) {
            varParamMap = variableParamManager.lambdaQuery()
                    .in(RuleEngineVariableParam::getVariableId, varIds)
                    .list()
                    .stream()
                    .collect(Collectors.groupingBy(RuleEngineVariableParam::getVariableId));

        }
        return varParamMap;
    }

    private AbstractCondition getConditonSet(List<ConditionGroupDetail> conditionGroups, Map<Long, RuleEngineElement> elementMap, Map<Long, RuleEngineVariable> varMap, Map<Long, List<RuleEngineVariableParam>> varParamMap) {
        final List<com.zjb.ruleengine.core.condition.ConditionGroup> engineCongtionGroups = conditionGroups
                .stream()
                .sorted(Comparator.comparing(ConditionGroupDetail::getOrder))
                .map(group -> {
                    final List<DefaultCondition> defaultConditions = group.getConditions().stream()
                            .map(conditionParam -> getDefaultCondition(conditionParam, elementMap, varMap, varParamMap)).collect(Collectors.toList());
                    return new com.zjb.ruleengine.core.condition.ConditionGroup(defaultConditions);
                }).collect(Collectors.toList());

        return new ConditionSet(engineCongtionGroups);
    }

    private Value getValue(LeftBean action, Map<Long, RuleEngineElement> elementMap, Map<Long, RuleEngineVariable> varMap, Map<Long, List<RuleEngineVariableParam>> varParamMap) {
        final String valueType = action.getValueType();

        if (Objects.equals(valueType, ELEMENT)) {
            final Long eleId = Long.valueOf(action.getValue());
            return new Element(DataTypeEnum.valueOf(action.getValueDataType()), elementMap.get(eleId).getCode());

        }
        if (Objects.equals(valueType, VARIABLE)) {
            final Long varId = Long.valueOf(action.getValue());
            RuleEngineVariable dbVariable = varMap.get(varId);
            //if (dbVariable == null) {
            //    dbVariable = variableManager.getById(varId);
            //}
            Map<String, Value> params = Collections.EMPTY_MAP;
            List<RuleEngineVariableParam> variableParams;
            //if (varParamMap.containsKey(varId)) {
                variableParams = varParamMap.get(varId);
            //} else {
            //    variableParams = variableParamManager.lambdaQuery().eq(RuleEngineVariableParam::getVariableId, varId).list();
            //}
            if (CollUtil.isNotEmpty(variableParams)) {
                params = variableParams
                        .stream()
                        .collect(Collectors.toMap(RuleEngineVariableParam::getFunctionParamName, param -> getValue(new LeftBean(param.getFunctionParamValueDataType(), param.getFunctionParamValue(), param.getFunctionParamValueDescription(), param.getFunctionParamValueType()), elementMap, varMap, varParamMap)));
            }
            final DataTypeEnum dataTypeByName = DataTypeEnum.getDataTypeByName(dbVariable.getValueDataType());
            return new Variable(dataTypeByName,new VariableFunction(dbVariable.getFunctionName(), params, functionHolder));
        }

        final DataTypeEnum dataTypeEnum = DataTypeEnum.valueOf(action.getValueType());
        //不为空，则为固定值
        if (dataTypeEnum != null) {
            switch (dataTypeEnum) {
                case STRING:
                    return new Constant(dataTypeEnum, action.getValue());
                case NUMBER:
                    return new Constant(dataTypeEnum, NumberUtils.createNumber(action.getValue()));
                case POJO:
                    //todo 只支持系统内pojo
                    throw new UnsupportedOperationException("只支持系统内pojo,未开放");
                case BOOLEAN:
                    return new Constant(dataTypeEnum, Boolean.valueOf(action.getValue()));
                case COLLECTION:
                    try {
                        final Collection collection = new ObjectMapper().readValue(action.getValue(), Collection.class);
                        return new Constant(dataTypeEnum, collection);
                    } catch (JsonProcessingException e) {
                        log.error("{}", e);
                        throw new RuleCompileException(e);
                    }
                case JSONOBJECT:
                    final JsonNode jsonNode;
                    try {
                        jsonNode = new ObjectMapper().readTree(action.getValue());
                        return new Constant(dataTypeEnum, jsonNode);
                    } catch (JsonProcessingException e) {
                        log.error("{}", e);
                        throw new RuleCompileException(e);
                    }

            }


        }


        return null;
    }

    private DefaultCondition getDefaultCondition(ConditionParam conditionParam, Map<Long, RuleEngineElement> elementMap, Map<Long, RuleEngineVariable> varMap, Map<Long, List<RuleEngineVariableParam>> varParamMap) {
        final Value left = getValue(conditionParam.getConfig().getLeftVariable(), elementMap, varMap, varParamMap);
        final Value right = getValue(conditionParam.getConfig().getRightVariable(), elementMap, varMap, varParamMap);
        final Symbol symbol = getSymbol(conditionParam.getConfig().getSymbol(), conditionParam.getConfig().getLeftVariable().getValueDataType());
        return new DefaultCondition(conditionParam.getId() + conditionParam.getName(), left, symbol, right);
    }

    private Symbol getSymbol(String symbol, String leftValueDataType) {

        final DataTypeEnum byName = DataTypeEnum.valueOf(leftValueDataType);
        if (byName == null) {
            throw new ValidationException(String.format("%s类型不存在", leftValueDataType));
        }
        return Symbol.getSymbolByDataType(byName, symbol);
    }


}
