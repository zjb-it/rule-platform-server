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
package com.zjb.ruleplatform.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.lang.Validator;
import cn.hutool.core.util.NumberUtil;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.StringPool;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.founder.ego.common.pojo.ManageUser;
import com.founder.ego.config.WfcRuleContext;
import com.founder.ego.enumbean.DeletedEnum;
import com.founder.ego.enumbean.PublishEnum;
import com.founder.ego.enumbean.RuleEnvironmentEnum;
import com.founder.ego.exception.ValidException;
import com.founder.ego.ruleengine.core.condition.ConditionGroup;
import com.founder.ego.ruleengine.core.condition.ConditionSet;
import com.founder.ego.ruleengine.core.enums.DataType;
import com.founder.ego.ruleengine.core.enums.NodeRepetitionEnum;
import com.founder.ego.ruleengine.core.enums.StatusEnum;
import com.founder.ego.ruleengine.core.enums.Symbol;
import com.founder.ego.ruleengine.core.rule.NormalRuleSet;
import com.founder.ego.ruleengine.core.rule.Rule;
import com.founder.ego.ruleengine.core.rule.RuleSet;
import com.founder.ego.ruleengine.core.value.Element;
import com.founder.ego.ruleengine.core.value.Variable;
import com.founder.ego.service.ruleengine.RuleEngineRuleSetService;
import com.founder.ego.service.ruleengine.RuleEngineRuleSetServiceV2;
import com.founder.ego.service.ruleengine.RuleLockService;
import com.founder.ego.store.bpm.entity.*;
import com.founder.ego.store.bpm.manager.*;
import com.founder.ego.store.bpm.mapper.RuleEngineEnviromentPublishHistoryMapper;
import com.founder.ego.store.bpm.mapper.RuleEngineRuleSetMapper;
import com.founder.ego.thirdapi.EmployeeService;
import com.founder.ego.utils.DB;
import com.founder.ego.utils.check.Check;
import com.founder.ego.vo.EmployeeResponse;
import com.founder.ego.vo.EnvVersion;
import com.founder.ego.vo.IdRequest;
import com.founder.ego.vo.ruleengine.*;
import lombok.val;
import org.springframework.cglib.proxy.Enhancer;
import org.springframework.cglib.proxy.MethodInterceptor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import javax.validation.ValidationException;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.founder.ego.utils.LambdaUtils.getKey;

/**
 * 〈一句话功能简述〉<br>
 * 〈〉
 *
 * @author v-dingqianwen.ea
 * @create 2019/9/29
 * @since 1.0.0
 */
@Service
public class RuleEngineRuleSetServiceV2Impl implements RuleEngineRuleSetServiceV2 {

    @Resource
    private EmployeeService employeeService;
    @Resource
    private RuleEngineRuleSetJsonManager ruleEngineRuleSetJsonManager;
    @Resource
    private RuleEngineRuleSetMapper ruleEngineRuleSetMapper;
    @Resource
    private RuleEngineRuleSetManager ruleEngineRuleSetManager;
    @Resource
    private RuleEngineRuleSetService ruleEngineRuleSetService;
    @Resource
    private RuleEngineElementManager ruleEngineElementManager;
    @Resource
    private RuleEngineRuleManager ruleEngineRuleManager;
    @Resource
    private RuleEngineRuleSetTestCaseManager ruleEngineRuleSetTestCaseManager;
    @Resource
    private RuleEngineRuleSetFrontTempManager ruleEngineRuleSetFrontTempManager;
    @Resource
    private RuleEngineEnviromentPublishHistoryMapper ruleEngineEnviromentPublishHistoryMap;
    @Resource
    private RuleLockService ruleLockService;

    /**
     * ----------------------------------RuleSet-------------------------------
     */
    static final String RULE_CODE = getKey(RuleSet::getRuleCode);
    static final String NAME = getKey(RuleSet::getName);
    static final String DESCRIPTION = getKey(RuleSet::getDescription);
    static final String HIT_POLICY = getKey(NormalRuleSet::getHitPolicy);
    static final String ENABLE_DEFAULT_RULE = getKey(RuleSet::getEnableDefaultRule);
    static final String NODE_REPETITION_POLICY = getKey(RuleSet::getNodeRepetitionPolicy);
    static final String PROCESS_REPETITION_POLICY = getKey(RuleSet::getProcessRepetitionPolicy);
    static final String NODE_ID_LIST = getKey(RuleSet::getNodeIdList);
    static final String RULES = getKey(RuleSet::getRules);
    static final String SPECIAL_RULES = getKey(NormalRuleSet::getSpecialRules);
    static final String DEFAULT_RULE = getKey(RuleSet::getDefaultRule);
    /**
     * ----------------------------------Rule-------------------------------
     */
    static final String ACTION_VARIABLE = getKey(Rule::getActionVariable);
    static final String RULE_ID = getKey(Rule::getRuleId);
    static final String ORDER_NO = getKey(Rule::getOrderNo);
    static final String CONDITION_SET = getKey(Rule::getConditionSet);
    /**
     * ----------------------------------Other-------------------------------
     */
    static final String DATA_TYPE = "dataType";
    static final String VARIABLE_NAME = "variableName";
    static final String OPERATOR_TYPE = "operatorType";
    static final String PARAMETER_LIST = "parameterList";
    private static final String ASSIGN_PERSON_ = "assign_person";
    private static final String ASSIGNED_PERSON_ = "assigned_person";
    private static final String ASSIGN_WAY_ = "assign_way";
    static final String VALUE = "value";
    private static final String PUBLISHED = "published";
    private static final String ASSIGN_AMOUNT_ = "assign_amount";
    private static final String AMOUNT = "amount";
    private static final String VERSIONS = "versions";
    private static final String CURRENT_VERSION_ = "current_version";
    private static final String PUBLISH_STATUS_ = "publish_status";
    private static final String CODE = "code";
    private static final String HIT_POLICY_ = "hit_policy";
    private static final String NODE_REPETITION_POLICY_ = "node_repetition_policy";
    private static final String DEFAULT_RULE_POLICY_ = "default_rule_policy";
    private static final String PROCESS_REPETITION_POLICY_ = "process_repetition_policy";
    private static final String PROCESS_REPETITION_NODE_NAME_ = "process_repetition_node_name";
    static final String PARAMETERS = "parameters";
    private static final String RANDOM_RESULT = "randomResult";
    private static final String COUNT = "count";
    private static final String ENABLE = "enable";
    private static final String NORMAL_RULE_ORDER_ = "normal_rule_order";
    private static final String CONDITION_SET_ = "condition_set";
    private static final String NORMAL_RULES_ = "normal_rules";
    private static final String SPECIAL_RULE_ORDER_ = "special_rule_order";
    private static final String SPECIAL_POLICY_ = "special_policy";
    private static final String SPECIAL_RULE_ = "special_rule";
    private static final String CONDITION_GROUP_ORDER_ = "condition_group_order";
    private static final String CONDITION_ORDER_ = "condition_order";
    private static final String SYMBOL = "symbol";
    private static final String LEFT_VARIABLE_ = "left_variable";
    private static final String RIGHT_VARIABLE_ = "right_variable";
    private static final String VALUE_TYPE_ = "value_type";
    private static final String VALUE_NAME_ = "value_name";
    private static final String AVATAR_URL = "avatarUrl";
    private static final String ACTION_VARIABLE_ = "action_variable";
    private static final String DEFAULT_RULE_ = "default_rule";
    private static final String UN_PUBLISH = "unPublish";
    private static final String TEST_PRE_PRD = "test,pre,prd";

    /**
     * ----------------------------------前置条件集-------------------------------
     */
    private static final String PRE_CONDITION_SET = "preConditionSet";
    private static final String PRE_CONDITION_SET_ = "pre_condition_set";
    private static final String POLICY = "policy";
    private static final String ORDER = "order";
    private static final String SCOPE = "scope";
    private static final String SET = "set";
    private static final String AUTHORIZATION = "authorization";

    private static final String PRE_CONDITION_SET_NAME = "preConditionSetName";
    private static final String ID = "id";
    private static final String LIST = "list";
    private static final String ZH = "zh";
    private static final String AVATAR = "avatar";
    /**
     * ----------------------------------Element-------------------------------
     */
    static final String ELEMENT_ID = getKey(Element::getElementId);
    /**
     * ----------------------------------Variable-------------------------------
     */
    static final String VARIABLE_ID = getKey(Variable::getVariableId);
    /**
     * ----------------------------------ConditionSet-------------------------------
     */
    static final String CONDITION_GROUPS = getKey(ConditionSet::getConditionGroups);
    /**
     * ----------------------------------ConditionGroup-------------------------------
     */
    static final String CONDITION_GROUP_ORDER = getKey(ConditionGroup::getConditionGroupOrder);
    static final String CONDITIONS = getKey(ConditionGroup::getConditions);
    /**
     * ----------------------------------ConditionSetBean.ConditionsBean-------------------------------
     */
    static final String CONDITION_ORDER = getKey(ConditionSetBean.ConditionsBean::getConditionOrder);
    static final String CONDITION_ID = getKey(ConditionSetBean.ConditionsBean::getConditionId);
    static final String CONDITION_NAME = getKey(ConditionSetBean.ConditionsBean::getConditionName);
    /**
     * ----------------------------------ConfigBean-------------------------------
     */
    static final String LEFT_VARIABLE = getKey(ConfigBean::getLeftVariable);
    static final String RIGHT_VARIABLE = getKey(ConfigBean::getRightVariable);

    /**
     * 规则预览页面-规则撤销
     * 撤销待发布的版本
     *
     * @param ruleSetCancelRequest ruleSetCancelRequest
     * @return true时撤销成功
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean cancel(RuleSetCancelRequest ruleSetCancelRequest) {
        ruleLockService.ruleSetValid(ruleSetCancelRequest.getId());
        //先查询到撤销的版本，方便删除此版本对应的测试用例
        RuleEngineRuleSetJson jsonManagerOne = ruleEngineRuleSetJsonManager.lambdaQuery()
                .eq(RuleEngineRuleSetJson::getRuleSetId, ruleSetCancelRequest.getId())
                .eq(RuleEngineRuleSetJson::getPublished, PublishEnum.WAITING_PUBLISH.getType())
                .eq(RuleEngineRuleSetJson::getDeleted, DeletedEnum.ENABLE.getStatus()).one();
        if (jsonManagerOne == null) {
            throw new ValidationException("撤销失败,没有查询到待发布版本");
        }
        //删除待发布版本
        ruleEngineRuleSetJsonManager.removeById(jsonManagerOne.getId());
        //撤销后，把撤销的版本的测试用例删除
        ruleEngineRuleSetTestCaseManager.lambdaUpdate()
                .eq(RuleEngineRuleSetTestCase::getRuleVersion, jsonManagerOne.getRuleVersion())
                .eq(RuleEngineRuleSetTestCase::getRuleSetId, jsonManagerOne.getRuleSetId()).remove();
        //查询已发布json
        List<RuleEngineRuleSetJson> ruleEngineRuleSetJsons = ruleEngineRuleSetJsonManager.lambdaQuery()
                .eq(RuleEngineRuleSetJson::getRuleSetId, ruleSetCancelRequest.getId())
                .eq(RuleEngineRuleSetJson::getIsCurEnv, RuleEnvironmentEnum.CURRENT_ENV.getValue())
                //需要回退到的版本
                .eq(RuleEngineRuleSetJson::getRuleVersion, ruleSetCancelRequest.getVersion())
                .eq(RuleEngineRuleSetJson::getDeleted, DeletedEnum.ENABLE.getStatus()).list();
        if (CollUtil.isEmpty(ruleEngineRuleSetJsons)) {
            throw new ValidationException("没有找到撤销的版本JSON数据");
        }
        //获取一个版本数据
        RuleEngineRuleSetJson ruleEngineRuleSetJson = ruleEngineRuleSetJsons.get(0);
        //更新前数据准备
        JSONObject jsonObject = JSONObject.parseObject(ruleEngineRuleSetJson.getRuleSetJson());
        //ruleEngineRuleSet
        RuleSetEditRequest ruleSetEditRequest = new RuleSetEditRequest();
        //调用updateRuleSet接口,规则撤销标记
        ruleSetEditRequest.setRuleCancel(true);
        ruleSetEditRequest.setId(Integer.valueOf(ruleEngineRuleSetJson.getRuleSetId() + StringPool.EMPTY));
        ruleSetEditRequest.setCode(jsonObject.getString(RULE_CODE));
        ruleSetEditRequest.setName(jsonObject.getString(NAME));
        ruleSetEditRequest.setDescription(jsonObject.getString(DESCRIPTION));
        ruleSetEditRequest.setHitPolicy(StatusEnum.getByName(jsonObject.getString(HIT_POLICY)).getStatus());
        ruleSetEditRequest.setDefaultRulePolicy(StatusEnum.getByName(jsonObject.getString(ENABLE_DEFAULT_RULE)).getStatus());
        ruleSetEditRequest.setNodeRepetitionPolicy(NodeRepetitionEnum.getByName(jsonObject.getString(NODE_REPETITION_POLICY)).getStatus());
        ruleSetEditRequest.setProcessRepetitionPolicy(StatusEnum.getByName(jsonObject.getString(PROCESS_REPETITION_POLICY)).getStatus());
        JSONArray nodeIdList = jsonObject.getJSONArray(NODE_ID_LIST);
        if (CollUtil.isNotEmpty(nodeIdList)) {
            String collect = nodeIdList.stream().map(Object::toString).collect(Collectors.joining(StringPool.COMMA));
            ruleSetEditRequest.setProcessRepetitionNodeName(collect);
        }
        //撤销前置条件
        if (jsonObject.containsKey(PRE_CONDITION_SET)) {
            cancelPreConditionSet(jsonObject, ruleSetEditRequest);
        }
        //规则
        if (jsonObject.containsKey(RULES)) {
            cancelNormalRules(jsonObject, ruleSetEditRequest);
        } else {
            ruleSetEditRequest.setNormalRules(null);
        }
        //特殊规则
        if (jsonObject.containsKey(SPECIAL_RULES)) {
            cancelSpecialRule(jsonObject, ruleSetEditRequest);
        } else {
            ruleSetEditRequest.setSpecialRule(null);
        }
        //默认规则
        if (jsonObject.containsKey(DEFAULT_RULE)) {
            cancelDefaultRule(jsonObject, ruleSetEditRequest);
        } else {
            //如果没有默认规则了，删除default_rule_id
            ruleSetEditRequest.setDefaultRule(null);
        }
        //撤销分发策略
        RuleSetEditRequest.RandomResult randomResult = null;
        if (jsonObject.containsKey(RANDOM_RESULT)) {
            JSONObject randomResultJson = jsonObject.getJSONObject(RANDOM_RESULT);
            randomResult = new RuleSetEditRequest.RandomResult();
            randomResult.setCount(randomResultJson.getInteger(COUNT));
            randomResult.setResultSwitch(randomResultJson.getBoolean(ENABLE));
        }
        ruleSetEditRequest.setRandomResult(randomResult);
        //把set表中的待发布版本删除，已发布版本还原
        ruleEngineRuleSetMapper.ruleCancel(ruleSetCancelRequest.getVersion(), ruleSetCancelRequest.getId());
        return ruleEngineRuleSetService.updateRuleSet(ruleSetEditRequest);
    }

    /**
     * 撤销前置条件
     *
     * @param jsonObject         j
     * @param ruleSetEditRequest r
     */
    private void cancelPreConditionSet(JSONObject jsonObject, RuleSetEditRequest ruleSetEditRequest) {
        JSONObject preConditionSets = jsonObject.getJSONObject(PRE_CONDITION_SET);
        PreConditionSet preConditionSetNew = new PreConditionSet();
        ruleSetEditRequest.setPreConditionSet(preConditionSetNew);
        preConditionSetNew.setPolicy(preConditionSets.getInteger(POLICY));
        if (Objects.equals(preConditionSets.getInteger(POLICY), 0)) {
            return;
        }
        List<PreConditionSet.ConditionSet> conditionSetNew = new ArrayList<>();
        JSONArray conditionSet = preConditionSets.getJSONArray(SET);
        for (Object preConditionSet : conditionSet) {
            JSONObject preCondition = getJSONObjectProxy((JSONObject) preConditionSet);
            PreConditionSet.ConditionSet conditionNew = new PreConditionSet.ConditionSet();
            conditionNew.setName(preCondition.getString(NAME));
            conditionNew.setScope(preCondition.getInteger(SCOPE));
            conditionNew.setOrder(preCondition.getInteger(ORDER));

            //condition_set
            ArrayList<ConditionSetBean> conditionSetBeans = new ArrayList<>();
            JSONObject preConditionJSONObject = preCondition.getJSONObject(CONDITION_SET);
            rulesConditionSet(conditionSetBeans, preConditionJSONObject);
            conditionNew.setConditionSet(conditionSetBeans);

            conditionSetNew.add(conditionNew);
        }
        preConditionSetNew.setSet(conditionSetNew);
    }


    /**
     * 回退默认规则
     *
     * @param jsonObject         jsonObject
     * @param ruleSetEditRequest ruleSetEditRequest
     */
    private void cancelDefaultRule(JSONObject jsonObject, RuleSetEditRequest ruleSetEditRequest) {
        JSONObject defaultRule = getJSONObjectProxy(jsonObject.getJSONObject(DEFAULT_RULE));
        JSONObject actionVariable = defaultRule.getJSONObject(ACTION_VARIABLE);
        val defaultRuleBean = new RuleSetEditRequest.DefaultRuleBean();
        val actionVariableBean = new RuleSetEditRequest.ActionVariableBean();
        //元素，变量，固定值数值类型
        actionVariableBean.setValueType(actionVariable.getString(DATA_TYPE));
        actionVariableBean.setType(RuleEngineLoadJsonDataImpl.getType(actionVariable));
        actionVariableBean.setValue(getVariableValueId(actionVariable));
        defaultRuleBean.setActionVariable(actionVariableBean);
        ruleSetEditRequest.setDefaultRule(defaultRuleBean);
    }

    /**
     * 回退特殊规则
     *
     * @param jsonObject         jsonObject
     * @param ruleSetEditRequest ruleSetEditRequest
     */
    private void cancelSpecialRule(JSONObject jsonObject, RuleSetEditRequest ruleSetEditRequest) {
        JSONArray specialRules = jsonObject.getJSONArray(SPECIAL_RULES);
        if (CollUtil.isEmpty(specialRules)) {
            return;
        }
        List<RuleSetEditRequest.SpecialRuleBean> specialRuleBeans = new ArrayList<>();
        for (Object specialRule : specialRules) {
            JSONObject special = getJSONObjectProxy((JSONObject) specialRule);
            RuleSetEditRequest.SpecialRuleBean specialRuleBean = new RuleSetEditRequest.SpecialRuleBean();
            SpecialRuleEditParam.SpecialPolicyBean specialPolicyBean = new SpecialRuleEditParam.SpecialPolicyBean();
            JSONObject actionVariable = special.getJSONObject(ACTION_VARIABLE);
            JSONObject parameterList = actionVariable.getJSONObject(PARAMETER_LIST);
            //加签人
            JSONObject assignPerson = parameterList.getJSONObject(ASSIGN_PERSON_);
            specialPolicyBean.setAssignPerson(cancelAssign(assignPerson));
            //被加签人
            JSONObject assignedPerson = parameterList.getJSONObject(ASSIGNED_PERSON_);
            specialPolicyBean.setAssignedPerson(cancelAssign(assignedPerson));
            JSONObject assignWay = parameterList.getJSONObject(ASSIGN_WAY_);
            if (assignWay != null) {
                specialPolicyBean.setAssignWay(assignWay.getInteger(VALUE));
            }
            //授权非授权
            JSONObject authorization = parameterList.getJSONObject(AUTHORIZATION);
            if (authorization != null) {
                specialPolicyBean.setAuthorizationType(authorization.getInteger(VALUE));
            }
            JSONObject assignAmount = parameterList.getJSONObject(ASSIGN_AMOUNT_);
            if (assignAmount != null) {
                specialPolicyBean.setAssignAmount(assignAmount.get(VALUE));
            }
            JSONObject amount = parameterList.getJSONObject(AMOUNT);
            if (amount != null) {
                Integer elementId = amount.getInteger(ELEMENT_ID);
                //amount.elementId 如果不为空，就是有金额
                if (Validator.isNotEmpty(elementId)) {
                    //amount.value
                    specialPolicyBean.setElementId(elementId);
                    specialPolicyBean.setHasAmount(true);
                } else {
                    specialPolicyBean.setHasAmount(false);
                }
            }
            //特殊规则条件
            ArrayList<ConditionSetBean> conditionSetBeans = new ArrayList<>(special.size());
            rulesConditionSet(conditionSetBeans, special.getJSONObject(CONDITION_SET));
            specialRuleBean.setConditionSet(conditionSetBeans);
            //特殊规则策略
            specialRuleBean.setSpecialPolicyBean(specialPolicyBean);
            //特殊规则id
            specialRuleBean.setSpecialRuleId(special.getIntValue(RULE_ID));
            //特殊规规则order
            specialRuleBean.setSpecialRuleOrder(special.getIntValue(ORDER_NO));
            //前置条件
            List<PreConditionSetBean> preConditionSetBeans = cancelPreConditionSetName(special);
            specialRuleBean.setPreConditionSet(preConditionSetBeans);
            specialRuleBeans.add(specialRuleBean);
        }
        ruleSetEditRequest.setSpecialRule(specialRuleBeans);
    }

    /**
     * 前置条件
     *
     * @param special special
     */
    private List<PreConditionSetBean> cancelPreConditionSetName(JSONObject special) {
        List<PreConditionSetBean> preConditionSet = new ArrayList<>();
        JSONArray preConditionSetName = special.getJSONArray(PRE_CONDITION_SET_NAME);
        if (CollUtil.isNotEmpty(preConditionSetName)) {
            for (Object name : preConditionSetName) {
                PreConditionSetBean conditionSetBean = new PreConditionSetBean();
                conditionSetBean.setName(name + StringPool.EMPTY);
                preConditionSet.add(conditionSetBean);
            }
        }
        return preConditionSet;
    }

    /**
     * @param assign assign
     */
    private SpecialRuleEditParam.SpecialPolicyBean.AssignPersonBean cancelAssign(JSONObject assign) {
        if (assign != null) {
            val assignPersonBean = new SpecialRuleEditParam.SpecialPolicyBean.AssignPersonBean();
            assignPersonBean.setType(RuleEngineLoadJsonDataImpl.getType(assign));
            assignPersonBean.setValueType(assign.getString(DATA_TYPE));
            assignPersonBean.setValue(getVariableValueId(assign));
            return assignPersonBean;
        } else {
            return null;
        }
    }

    /**
     * 回退普通规则
     *
     * @param jsonObject         jsonObject
     * @param ruleSetEditRequest ruleSetEditRequest
     */
    private void cancelNormalRules(JSONObject jsonObject, RuleSetEditRequest ruleSetEditRequest) {
        JSONArray rules = jsonObject.getJSONArray(RULES);
        if (CollUtil.isEmpty(rules)) {
            return;
        }
        List<RuleSetEditRequest.NormalRulesBean> normalRulesBeans = new ArrayList<>();
        for (Object rule : rules) {
            JSONObject rul = getJSONObjectProxy((JSONObject) rule);
            RuleSetEditRequest.NormalRulesBean normalRulesBean = new RuleSetEditRequest.NormalRulesBean();
            normalRulesBean.setNormalRuleOrder(rul.getInteger(ORDER_NO));
            RuleSetEditRequest.ActionVariableBean actionVariableBean = new RuleSetEditRequest.ActionVariableBean();
            JSONObject actionVariable = rul.getJSONObject(ACTION_VARIABLE);
            actionVariableBean.setValueType(actionVariable.getString(DATA_TYPE));
            actionVariableBean.setType(RuleEngineLoadJsonDataImpl.getType(actionVariable));
            actionVariableBean.setValue(getVariableValueId(actionVariable));
            normalRulesBean.setActionVariable(actionVariableBean);
            ArrayList<ConditionSetBean> conditionSetBeans = new ArrayList<>();
            rulesConditionSet(conditionSetBeans, rul.getJSONObject(CONDITION_SET));
            normalRulesBean.setConditionSet(conditionSetBeans);

            //前置条件
            List<PreConditionSetBean> preConditionSetBeans = cancelPreConditionSetName(rul);
            normalRulesBean.setPreConditionSet(preConditionSetBeans);
            normalRulesBeans.add(normalRulesBean);
        }
        ruleSetEditRequest.setNormalRules(normalRulesBeans);
    }

    /**
     * rulesConditionSet
     *
     * @param conditionSetBeans conditionSetBeans
     * @param rul               rul
     */
    private void rulesConditionSet(ArrayList<ConditionSetBean> conditionSetBeans, JSONObject rul) {
        JSONArray conditionGroups = rul.getJSONArray(CONDITION_GROUPS);
        if (CollUtil.isEmpty(conditionGroups)) {
            return;
        }
        for (Object conditionGroup : conditionGroups) {
            JSONObject conditionGr = getJSONObjectProxy((JSONObject) conditionGroup);
            ConditionSetBean conditionSetBean = new ConditionSetBean();
            conditionSetBean.setConditionGroupOrder(conditionGr.getInteger(CONDITION_GROUP_ORDER));
            ArrayList<ConditionSetBean.ConditionsBean> conditionsBeans = new ArrayList<>();
            JSONArray conditions = conditionGr.getJSONArray(CONDITIONS);
            if (CollUtil.isEmpty(conditions)) {
                continue;
            }
            for (Object condition : conditions) {
                JSONObject con = getJSONObjectProxy((JSONObject) condition);
                ConditionSetBean.ConditionsBean conditionsBean = new ConditionSetBean.ConditionsBean();
                conditionsBean.setConditionOrder(con.getInteger(CONDITION_ORDER));
                conditionsBean.setConditionId(con.getIntValue(CONDITION_ID));
                conditionsBean.setConditionName(con.getString(CONDITION_NAME));
                JSONObject leftVariable = con.getJSONObject(LEFT_VARIABLE);
                ConfigBean configBean = new ConfigBean();
                ConfigBean.LeftBean leftBean = new ConfigBean.LeftBean();
                leftBean.setType(RuleEngineLoadJsonDataImpl.getType(leftVariable));
                leftBean.setValue(getVariableValueId(leftVariable));
                leftBean.setValueName(leftVariable.getString(VARIABLE_NAME));
                leftBean.setValueType(leftVariable.getString(DATA_TYPE));
                configBean.setLeftVariable(leftBean);
                configBean.setSymbol(Symbol.getSymbolByEnumName(con.getString(OPERATOR_TYPE)).getSymbol());
                JSONObject rightVariable = con.getJSONObject(RIGHT_VARIABLE);
                ConfigBean.RightBean rightBean = new ConfigBean.RightBean();
                rightBean.setType(RuleEngineLoadJsonDataImpl.getType(rightVariable));
                rightBean.setValue(getVariableValueId(rightVariable));
                rightBean.setValueName(rightVariable.getString(VARIABLE_NAME));
                rightBean.setValueType(rightVariable.getString(DATA_TYPE));
                configBean.setRightVariable(rightBean);
                conditionsBean.setConfig(configBean);
                conditionsBeans.add(conditionsBean);
            }
            conditionSetBean.setConditions(conditionsBeans);
            conditionSetBeans.add(conditionSetBean);
        }
    }


    /**
     * 规则预览页面-规则展示
     *
     * @param ruleSetViewRequest ruleSetViewRequest
     * @return 规则
     */
    @Override
    public JSONObject view(RuleSetViewRequest ruleSetViewRequest) {
        RuleEngineRuleSet ruleEngineRuleSet = ruleEngineRuleSetManager.getById(ruleSetViewRequest.getId());
        if (Objects.isNull(ruleEngineRuleSet)) {
            throw new ValidationException((String.format("根据%s未查询到规则信息", ruleSetViewRequest.getId())));
        }
        ManageUser manageUser = WfcRuleContext.getLoginedUser();
        String editUserId = ruleEngineRuleSet.getEditUserId();
        //如果有人在编辑，如果不是自己，则.
        if (Validator.isNotEmpty(editUserId)) {
            if (!Objects.equals(editUserId, manageUser.getEmployeeId())) {
                throw new ValidException(400007, "用户：{},正在编辑中！", ruleEngineRuleSet.getEditUserName());
            }
        }
        List<RuleEngineRuleSetJson> list = ruleEngineRuleSetJsonManager.lambdaQuery()
                .eq(RuleEngineRuleSetJson::getIsCurEnv, RuleEnvironmentEnum.CURRENT_ENV.getValue())
                .eq(RuleEngineRuleSetJson::getRuleSetId, ruleSetViewRequest.getId())
                //不显示草稿与历史    11.19日修改 规则大闭环，可以展示历史规则
                .notIn(RuleEngineRuleSetJson::getPublished, Collections.singletonList(PublishEnum.DRAFT.getType()))
                .eq(RuleEngineRuleSetJson::getDeleted, DeletedEnum.ENABLE.getStatus()).list();
        if (CollUtil.isEmpty(list)) {
            throw new ValidationException("没有查询到数据");
        }
        String version = ruleSetViewRequest.getVersion();
        RuleEngineRuleSetJson ruleEngineRuleSetJson;
        //如果传版本了
        if (Validator.isNotEmpty(version)) {
            List<RuleEngineRuleSetJson> ruleEngineRuleSetJsons = list.stream().filter(f -> f.getRuleVersion().equals(ruleSetViewRequest.getVersion())).collect(Collectors.toList());
            if (CollUtil.isEmpty(ruleEngineRuleSetJsons)) {
                throw new ValidationException("没有与之对应的版本");
            }
            ruleEngineRuleSetJson = ruleEngineRuleSetJsons.get(0);
        } else {
            ruleEngineRuleSetJson = RuleEngineLoadServiceImpl.getRuleEngineRuleSetJsonNew(list);
            if (ruleEngineRuleSetJson == null) {
                throw new ValidationException("既没有待发布版本，也没有已发布版本");
            }
        }
        String ruleSetJson = ruleEngineRuleSetJson.getRuleSetJson();
        JSONObject jsonObject = JSONObject.parseObject(ruleSetJson);
        JSONObject newJsonObject = new JSONObject();
        //version
        JSONArray versionsJson = new JSONArray();
        for (RuleEngineRuleSetJson engineRuleSetJson : list) {
            if (RuleEnvironmentEnum.CURRENT_ENV.getValue().equals(engineRuleSetJson.getIsCurEnv())) {
                JSONObject versionJson = new JSONObject();
                versionJson.put(VALUE, engineRuleSetJson.getRuleVersion());
                //如果是发布到其他环境的，设置为已发布
                if (engineRuleSetJson.getPublished().equals(PublishEnum.PUBLISH_OTHER_ENV.getType())) {
                    versionJson.put(PUBLISHED, PublishEnum.PUBLISH.getType());
                } else {
                    versionJson.put(PUBLISHED, engineRuleSetJson.getPublished());
                }
                versionsJson.add(versionJson);
            }
        }
        //如果是待发布版本,读取最新的名称与说明
        if (ruleEngineRuleSetJson.getPublished().equals(PublishEnum.WAITING_PUBLISH.getType())) {
            RuleEngineRuleSet engineRuleSetManagerById = ruleEngineRuleSetManager.getById(ruleEngineRuleSetJson.getRuleSetId());
            newJsonObject.put(NAME, engineRuleSetManagerById.getName());
            newJsonObject.put(DESCRIPTION, engineRuleSetManagerById.getDescription());
        } else {
            newJsonObject.put(NAME, jsonObject.getString(NAME));
            newJsonObject.put(DESCRIPTION, jsonObject.getString(DESCRIPTION));
        }
        newJsonObject.put(VERSIONS, versionsJson);
        newJsonObject.put(CURRENT_VERSION_, ruleEngineRuleSetJson.getRuleVersion());
        //getPublished待确认
        newJsonObject.put(PUBLISH_STATUS_, ruleEngineRuleSetJson.getPublished());
        //根据id查询这个规则的数据
        newJsonObject.put(CODE, jsonObject.getString(RULE_CODE));
        newJsonObject.put(HIT_POLICY_, StatusEnum.getByName(jsonObject.getString(HIT_POLICY)).getStatus());
        newJsonObject.put(NODE_REPETITION_POLICY_, NodeRepetitionEnum.getByName(jsonObject.getString(NODE_REPETITION_POLICY)).getStatus());
        newJsonObject.put(DEFAULT_RULE_POLICY_, StatusEnum.getByName(jsonObject.getString(ENABLE_DEFAULT_RULE)).getStatus());
        newJsonObject.put(PROCESS_REPETITION_POLICY_, StatusEnum.getByName(jsonObject.getString(PROCESS_REPETITION_POLICY)).getStatus());
        JSONArray nodeIdList = jsonObject.getJSONArray(NODE_ID_LIST);
        if (CollUtil.isNotEmpty(nodeIdList)) {
            String collect = nodeIdList.stream().map(Object::toString).collect(Collectors.joining(","));
            newJsonObject.put(PROCESS_REPETITION_NODE_NAME_, Validator.isEmpty(collect) ? null : collect);
        } else {
            //10.30日修改，如果没有节点名称返回一个null
            newJsonObject.put(PROCESS_REPETITION_NODE_NAME_, null);
        }
        if (jsonObject.containsKey(PRE_CONDITION_SET)) {
            viewPreConditionSet(jsonObject, newJsonObject);
        }
        //规则
        if (jsonObject.containsKey(RULES)) {
            viewNormalRules(jsonObject, newJsonObject);
        }
        //特殊规则
        if (jsonObject.containsKey(SPECIAL_RULES)) {
            viewSpecialRule(jsonObject, newJsonObject);
        }
        //默认规则
        if (jsonObject.containsKey(DEFAULT_RULE)) {
            viewDefaultRule(jsonObject, newJsonObject);
        } else {
            //如果没有DEFAULT_RULE 则是null，前端要求需要此字段
            newJsonObject.put(DEFAULT_RULE_, null);
        }
        //parameters
        if (jsonObject.containsKey(PARAMETERS)) {
            viewParameters(jsonObject, newJsonObject);
        }
        //分发策略
        if (jsonObject.containsKey(RANDOM_RESULT)) {
            JSONObject randomResultJson = jsonObject.getJSONObject(RANDOM_RESULT);
            JSONObject rulNew = new JSONObject();
            rulNew.put(COUNT, randomResultJson.getInteger(COUNT));
            rulNew.put(ENABLE, randomResultJson.getBoolean(ENABLE));
            newJsonObject.put(RANDOM_RESULT, rulNew);
        } else {
            newJsonObject.put(RANDOM_RESULT, null);
        }
        return newJsonObject;
    }

    /**
     * 展示前置条件
     *
     * @param jsonObject    j
     * @param newJsonObject n
     */
    private void viewPreConditionSet(JSONObject jsonObject, JSONObject newJsonObject) {
        JSONObject preConditionSets = jsonObject.getJSONObject(PRE_CONDITION_SET);
        JSONObject preConditionSetsNew = new JSONObject();
        preConditionSetsNew.put(POLICY, preConditionSets.getInteger(POLICY));
        //前置条件集策略 0关闭 1 开启
        if (Objects.equals(preConditionSets.getInteger(POLICY), 0)) {
            newJsonObject.put(PRE_CONDITION_SET_, preConditionSetsNew);
            return;
        }
        JSONArray conditionSetsNew = new JSONArray();
        preConditionSetsNew.put(SET, conditionSetsNew);
        JSONArray conditionSet = preConditionSets.getJSONArray(SET);
        for (Object preConditionSet : conditionSet) {
            JSONObject preCondition = getJSONObjectProxy((JSONObject) preConditionSet);
            JSONObject preConditionNew = new JSONObject();
            preConditionNew.put(ORDER, preCondition.getInteger(ORDER));
            preConditionNew.put(NAME, preCondition.getString(NAME));
            preConditionNew.put(SCOPE, preCondition.getInteger(SCOPE));
            //condition_set
            JSONObject set = preCondition.getJSONObject(CONDITION_SET);
            //viewNormalRules.conditionSet
            JSONArray conditionSetNew = viewConditionSet(set);
            preConditionNew.put(CONDITION_SET_, conditionSetNew);
            conditionSetsNew.add(preConditionNew);
        }
        newJsonObject.put(PRE_CONDITION_SET_, preConditionSetsNew);
    }

    /**
     * parameters
     * 解决元素名称修改，规则展示时的bug
     *
     * @param jsonObject    jsonObject
     * @param newJsonObject newJsonObject
     */
    void viewParameters(JSONObject jsonObject, JSONObject newJsonObject) {
        JSONArray params = jsonObject.getJSONArray(PARAMETERS);
        if (CollUtil.isNotEmpty(params)) {
            //获取到biz key
            String bizKey = jsonObject.getString(RuleEngineLoadJsonDataImpl.BIZ_KEY);
            //对元素去重
            Object[] toArray = params.stream().distinct().toArray();
            Set<String> codes = Stream.of(toArray).map(m -> ((JSONObject) m).getString(CODE)).collect(Collectors.toSet());
            List<RuleEngineElement> elements = ruleEngineElementManager.lambdaQuery().eq(RuleEngineElement::getDeleted, DeletedEnum.ENABLE.getStatus())
                    .eq(RuleEngineElement::getBizCode, bizKey)
                    .in(RuleEngineElement::getCode, codes).list();
            //key 是元素code，value是元素name
            Map<String, String> ruleElementCache = elements.stream().collect(Collectors.toMap(RuleEngineElement::getCode, RuleEngineElement::getName));
            JSONArray jsonArray = new JSONArray();
            //遍历元素，以及从查询缓存中获取name
            for (Object jsonObj : toArray) {
                JSONObject param = (JSONObject) jsonObj;
                JSONObject paramNew = new JSONObject();
                String code = param.getString(CODE);
                paramNew.put(CODE, code);
                String name = ruleElementCache.get(code) != null ? ruleElementCache.get(code) : param.getString(NAME);
                paramNew.put(NAME, name);
                //规则预览页面-测试用例list 需要展示value字段
                if (!Objects.isNull(param.getString(VALUE))) {
                    paramNew.put(VALUE, param.getString(VALUE));
                }
                paramNew.put(DATA_TYPE, param.getString(DATA_TYPE));
                jsonArray.add(paramNew);
            }
            newJsonObject.put(PARAMETERS, jsonArray);
        }
    }

    /**
     * 普通规则
     *
     * @param jsonObject    查询到的json
     * @param newJsonObject 需要返回的json
     */
    private void viewNormalRules(JSONObject jsonObject, JSONObject newJsonObject) {
        JSONArray rules = jsonObject.getJSONArray(RULES);
        JSONArray specialRulesNew = new JSONArray();
        for (Object rule : rules) {
            JSONObject rul = getJSONObjectProxy((JSONObject) rule);
            JSONObject rulNew = new JSONObject();
            //normal_rule_order
            rulNew.put(NORMAL_RULE_ORDER_, rul.getString(ORDER_NO));
            //action_variable
            JSONObject actionVariableNew = new JSONObject();
            JSONObject actionVariable = rul.getJSONObject(ACTION_VARIABLE);
            actionVariableNew.put(VALUE, viewVariableValue(actionVariable));
            rulNew.put(ACTION_VARIABLE_, actionVariableNew);
            //condition_set
            JSONObject conditionSet = rul.getJSONObject(CONDITION_SET);
            //viewNormalRules.conditionSet
            JSONArray conditionSetNew = viewConditionSet(conditionSet);
            //如果特殊规则没有条件，返回一个[]，前端无法判断，要求返回null
            rulNew.put(CONDITION_SET_, conditionSetNew);

            //前置条件
            viewPreConditionSetName(rul, rulNew);
            specialRulesNew.add(rulNew);
        }
        newJsonObject.put(NORMAL_RULES_, specialRulesNew);
    }

    /**
     * 规则前置条件处理
     *
     * @param rul    rul
     * @param rulNew rulNew
     */
    private void viewPreConditionSetName(JSONObject rul, JSONObject rulNew) {
        JSONArray preConditionSetName = rul.getJSONArray(PRE_CONDITION_SET_NAME);
        if (CollUtil.isNotEmpty(preConditionSetName)) {
            JSONArray preConditionSetNameNew = new JSONArray();
            for (Object name : preConditionSetName) {
                JSONObject jsonNew = new JSONObject();
                jsonNew.put(NAME, name);
                preConditionSetNameNew.add(jsonNew);
            }
            rulNew.put(PRE_CONDITION_SET_, preConditionSetNameNew);
        }
    }

    /**
     * 特殊规则
     *
     * @param jsonObject    查询到的json
     * @param newJsonObject 需要返回的json
     */
    private void viewSpecialRule(JSONObject jsonObject, JSONObject newJsonObject) {
        JSONArray specialRules = jsonObject.getJSONArray(SPECIAL_RULES);
        JSONArray specialRulesNew = new JSONArray();
        for (Object specialRule : specialRules) {
            JSONObject special = getJSONObjectProxy((JSONObject) specialRule);
            JSONObject objectNew = getJSONObjectProxy(new JSONObject());
            objectNew.put(SPECIAL_RULE_ORDER_, special.getString(ORDER_NO));
            //special_policy
            JSONObject specialPolicyNew = new JSONObject();
            JSONObject actionVariable = special.getJSONObject(ACTION_VARIABLE);
            JSONObject parameterList = getJSONObjectProxy(actionVariable.getJSONObject(PARAMETER_LIST));
            //assignPerson
            specialPolicyNew.put(ASSIGN_PERSON_, viewAssign(parameterList.getJSONObject(ASSIGN_PERSON_)));
            //assignedPerson
            specialPolicyNew.put(ASSIGNED_PERSON_, viewAssign(parameterList.getJSONObject(ASSIGNED_PERSON_)));
            //assignWay
            specialPolicyNew.put(ASSIGN_WAY_, parameterList.getJSONObject(ASSIGN_WAY_).getString(VALUE));
            //授权非授权
            specialPolicyNew.put(AUTHORIZATION, parameterList.getJSONObject(AUTHORIZATION).getString(VALUE));
            //assignAmount
            specialPolicyNew.put(ASSIGN_AMOUNT_, parameterList.getJSONObject(ASSIGN_AMOUNT_).getString(VALUE));
            //assign
            JSONObject amount = parameterList.getJSONObject(AMOUNT);
            if (amount != null) {
                JSONObject amountNew = new JSONObject();
                RuleEngineElement engineElement = DB.getAndCache(RuleEngineElement.class, amount.getLong(ELEMENT_ID));
                amountNew.put(VARIABLE_NAME, Check.els(engineElement, RuleEngineElement::getName));
                specialPolicyNew.put(AMOUNT, amountNew);
            }
            objectNew.put(SPECIAL_POLICY_, specialPolicyNew);
            //condition_set
            JSONObject conditionSet = special.getJSONObject(CONDITION_SET);
            JSONArray conditionSetNew = viewConditionSet(conditionSet);
            objectNew.put(CONDITION_SET_, conditionSetNew);

            //前置条件
            viewPreConditionSetName(special, objectNew);

            specialRulesNew.add(objectNew);
        }
        newJsonObject.put(SPECIAL_RULE_, specialRulesNew);
    }

    /**
     * viewConditionSet
     * 如果特殊规则没有条件，返回一个[]，前端无法判断，要求返回null
     *
     * @param conditionSet conditionSet
     */
    private JSONArray viewConditionSet(JSONObject conditionSet) {
        JSONArray conditionGroups = conditionSet.getJSONArray(CONDITION_GROUPS);
        if (CollUtil.isEmpty(conditionGroups)) {
            return null;
        }
        JSONArray conditionSetNew = new JSONArray();
        for (Object conditionGroup : conditionGroups) {
            JSONObject group = getJSONObjectProxy((JSONObject) conditionGroup);
            JSONObject groupNew = new JSONObject();
            groupNew.put(CONDITION_GROUP_ORDER_, group.get(CONDITION_GROUP_ORDER));
            JSONArray conditionsNew = null;
            JSONArray conditions = group.getJSONArray(CONDITIONS);
            if (CollUtil.isNotEmpty(conditions)) {
                conditionsNew = new JSONArray();
                for (Object condition : conditions) {
                    JSONObject con = getJSONObjectProxy((JSONObject) condition);
                    JSONObject conNew = new JSONObject();
                    conNew.put(CONDITION_ORDER_, con.get(CONDITION_ORDER));
                    conNew.put(SYMBOL, Symbol.getSymbolByEnumName(con.getString(OPERATOR_TYPE)).getSymbol());
                    conNew.put(CONDITION_NAME, con.get(CONDITION_NAME));
                    //条件左
                    conNew.put(LEFT_VARIABLE_, viewConditionConfig(con.getJSONObject(LEFT_VARIABLE)));
                    //条件右边
                    conNew.put(RIGHT_VARIABLE_, viewConditionConfig(con.getJSONObject(RIGHT_VARIABLE)));
                    conditionsNew.add(conNew);
                }
            }
            groupNew.put(CONDITIONS, conditionsNew);
            conditionSetNew.add(groupNew);
        }
        return conditionSetNew;
    }

    /**
     * 条件配置
     *
     * @param config config
     * @return JSONObject
     */
    private JSONObject viewConditionConfig(JSONObject config) {
        JSONObject configNew = getJSONObjectProxy(new JSONObject());
        configNew.put(VALUE, viewVariableValue(config));
        configNew.put(DATA_TYPE, config.getString(DATA_TYPE));
        Object leftVariableName = config.get(VARIABLE_NAME);
        configNew.put(VARIABLE_NAME, leftVariableName);
        return configNew;
    }

    /**
     * 获取左右值
     *
     * @param leftVariable leftVariable
     * @return String
     */
    private String viewVariableValue(JSONObject leftVariable) {
        if (leftVariable.containsKey(VARIABLE_ID)) {
            //变量，有value显示value,没有显示变量name
            return viewVariableValueProcess(leftVariable);
        } else if (leftVariable.containsKey(ELEMENT_ID)) {
            //元素 显示元素name,原json是元素code，这里需要查询
            Integer elementId = leftVariable.getInteger(ELEMENT_ID);
            RuleEngineElement engineElement = DB.getAndCache(RuleEngineElement.class, elementId);
            return engineElement.getName();
        } else {
            //固定值
            return leftVariable.getString(VALUE);
        }
    }

    /**
     * 如果有值显示值，否则显示VARIABLE_NAME
     *
     * @param actionVariable actionVariable
     * @return String
     */
    private String viewVariableValueProcess(JSONObject actionVariable) {
        //固定值变量 /固定值
        if (actionVariable.containsKey(VALUE)) {
            return actionVariable.getString(VALUE);
        }
        //函数显示函数名称
        RuleEngineVariable engineVariable = DB.getAndCache(RuleEngineVariable.class, actionVariable.getInteger(VARIABLE_ID));
        return engineVariable.getName();
    }

    /**
     * 获取左右值id
     *
     * @param leftVariable leftVariable
     * @return String
     */
    private String getVariableValueId(JSONObject leftVariable) {
        if (leftVariable.containsKey(VARIABLE_ID)) {
            //变量
            return leftVariable.getString(VARIABLE_ID);
        } else if (leftVariable.containsKey(ELEMENT_ID)) {
            //元素
            return leftVariable.getString(ELEMENT_ID);
        } else {
            //固定值
            return leftVariable.getString(VALUE);
        }
    }

    /**
     * 加签
     *
     * @param assignPerson assignPerson
     * @return JSONObject
     */
    private JSONObject viewAssign(JSONObject assignPerson) {
        if (assignPerson.isEmpty()) {
            return null;
        }
        JSONObject assignPersonNew = getJSONObjectProxy(new JSONObject());
        assignPersonNew.put(VALUE_TYPE_, assignPerson.get(DATA_TYPE));
        //元素显示元素名称
        if (assignPerson.containsKey(ELEMENT_ID)) {
            Integer elementId = assignPerson.getInteger(ELEMENT_ID);
            RuleEngineElement managerById = DB.getAndCache(RuleEngineElement.class, elementId);
            assignPersonNew.put(VALUE, null);
            assignPersonNew.put(VALUE_NAME_, managerById.getName());
            return assignPersonNew;
        }
        String assignPersonNewEmpId = assignPerson.getString(VALUE);
        //如果存在员工id
        if (Validator.isNotEmpty(assignPersonNewEmpId)) {
            //如果是集合场景
            if (Objects.equals(assignPerson.get(DATA_TYPE), DataType.COLLECTION.name())) {
                JSONArray list = new JSONArray();
                String[] split = assignPersonNewEmpId.split(StringPool.COMMA);
                for (String id : split) {
                    JSONObject jsonObject = new JSONObject();
                    JSONObject name = new JSONObject();
                    name.put(ZH, id);
                    //默认没有头像
                    jsonObject.put(AVATAR, null);
                    //如果是数字
                    if (id.length() < 10 && NumberUtil.isNumber(id)) {
                        EmployeeResponse employeeResponse = employeeService.getEmployeeById(id);
                        //如果查询到结果
                        if (employeeResponse != null) {
                            jsonObject.put(AVATAR, employeeResponse.getAvatarUrl());
                            //如果有名称就名称，如果没有名称就只有工号
                            name.put(ZH, employeeResponse.getEmployeeName() + id);
                        }
                    }
                    jsonObject.put(NAME, name);
                    list.add(jsonObject);
                }
                assignPersonNew.put(LIST, list);
                return assignPersonNew;
            }
            //其他场景
            assignPersonNew.put(VALUE, assignPersonNewEmpId);
            //如果加签人与被加签人找不到，只显示value
            assignPersonNew.put(VALUE_NAME_, null);
            //如果是数字,10.25 bug修复，我们根据id查询员工设计时使用Integer接收员工id,大于10位使用Integer接收报错
            // 如果加签人与被加签人输入的大于等于10就不是员工编号，目前最大员工编号7位
            if (assignPersonNewEmpId.length() < 10 && NumberUtil.isNumber(assignPersonNewEmpId)) {
                EmployeeResponse employeeResponse = employeeService.getEmployeeById(assignPersonNewEmpId);
                //如果查询到结果
                if (employeeResponse != null) {
                    assignPersonNew.put(AVATAR_URL, employeeResponse.getAvatarUrl());
                    //如果是员工，显示员工name
                    assignPersonNew.put(VALUE_NAME_, employeeResponse.getEmployeeName());
                }
            }
            return assignPersonNew;
        }
        //函数显示函数名称
        Integer variableId = assignPerson.getInteger(VARIABLE_ID);
        if (variableId == null) {
            return null;
        }
        RuleEngineVariable ruleEngineVariable = DB.getAndCache(RuleEngineVariable.class, variableId);
        assignPersonNew.put(VALUE, null);
        assignPersonNew.put(VALUE_NAME_, ruleEngineVariable.getName());
        return assignPersonNew;
    }

    /**
     * 默认规则
     *
     * @param jsonObject    查询到的json
     * @param newJsonObject 需要返回的json
     */
    private void viewDefaultRule(JSONObject jsonObject, JSONObject newJsonObject) {
        JSONObject defaultRule = getJSONObjectProxy(jsonObject.getJSONObject(DEFAULT_RULE));
        if (defaultRule.containsKey(ACTION_VARIABLE)) {
            JSONObject actionVariable = defaultRule.getJSONObject(ACTION_VARIABLE);
            JSONObject defaultRuleJson = new JSONObject();
            JSONObject actionVariableJson = new JSONObject();
            //有value显示value，没有显示VARIABLE_NAME
            actionVariableJson.put(VALUE, viewVariableValue(actionVariable));
            defaultRuleJson.put(ACTION_VARIABLE_, actionVariableJson);
            newJsonObject.put(DEFAULT_RULE_, defaultRuleJson);
        }
    }

    /**
     * 使用动态代理技术解决频繁空指针异常
     *
     * @param jsonObject 被代理的目标类
     * @return JSONObject
     */
    private static JSONObject getJSONObjectProxy(JSONObject jsonObject) {
        Class<? extends JSONObject> aClass = jsonObject.getClass();
        return (JSONObject) Enhancer.create(aClass, aClass.getInterfaces(), (MethodInterceptor) (object, method, args, methodProxy) -> {
            //put时，如果为null 不put
            if ("put".equals(method.getName())) {
                if (args[0] == null || args[1] == null) {
                    return null;
                }
            }
            //当获取getJSONObject时，即使没有，也不返回null
            if ("getJSONObject".equals(method.getName())) {
                Object invoke = method.invoke(jsonObject, args);
                if (invoke == null) {
                    //对于新创建的也被代理
                    return getJSONObjectProxy(new JSONObject());
                }
                return invoke;
            }
            //原有的isEmpty没有判断null
            if ("isEmpty".equals(method.getName())) {
                return CollUtil.isEmpty(jsonObject);
            }
            //其他方法忽略
            return method.invoke(jsonObject, args);
        });
    }

    /**
     * 规则预览页面获取版本接口
     *
     * @param id 规则id
     * @return 版本信息
     */
    @Override
    public List<EnvVersion> envVersions(IdRequest id) {
        List<EnvVersion> envVersions = new ArrayList<>();
        int ruleSetId = id.getId();
        //首先获取最新版本
        String maxVersion = "";
        //未发布版本信息
        EnvVersion hasUnPublish = new EnvVersion();
        hasUnPublish.setEnvironment(UN_PUBLISH);
        hasUnPublish.setPublished(false);
        RuleEngineRuleSet ruleSetById = ruleEngineRuleSetManager.getById(ruleSetId);
        if (!Objects.isNull(ruleSetById)) {
            //已发布版本号
            String publishVersion = ruleSetById.getPublishVersion();
            //待发布版本号
            String preparedVersion = ruleSetById.getPreparedVersion();
            if (StringUtils.isNotEmpty(preparedVersion)) {
                maxVersion = preparedVersion;
                //在rule_engine_rule_set表中查询到未发布的信息("enviroment":"unPublish",//待发布)
                LambdaQueryWrapper<RuleEngineRuleSet> ruleSetQuery = new LambdaQueryWrapper<>();
                ruleSetQuery.eq(RuleEngineRuleSet::getId, ruleSetId).eq(RuleEngineRuleSet::getCreateStatus, 0);
                String[] arr = preparedVersion.split(",");
                hasUnPublish.setVersion(Arrays.asList(arr));
            } else {
                //该规则已发布
                maxVersion = publishVersion;
                //人为补齐数据
                hasUnPublish.setVersion(null);
            }
            envVersions.add(hasUnPublish);
        }
        //首先在rule_engine_enviroment_publish_history 表中查询发布历史
        List<RuleEngineEnviromentPublishHistory> envVersionlistById = ruleEngineEnviromentPublishHistoryMap.getEnvVersionById(ruleSetId);
        List<String> allEnviromentNames = Arrays.asList(TEST_PRE_PRD.split(","));
        List<String> enviromentNames = envVersionlistById.stream().map(RuleEngineEnviromentPublishHistory::getEnviromentName).collect(Collectors.toList());
        List<String> reduce1 = allEnviromentNames.stream().filter(item -> !enviromentNames.contains(item)).collect(Collectors.toList());
        //将没有发布的数据人为补齐
        if (CollUtil.isNotEmpty(reduce1)) {
            for (String notExistEnv : reduce1) {
                EnvVersion envVersion = new EnvVersion();
                envVersion.setEnvironment(notExistEnv.toUpperCase());
                envVersion.setVersion(null);
                envVersion.setPublished(false);
                envVersions.add(envVersion);
            }
        }
        //查询各个环境发布历史数据
        for (RuleEngineEnviromentPublishHistory ruleEnvVersion : envVersionlistById) {
            EnvVersion envVersion = new EnvVersion();
            String enviromentName = ruleEnvVersion.getEnviromentName();
            String versions = ruleEnvVersion.getRuleSetVersion();
            envVersion.setEnvironment(enviromentName.toUpperCase());
            envVersion.setVersion(versionSort(Arrays.asList(versions.split(","))));
            //当前规则的最新版本是否已发布到 enviroment
            boolean result = versions.contains(maxVersion);
            envVersion.setPublished(result);
            envVersions.add(envVersion);
        }
        //排序 prd-pre-test-unPublish
        return envVersions.stream().sorted((h1, h2) -> h1.getEnvironment().compareTo(h2.getEnvironment())).collect(Collectors.toList());
    }


    public static List<String> versionSort(List<String> version) {
        //如果为null 不排序
        if (CollUtil.isEmpty(version)) {
            return null;
        }
        //如果只有1个直接返回，不排序
        if (version.size() == 1) {
            return version;
        }
        //排序，求最新版本
        return version.stream().
                sorted((v1, v2) -> Integer.parseInt(v2.replace("V", "").split("\\.")[0])
                        - Integer.parseInt(v1.replace("V", "").split("\\.")[0]))
                .sorted((v1, v2) -> {
                    String[] max = v2.replace("V", "").split("\\.");
                    String[] min = v1.replace("V", "").split("\\.");
                    if (Objects.equals(max[0], min[0])) {
                        return Integer.parseInt(max[1]) - Integer.parseInt(min[1]);
                    } else {
                        return 0;
                    }
                }).collect(Collectors.toList());
    }

}