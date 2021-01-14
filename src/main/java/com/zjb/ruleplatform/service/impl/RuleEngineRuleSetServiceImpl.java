package com.zjb.ruleplatform.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.lang.Validator;
import cn.hutool.core.util.ObjectUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.JSONPath;
import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.founder.ego.annotation.UpdateRuleEngine;
import com.founder.ego.common.pojo.ManageUser;
import com.founder.ego.common.request.PageBase;
import com.founder.ego.common.request.PageRequest;
import com.founder.ego.common.response.PageResponse;
import com.founder.ego.common.response.PageResult;
import com.founder.ego.common.response.Rows;
import com.founder.ego.config.WfcRuleContext;
import com.founder.ego.enumbean.*;
import com.founder.ego.exception.ValidException;
import com.founder.ego.ruleengine.core.enums.RuleTypeEnum;
import com.founder.ego.ruleengine.core.enums.VariableTypeEnum;
import com.founder.ego.ruleengine.core.rule.component.RuleSetConditionSet;
import com.founder.ego.service.auth.AuthService;
import com.founder.ego.service.auth.RuleEngineBizMemberDataService;
import com.founder.ego.service.auth.RuleEngineBizMemberService;
import com.founder.ego.service.ruleengine.RuleEngineLoadService;
import com.founder.ego.service.ruleengine.RuleEngineRuleSetService;
import com.founder.ego.service.ruleengine.RuleEngineSpecialRuleService;
import com.founder.ego.service.ruleengine.RuleLockService;
import com.founder.ego.store.bpm.entity.*;
import com.founder.ego.store.bpm.manager.*;
import com.founder.ego.store.bpm.mapper.RuleEngineRuleSetMapper;
import com.founder.ego.utils.DB;
import com.founder.ego.utils.JsonUtils;
import com.founder.ego.utils.PageUtils;
import com.founder.ego.utils.RuleNormalUtils;
import com.founder.ego.vo.ruleengine.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.*;
import java.util.stream.Collectors;

import static com.founder.ego.enumbean.DeletedEnum.DISABLE;
import static com.founder.ego.enumbean.DeletedEnum.ENABLE;
import static com.founder.ego.enumbean.DirectEnum.RULE_DEF;
import static com.founder.ego.enumbean.DirectEnum.RULE_VIEW;
import static com.founder.ego.enumbean.EnvEnum.PRD;
import static com.founder.ego.enumbean.PublishEnum.WAITING_PUBLISH;
import static com.founder.ego.enumbean.ResponseCode.PARAM_ERROR;
import static com.founder.ego.enumbean.ResponseCode.SERVICE_ERROR;
import static com.founder.ego.ruleengine.core.enums.VariableTypeEnum.*;
import static com.founder.ego.utils.RuleNormalUtils.saveConditionSet;

/**
 * @author yuzhiji
 */
@Service
@Slf4j
public class RuleEngineRuleSetServiceImpl implements RuleEngineRuleSetService {

    @Resource
    private RuleEngineRuleSetManager ruleEngineRuleSetManager;
    @Resource
    private RuleEngineRuleSetMapper ruleEngineRuleSetMapper;
    @Resource
    private RuleEngineRuleManager ruleEngineRuleManager;
    @Resource
    private RuleEngineRuleSetRulesManager ruleEngineRuleSetRulesManager;
    @Resource
    private RuleEngineVariableManager ruleEngineVariableManager;
    @Resource
    private RuleEngineElementManager ruleEngineElementManager;
    @Resource
    private RuleEngineRuleSetJsonManager ruleEngineRuleSetJsonManager;
    @Resource
    private RuleEngineLoadService ruleEngineLoadService;
    @Resource
    private RuleEngineConditionManager ruleEngineConditionManager;
    @Resource
    private RuleEngineSpecialRuleService ruleEngineSpecialRuleService;
    @Resource
    private RuleEngineVariableParamManager ruleEngineVariableParamManager;
    @Resource
    private RuleEngineRuleConditionGroupManager ruleEngineRuleConditionGroupManager;
    @Resource
    private RuleEngineConditionGroupConditionManager ruleEngineConditionGroupConditionManager;
    @Resource
    private RuleEngineRuleMultipleEnvExecutor ruleEngineRuleMultipleEnvExecutor;
    @Resource
    private RuleEngineEnviromentPublishHistoryManager ruleEngineEnviromentPublishHistoryManager;
    @Resource
    private RuleEngineAuthBizMemberDataManager ruleEngineAuthBizMemberDataManager;
    @Resource
    private RuleEngineRuleSetTestCaseManager ruleEngineRuleSetTestCaseManager;
    @Resource
    private RedissonClient redissonClient;
    @Resource
    private RuleEngineBizMemberDataService ruleEngineBizMemberDataService;
    @Resource
    private AuthService authService;
    @Resource
    private RuleEngineBizMemberService ruleEngineBizMemberService;
    @Resource
    private RuleEngineConditionServiceImpl ruleEngineConditionService;
    @Resource
    private RuleLockService ruleLockService;

    private static final String SUFFIX = "的默认规则";
    private static final String NORMAL_RULE = "普通规则";
    private static final String PRE_CONDITION = "前置条件";
    private static final String DRAFT = "draft";
    public static final Integer RULE_SIZE = 2;
    public static final Integer RIGHT_POS = 1;

    @Override
    public RuleSetConfig getRuleSet(Long ruleSetId) {
        RuleEngineRuleSet ruleEngineRuleSet = getRuleEngineRuleSet(ruleSetId);
        //get接口加锁
        ruleLockService.ruleSetLock(ruleEngineRuleSet, false);
        RuleEngineRuleSetJson ruleEngineRuleSetJson = ruleEngineRuleSetJsonManager.lambdaQuery().eq(
                RuleEngineRuleSetJson::getRuleSetId, ruleSetId).eq(RuleEngineRuleSetJson::getPublished, PublishEnum.DRAFT.getType()).one();

        if (Objects.isNull(ruleEngineRuleSetJson) /*|| ruleEngineRuleSet.getUpdateTime().compareTo(ruleEngineRuleSetJson.getUpdateTime()) > 0*/) {
            return new RuleSetConfig("RuleSet", getRuleSetById(new CommonIdParam(ruleSetId)));
        }
        return new RuleSetConfig("Draft", getDraft(ruleSetId));
    }

    @Override
    public RuleSetResult getRuleSetById(CommonIdParam commonIdParam) {
        RuleEngineRuleSet ruleEngineRuleSet = getRuleEngineRuleSet(commonIdParam.getId());
        RuleSetResult ruleSetResult = new RuleSetResult();
        ruleSetResult.setId(ruleEngineRuleSet.getId().intValue());
        ruleSetResult.setName(ruleEngineRuleSet.getName());
        ruleSetResult.setCode(ruleEngineRuleSet.getCode());
        ruleSetResult.setCreateStatus(ruleEngineRuleSet.getCreateStatus());
        ruleSetResult.setDescription(ruleEngineRuleSet.getDescription());
        ruleSetResult.setDefaultRulePolicy(ruleEngineRuleSet.getDefaultRulePolicy());
        ruleSetResult.setNodeRepetitionPolicy(ruleEngineRuleSet.getNodeRepetitionPolicy());
        ruleSetResult.setProcessRepetitionPolicy(ruleEngineRuleSet.getProcessRepetitionPolicy());
        ruleSetResult.setProcessRepetitionNodeName(ruleEngineRuleSet.getProcessRepetitionNodeName());
        ruleSetResult.setHitPolicy(ruleEngineRuleSet.getHitPolicy());
        ruleSetResult.setEditUserId(ruleEngineRuleSet.getEditUserId());
        ruleSetResult.setEditUserName(ruleEngineRuleSet.getEditUserName());

        //加入随机结果的返回
        RuleSetResult.RandomResult randomResult = new RuleSetResult.RandomResult();
        randomResult.setCount(ruleEngineRuleSet.getRandomCount());
        if (ruleEngineRuleSet.getRandomSwitch().equals(0)) {
            randomResult.setResultSwitch(false);
        } else {
            randomResult.setResultSwitch(true);
        }
        ruleSetResult.setRandomResult(randomResult);
        Long defaultRuleId = ruleEngineRuleSet.getDefaultRuleId();
        /*
         * 查询所需的规则
         */
        List<RuleEngineRuleSetRules> ruleEngineRuleSetRulesList = ruleEngineRuleSetRulesManager.lambdaQuery()
                .eq(RuleEngineRuleSetRules::getRuleSetId, commonIdParam.getId())
                .eq(RuleEngineRuleSetRules::getDeleted, ENABLE.getStatus()).list();
        Map<Long, RuleEngineRuleSetRules> ruleOrderMap = ruleEngineRuleSetRulesList.stream().collect(Collectors.toMap(
                RuleEngineRuleSetRules::getRuleId, a -> a, (k1, k2) -> k1));
        List<Long> ruleIdList = ruleEngineRuleSetRulesList.stream().map(RuleEngineRuleSetRules::getRuleId).collect(Collectors.toList());
        //拿到这个规则中用的所有的条件缓存起来
        RuleAllConditionInfo ruleSetAllConditionInfo = RuleNormalUtils.getRuleSetAllCondition(ruleIdList);
        //默认规则无条件
        ruleIdList.add(defaultRuleId);
        List<RuleEngineRule> ruleEngineRuleList = ruleEngineRuleManager.lambdaQuery().in(RuleEngineRule::getId, ruleIdList).list();
        List<RuleSetResult.SpecialRuleBean> specialRuleBeanList = new ArrayList<>();
        List<RuleSetResult.NormalRulesBean> normalRulesBeanList = new ArrayList<>();
        //前置条件
        PreConditionSet preCondition = new PreConditionSet();
        preCondition.setPolicy(ruleEngineRuleSet.getPreConditionPolicy());
        List<PreConditionSet.ConditionSet> conditionSets = new ArrayList<>();
        preCondition.setSet(conditionSets);
        for (RuleEngineRule ruleEngineRule : ruleEngineRuleList) {
            String conditionSetNames = ruleEngineRule.getConditionSetNames();
            List<PreConditionSetBean> preConditionSetBeanList = new ArrayList<>();
            if (StringUtils.isNotEmpty(conditionSetNames)) {
                List<String> preConditionSet = Arrays.stream(conditionSetNames.split(",")).collect(Collectors.toList());
                for (String name : preConditionSet) {
                    PreConditionSetBean preConditionSetBean = new PreConditionSetBean();
                    preConditionSetBean.setName(name);
                    preConditionSetBeanList.add(preConditionSetBean);
                }
            }
            if (ruleEngineRule.getId().equals(defaultRuleId)) {
                setDefaultRule(ruleSetResult, ruleEngineRule);
            } else if (ruleEngineRule.getRuleType().equals(RuleTypeEnum.SPECIAL.getStatus())) {
                /*特殊规则展示*/
                RuleSetResult.SpecialRuleBean specialRuleBean = new RuleSetResult.SpecialRuleBean();
                specialRuleBean.setSpecialRuleId(ruleEngineRule.getId().intValue());
                specialRuleBean.setSpecialRuleOrder(ruleOrderMap.get(ruleEngineRule.getId()).getRuleOrderNo().intValue());
                specialRuleBean.setSpecialRuleName(ruleEngineRule.getName());
                CommonIdParam idParam = new CommonIdParam();
                Long ruleId = ruleEngineRule.getId();
                idParam.setId(ruleId);
                specialRuleBean.setSpecialRuleResult(ruleEngineSpecialRuleService.getSpecialRuleById(idParam, ruleSetAllConditionInfo));
                specialRuleBean.setPreConditionSet(preConditionSetBeanList);
                specialRuleBeanList.add(specialRuleBean);
            } else if (ruleEngineRule.getRuleType().equals(RuleTypeEnum.NORMAL.getStatus())) {
                RuleSetResult.NormalRulesBean normalRulesBean = new RuleSetResult.NormalRulesBean();
                normalRulesBean.setNormalRuleOrder(ruleOrderMap.get(ruleEngineRule.getId()).getRuleOrderNo().intValue());
                Integer actionType = ruleEngineRule.getActionType();
                RuleSetResult.ActionVariableBean actionVariableBean =
                        new RuleSetResult.ActionVariableBean();
                actionVariableBean.setType(actionType);
                actionVariableBean.setValueType(ruleEngineRule.getActionDataType());
                setRuleVariableValue(ruleEngineRule, actionVariableBean, actionType);
                List<ConditionSetBean> conditionSetBeanList = RuleNormalUtils.getConditionSetBeans(ruleEngineRule, ruleSetAllConditionInfo);
                normalRulesBean.setConditionSet(conditionSetBeanList);
                conditionSetBeanList.sort(Comparator.comparing(ConditionSetBean::getConditionGroupOrder));
                normalRulesBean.setActionVariable(actionVariableBean);
                normalRulesBean.setPreConditionSet(preConditionSetBeanList);
                normalRulesBeanList.add(normalRulesBean);
            } else if (ruleEngineRule.getRuleType().equals(RuleTypeEnum.PRE_CONDITION_PRE.getStatus()) && Objects.equals(ruleEngineRuleSet.getPreConditionPolicy(), StatusEnum.VALID.getStatus())) {
                PreConditionSet.ConditionSet conditionSet = new PreConditionSet.ConditionSet();

                conditionSet.setName(ruleEngineRule.getName());
                conditionSet.setOrder(ruleOrderMap.get(ruleEngineRule.getId()).getRuleOrderNo().intValue());
                conditionSet.setScope(ruleEngineRule.getPreConditionScope());

                //set list对象
                List<ConditionSetBean> conditionSetBeanList = RuleNormalUtils.getConditionSetBeans(ruleEngineRule, ruleSetAllConditionInfo);
                conditionSetBeanList.sort(Comparator.comparing(ConditionSetBean::getConditionGroupOrder));

                conditionSet.setConditionSet(conditionSetBeanList);

                conditionSets.add(conditionSet);
            }
        }
        ruleSetResult.setPreConditionSet(preCondition);
        ruleSetResult.setSpecialRule(specialRuleBeanList);
        ruleSetResult.setNormalRules(normalRulesBeanList);
        return ruleSetResult;
    }

    @Override
    public RuleDefResult getDefById(CommonIdParam commonIdParam) {
        Long ruleId = commonIdParam.getId();
        ruleLockService.ruleSetValid(ruleId.intValue());
        RuleEngineRuleSet ruleEngineRuleSet = getRuleEngineRuleSet(ruleId);
        RuleDefResult ruleDefResult = new RuleDefResult();
        ruleDefResult.setCode(ruleEngineRuleSet.getCode());
        ruleDefResult.setDescription(ruleEngineRuleSet.getDescription());
        ruleDefResult.setId(Integer.parseInt(ruleEngineRuleSet.getId() + ""));
        ruleDefResult.setName(ruleEngineRuleSet.getName());
        return ruleDefResult;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public CommonIdResult addRuleSet(RuleSetEditRequest ruleSetEditRequest) {
        Integer ruleSetId = ruleSetEditRequest.getId();
        //校验数据权限
        if (ruleSetId != null) {
            //先校验是否被别人锁定了
            ruleLockService.ruleSetValid(ruleSetId);
            ruleEngineBizMemberDataService.verifyDataPermission(ruleSetId, DataTypeEnum.RULESET,
                    "rule-6-1-2", "规则平台/规则/编辑规则");
        } else {
            ruleEngineBizMemberDataService.verifyDataPermission(ruleSetId, DataTypeEnum.RULESET,
                    "rule-6-1-1", "规则平台/规则/新建规则");
        }

        /* 获取业务组 */
        RuleEngineBizBean engineBiz = RuleEngineBizServiceImpl.getEngineBiz();
        List<RuleEngineRuleSet> ruleSetEditRequests = ruleEngineRuleSetManager.lambdaQuery()
                .eq(RuleEngineRuleSet::getDeleted, ENABLE.getStatus()).eq(RuleEngineRuleSet::getBizCode, engineBiz.getBizCode())
                .and(q -> q.eq(RuleEngineRuleSet::getCode, ruleSetEditRequest.getCode()).or()
                        .eq(RuleEngineRuleSet::getName, ruleSetEditRequest.getName()))
                .list();
        if (ruleSetId != null) {
            /*如果是更新规则集*/
            if (CollUtil.isNotEmpty(ruleSetEditRequests) && ruleSetEditRequests.get(0).getId() != ruleSetId.longValue()) {
                throw new ValidException( "存在重复RuleSet");
            }
        } else {
            /*如果是新增规则集*/
            if (CollUtil.isNotEmpty(ruleSetEditRequests)) {
                throw new ValidException( "存在重复RuleSet");
            }
        }
        RuleEngineRuleSet ruleEngineRuleSet = ruleEngineRuleSetManager.getById(ruleSetId);
        ManageUser loginedUser = WfcRuleContext.getLoginedUser();
        if (ruleEngineRuleSet == null) {
            ruleEngineRuleSet = new RuleEngineRuleSet();
            ruleEngineRuleSet.setBizCode(engineBiz.getBizCode());
            ruleEngineRuleSet.setBizId(engineBiz.getId());
            ruleEngineRuleSet.setBizName(engineBiz.getBizName());
            ruleEngineRuleSet.setCreateUserId(loginedUser.getEmployeeId());
            ruleEngineRuleSet.setCreateUserName(loginedUser.getEmployeeName());
        } else {
            ruleEngineRuleSet.setUpdateTime(null);
            ruleEngineRuleSet.setUpdateUserId(loginedUser.getEmployeeId());
            ruleEngineRuleSet.setUpdateUserName(loginedUser.getEmployeeName());
        }
        /*存储规则定义信息*/
        ruleEngineRuleSet.setCode(ruleSetEditRequest.getCode());
        ruleEngineRuleSet.setName(ruleSetEditRequest.getName());
        ruleEngineRuleSet.setDescription(ruleSetEditRequest.getDescription());
        ruleEngineRuleSet.setCodeName(ruleSetEditRequest.getCode() + ruleSetEditRequest.getName());
        /* 判断是新增规则集还是更新规则集*/
        if (ruleSetId == null) {
            /* 新增规则集 */
            try {
                /* 草稿状态 */
                ruleEngineRuleSet.setCreateStatus(PublishEnum.DRAFT.getType());
                ruleEngineRuleSetManager.save(ruleEngineRuleSet);
            } catch (Exception e) {
                log.error("新增规则集");
                throw new ValidException(SERVICE_ERROR.code, "新增规则集错误");
            }
        } else {
            /* 如果是创建中， 需要更新*/
            Integer publishCount = ruleEngineRuleSetJsonManager.lambdaQuery()
                    .eq(RuleEngineRuleSetJson::getRuleSetId, ruleSetId).
                            in(RuleEngineRuleSetJson::getPublished, Arrays.asList(PublishEnum.PUBLISH.getType(), PublishEnum.PUBLISH_OTHER_ENV.getType()))
                    .eq(RuleEngineRuleSetJson::getIsCurEnv, RuleEnvironmentEnum.CURRENT_ENV.getValue()).count();
            if (ruleEngineRuleSet.getCreateStatus().equals(PublishEnum.DRAFT.getType()) && publishCount == 0) {
                ruleEngineRuleSetManager.updateById(ruleEngineRuleSet);
            } else {
                /* 如果有一条已发布和一条待发布的， 需要更新*/
                Integer waitingPublishCount = ruleEngineRuleSetJsonManager.lambdaQuery().eq(RuleEngineRuleSetJson::getRuleSetId, ruleSetId).
                        eq(RuleEngineRuleSetJson::getPublished, WAITING_PUBLISH.getType()).count();
                if (waitingPublishCount == 1) {
                    ruleEngineRuleSetManager.updateById(ruleEngineRuleSet);
                    ruleEngineLoadService.createRuleSetJson(ruleSetId, RuleSetUpdateSourceEnum.RULE_SET);
                } else {
                    /* 更新规则集不马上更新*/
                    RuleSetDefDraft ruleSetDefDraft = new RuleSetDefDraft();
                    ruleSetDefDraft.setId(ruleSetEditRequest.getId());
                    ruleSetDefDraft.setCode(ruleSetEditRequest.getCode());
                    ruleSetDefDraft.setDescription(ruleSetEditRequest.getDescription());
                    ruleSetDefDraft.setName(ruleSetEditRequest.getName());
                    redissonClient.getBucket(ruleSetId + DRAFT).set(ruleSetDefDraft);
                }
            }
        }
        /*
         * 新增或更新规则集
         */
        CommonIdResult commonIdResult = new CommonIdResult();
        commonIdResult.setId(ruleEngineRuleSet.getId());
        return commonIdResult;
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public Boolean updateRuleSet(RuleSetEditRequest ruleSetEditRequest) {
        Integer id = ruleSetEditRequest.getId();
        //解锁，先校验是否被别人锁定了
        ruleLockService.ruleSetUnLock(id);

        RuleEngineRuleSet ruleEngineRuleSet = ruleEngineRuleSetManager.getById(id);
        /*
         * 获取更新后的信息
         */
        if (ruleEngineRuleSet.getDeleted().equals(DISABLE.getStatus())) {
            throw new ValidException( "该规则集已经被删除，无法被更新");
        }
        checkCodeRepeat(ruleSetEditRequest.getCode(), ruleEngineRuleSet.getId());
        getRuleSetInfo(ruleSetEditRequest, ruleEngineRuleSet);


        ruleEngineRuleSet.setUpdateTime(null);
        ruleEngineRuleSet.setUpdateUserId(WfcRuleContext.getLoginedUser().getEmployeeId());
        ruleEngineRuleSet.setUpdateUserName(WfcRuleContext.getLoginedUser().getEmployeeName());

        /*
         *  更新默认规则，使用新的默认规则(由于id变了，没有删除老的默认规则)
         **/
        saveDefaultRule(ruleSetEditRequest, ruleEngineRuleSet);
        /*
         * 查询所有老的规则集和规则的对应关系，并逻辑删除
         */
        List<RuleEngineRuleSetRules> ruleEngineRuleSetRules = ruleEngineRuleSetRulesManager.lambdaQuery().eq(RuleEngineRuleSetRules::getDeleted, ENABLE.getStatus())
                .eq(RuleEngineRuleSetRules::getRuleSetId, id).list();
        List<Long> deleteIdList = ruleEngineRuleSetRules.stream().map(RuleEngineRuleSetRules::getId).collect(Collectors.toList());
        if (CollectionUtil.isNotEmpty(deleteIdList)) {
            try {
                ruleEngineRuleSetRulesManager.removeByIds(deleteIdList);
            } catch (Exception e) {
                log.error("批量逻辑删除旧规则错误");
                throw new ValidException(SERVICE_ERROR.code, "批量逻辑删除旧规则错误");
            }
        }
        /* 查询ruleSetId是否有对应的待发布版本 */
        List<RuleEngineRuleSetJson> ruleEngineRuleSetJsons = ruleEngineRuleSetJsonManager.lambdaQuery().eq(RuleEngineRuleSetJson::getRuleSetId,
                ruleEngineRuleSet.getId()).eq(RuleEngineRuleSetJson::getPublished, WAITING_PUBLISH.getType()).
                eq(RuleEngineRuleSetJson::getDeleted, ENABLE.getStatus()).list();

        /* 如果没有待发布版本 进行定义更新*/
        if (CollUtil.isEmpty(ruleEngineRuleSetJsons)) {
            RuleSetDefDraft ruleSet = (RuleSetDefDraft) redissonClient.getBucket(id + DRAFT).get();
            /* ruleSet为空，说明已经更新过了*/
            if (ruleSet != null) {
                ruleEngineRuleSet.setName(ruleSet.getName());
                ruleEngineRuleSet.setDescription(ruleSet.getDescription());
            }
        }
        //如果撤销时
        if (ruleSetEditRequest.isRuleCancel()) {
            ruleEngineRuleSet.setName(ruleSetEditRequest.getName());
            ruleEngineRuleSet.setDescription(ruleSetEditRequest.getDescription());
        }
        //保存前置条件
        savePreConditionSet(ruleSetEditRequest, ruleEngineRuleSet);
        try {
            ruleEngineRuleSet.setCodeName(ruleEngineRuleSet.getCode() + ruleEngineRuleSet.getName());
            //mybatis plus忽略了对null的处理，但是有的字段想设置为null，无法设置
            ruleEngineRuleSetMapper.updateAllRuleSetColumnById(ruleEngineRuleSet);
        } catch (Exception e) {
            log.error("更新规则集");
            throw new ValidException(SERVICE_ERROR.code, "更新规则集错误");
        }
        //保存基础规则与特殊规则
        saveSpecialAndNormalRule(ruleSetEditRequest, ruleEngineRuleSet);
        /*
         * 删除规则草稿
         *
         **/
        RuleEngineRuleSetJson ruleEngineRuleSetJson = ruleEngineRuleSetJsonManager.lambdaQuery().eq(
                RuleEngineRuleSetJson::getRuleSetId, id).eq(RuleEngineRuleSetJson::getPublished,
                PublishEnum.DRAFT.getType()).one();
        if (ruleEngineRuleSetJson != null) {
            ruleEngineRuleSetJsonManager.removeById(ruleEngineRuleSetJson.getId());
        }

        //生成新的json数据
        ruleEngineLoadService.createRuleSetJson(id, RuleSetUpdateSourceEnum.RULE_SET);

        if (CollUtil.isNotEmpty(ruleEngineRuleSetJsons)) {
            Wrapper<RuleEngineRuleSetJson> wrapper = new QueryWrapper<RuleEngineRuleSetJson>()
                    .eq("published", PublishEnum.DRAFT.getType()).eq("rule_set_id", id);
            ruleEngineRuleSetJsonManager.remove(wrapper);
        }
        return true;
    }

    /**
     * 保存前置条件
     *
     * @param ruleSetEditRequest 请求数据
     * @param ruleEngineRuleSet  ruleSet
     */
    private void savePreConditionSet(RuleSetEditRequest ruleSetEditRequest, RuleEngineRuleSet ruleEngineRuleSet) {
        PreConditionSet conditionSet = ruleSetEditRequest.getPreConditionSet();
        //前置条件集策略 0关闭 1 开启
        if (conditionSet == null) {
            ruleEngineRuleSet.setPreConditionPolicy(RuleSetConditionSet.POLICY_DISABLE);
            return;
        }
        ruleEngineRuleSet.setPreConditionPolicy(conditionSet.getPolicy());
        //禁用
        if (conditionSet.getPolicy().equals(StatusEnum.INVALID.getStatus())) {
            return;
        }
        List<RuleEngineRuleSetRules> ruleEngineRuleSetRulesList = new ArrayList<>();
        List<PreConditionSet.ConditionSet> conditionSetList = conditionSet.getSet();
        ManageUser loginedUser = WfcRuleContext.getLoginedUser();
        RuleEngineBizBean engineBiz = RuleEngineBizServiceImpl.getEngineBiz();
        for (PreConditionSet.ConditionSet set : conditionSetList) {
            RuleEngineRule ruleEngineRule = new RuleEngineRule();
            ruleEngineRule.setName(set.getName());
            ruleEngineRule.setPreConditionScope(set.getScope());
            ruleEngineRule.setShowed(DISABLE.getStatus());
            ruleEngineRule.setRuleType(RuleTypeEnum.PRE_CONDITION_PRE.getStatus());
            ruleEngineRule.setActionType(CONSTANT.getStatus());
            //创建人
            ruleEngineRule.setCreateUserName(loginedUser.getEmployeeName());
            ruleEngineRule.setCreateUserId(loginedUser.getEmployeeId());
            //业务组
            ruleEngineRule.setBizName(engineBiz.getBizName());
            ruleEngineRule.setBizCode(engineBiz.getBizCode());
            ruleEngineRule.setBizId(engineBiz.getId());

            //先保存，获取到自增id
            ruleEngineRuleManager.save(ruleEngineRule);
            //条件
            List<ConditionSetBean> beanList = set.getConditionSet();
            RuleNormalUtils.saveConditionSet(beanList, ruleSetEditRequest.getName() + PRE_CONDITION, ruleEngineRule.getId());
            //创建ruleSet_Rule关联实体
            RuleEngineRuleSetRules ruleEngineRuleSetRules = new RuleEngineRuleSetRules();
            ruleEngineRuleSetRules.setRuleId(ruleEngineRule.getId());
            ruleEngineRuleSetRules.setRuleSetId(ruleEngineRuleSet.getId());
            ruleEngineRuleSetRules.setRuleOrderNo(Long.valueOf(set.getOrder()));
            ruleEngineRuleSetRulesList.add(ruleEngineRuleSetRules);
        }
        ruleEngineRuleSetRulesManager.saveBatch(ruleEngineRuleSetRulesList);
    }


    @Override
    @UpdateRuleEngine
    @Transactional(rollbackFor = Exception.class)
    public Boolean switches(RuleSetSwitchRequest ruleSetSwitchRequest) {
        Long ruleSetId = ruleSetSwitchRequest.getId();
        RuleEngineRuleSet ruleEngineRuleSet = getRuleEngineRuleSet(ruleSetId);
        ruleEngineRuleSet.setStatus(ruleSetSwitchRequest.getStatus());
        try {
            ruleEngineRuleSetManager.updateById(ruleEngineRuleSet);
        } catch (Exception e) {
            log.error("id为{0}的规则集更新失败");
            throw new ValidException(SERVICE_ERROR.code, "更新规则集失败");
        }
        return true;
    }

    @Override
    @UpdateRuleEngine
    @Transactional(rollbackFor = Exception.class)
    public Boolean deleteRuleSet(CommonIdParam commonIdParam) {
        Long ruleSetId = commonIdParam.getId();
        RuleEngineRuleSet ruleEngineRuleSet = getRuleEngineRuleSet(ruleSetId);
        ruleEngineRuleSet.setDeleted(DISABLE.getStatus());
        /*
         * 更新rule_set状态
         */
        try {
            ruleEngineRuleSetManager.updateById(ruleEngineRuleSet);
            //删除已授权的相关数据 rule_engine_auth_biz_member_data type=0 的数据
            LambdaQueryWrapper<RuleEngineAuthBizMemberData> dataLambdaQueryWrapper = new LambdaQueryWrapper<>();
            dataLambdaQueryWrapper.eq(RuleEngineAuthBizMemberData::getDataType, DataTypeEnum.RULESET.getDataType())
                    .eq(RuleEngineAuthBizMemberData::getBizCode, ruleEngineRuleSet.getBizCode())
                    .eq(RuleEngineAuthBizMemberData::getDataCode, ruleEngineRuleSet.getCode());
            ruleEngineAuthBizMemberDataManager.remove(dataLambdaQueryWrapper);
            /*
             * 更新rule_set_rules状态
             */
            List<RuleEngineRuleSetRules> ruleEngineRuleSetRuleList =
                    ruleEngineRuleSetRulesManager.lambdaQuery().eq(RuleEngineRuleSetRules::getRuleSetId, commonIdParam.getId())
                            .eq(RuleEngineRuleSetRules::getDeleted, ENABLE.getStatus()).list();
            for (RuleEngineRuleSetRules ruleEngineRuleSetRules : ruleEngineRuleSetRuleList) {
                ruleEngineRuleSetRules.setDeleted(DISABLE.getStatus());
            }
            if (CollUtil.isNotEmpty(ruleEngineRuleSetRuleList)) {
                ruleEngineRuleSetRulesManager.updateBatchById(ruleEngineRuleSetRuleList);
            }
        } catch (Exception e) {
            log.error("id为{0}的规则集删除失败");
            throw new ValidException(SERVICE_ERROR.code, "删除规则集失败");
        }
        //一个事务bug导致，修改为
        List<RuleEngineRuleSetJson> list = ruleEngineRuleSetJsonManager.lambdaQuery().eq(RuleEngineRuleSetJson::getRuleSetId, ruleSetId)
                .eq(RuleEngineRuleSetJson::getIsCurEnv, RuleEnvironmentEnum.CURRENT_ENV.getValue()).list();
        Set<Long> collect = list.stream().map(RuleEngineRuleSetJson::getId).collect(Collectors.toSet());
        if (CollUtil.isNotEmpty(collect)) {
            ruleEngineRuleSetJsonManager.removeByIds(collect);
        }
        redissonClient.getBucket(ruleSetId + DRAFT).delete();

        //删除信息
        RemoveRuleInfo removeRuleInfo = new RemoveRuleInfo();
        removeRuleInfo.setRuleId(ruleSetId);
        removeRuleInfo.setBizCode(ruleEngineRuleSet.getBizCode());
        removeRuleInfo.setRuleCode(ruleEngineRuleSet.getCode());

        //删除json
        return ruleEngineRuleMultipleEnvExecutor.removeRule(removeRuleInfo);
    }

    @Override
    public Boolean validateUniqCode(CommonCodeParam commonCodeParam) {
        String bizCode = RuleEngineBizServiceImpl.getEngineBiz().getBizCode();
        int count = ruleEngineRuleSetManager.lambdaQuery()
                .eq(RuleEngineRuleSet::getDeleted, ENABLE.getStatus()).eq(RuleEngineRuleSet::getBizCode, bizCode)
                .eq(RuleEngineRuleSet::getCode, commonCodeParam.getCode()).count();
        //如果在规则中已经存在直接return false，否则去看是否在决策表中存在
        if (count != 0) {
            return true;
        }
        //再校验决策表中是否存在
        Integer count1 = DB.lQuery(RuleEngineDecision.class)
                .eq(RuleEngineDecision::getDeleted, ENABLE.getStatus()).eq(RuleEngineDecision::getBizCode, bizCode)
                .eq(RuleEngineDecision::getCode, commonCodeParam.getCode()).count();
        return count1 != 0;
    }

    @Override
    public Boolean validateUniqName(CommonNameParam commonNameParam) {
        String bizCode = RuleEngineBizServiceImpl.getEngineBiz().getBizCode();
        int count = ruleEngineRuleSetManager.lambdaQuery()
                .eq(RuleEngineRuleSet::getDeleted, ENABLE.getStatus()).eq(RuleEngineRuleSet::getBizCode, bizCode)
                .eq(RuleEngineRuleSet::getName, commonNameParam.getName()).count();
        //如果在规则中已经存在直接return false，否则去看是否在决策表中存在
        if (count != 0) {
            return true;
        }
        Integer count1 = DB.lQuery(RuleEngineDecision.class)
                .eq(RuleEngineDecision::getDeleted, ENABLE.getStatus()).eq(RuleEngineDecision::getBizCode, bizCode)
                .eq(RuleEngineDecision::getName, commonNameParam.getName()).count();
        //再校验决策表中是否存在
        return count1 != 0;
    }

    @Override
    public PageResult<RuleSetResponse> listRuleSet(PageRequest<RuleQueryParam> pageRequest) {
        RuleQueryParam ruleSetRequest = pageRequest.getQuery();
        PageBase page = pageRequest.getPage();
        String name = ruleSetRequest.getName();
        String code = ruleSetRequest.getCode();
        Integer state = ruleSetRequest.getState();
        //来源 0是我创建的，1是授权给我的(rule_engine_auth_biz_member_data)，默认是我创建的
        Integer source = ruleSetRequest.getSource();
        List<PageRequest.OrderBy> orders = pageRequest.getOrders();
        QueryWrapper<RuleEngineRuleSet> queryWrapper = new QueryWrapper<>();
//        if (Validator.isNotEmpty(name) && name.equals(code)) {
//            queryWrapper.lambda().like(RuleEngineRuleSet::getCodeName, code);
//        } else {
        if (Validator.isNotEmpty(code)) {
            queryWrapper.lambda().like(RuleEngineRuleSet::getCode, code);
        }
        if (Validator.isNotEmpty(name)) {
            queryWrapper.lambda().like(RuleEngineRuleSet::getName, name);
        }
        //}
        /*按状态进行查询*/
        if (state != null) {
            if (state.equals(PublishEnum.PUBLISH.getType())) {
                queryWrapper.lambda().in(RuleEngineRuleSet::getCreateStatus, PublishEnum.PUBLISH.getType(), PublishEnum.PUBLISH_OTHER_ENV.getType());
            } else {
                queryWrapper.lambda().eq(RuleEngineRuleSet::getCreateStatus, state);
            }
        }
        ManageUser loginedUser = WfcRuleContext.getLoginedUser();
        String loginUserId = loginedUser.getEmployeeId();

        //0是我创建的，1是授权给我的
        if (source != null) {
            if (source == 1) {
                if (authService.isSystemAdmin(loginUserId) || ruleEngineBizMemberService.isBizGroupAdmin(loginUserId)) {
                    //当用户为平台管理员或者业务组管理员时，授权给我的规则应该展示当前所在业务组下所有有效的规则
                    queryWrapper.lambda().eq(RuleEngineRuleSet::getBizCode, RuleEngineBizServiceImpl.getEngineBiz().getBizCode());
                } else {
                    //授权给我+我自己创建+业务组内全员可见
                    Set<Long> ids = new HashSet<>();
                    List<RuleEngineAuthBizMemberData> dataIds = ruleEngineBizMemberDataService.getByDataType(DataTypeEnum.RULESET.getDataType());
                    if (CollUtil.isNotEmpty(dataIds)) {
                        Set<Long> toMeIds = dataIds.stream().map(RuleEngineAuthBizMemberData::getDataId).map(Long::valueOf).collect(Collectors.toSet());
                        ids.addAll(toMeIds);
                    }
                    //业务组内全员可见 or 自己创建的
                    List<RuleEngineRuleSet> authShowAllIds = ruleEngineRuleSetManager.
                            list(new LambdaQueryWrapper<RuleEngineRuleSet>().eq(RuleEngineRuleSet::getBizCode, RuleEngineBizServiceImpl.getEngineBiz().getBizCode())
                                    .and(w -> w.eq(RuleEngineRuleSet::getAuthShowAll, ENABLE.getStatus()).or().eq(RuleEngineRuleSet::getCreateUserId, loginUserId)));
                    if (CollUtil.isNotEmpty(authShowAllIds)) {
                        Set<Long> showAllIds = authShowAllIds.stream().map(RuleEngineRuleSet::getId).collect(Collectors.toSet());
                        ids.addAll(showAllIds);
                    }
                    if (CollUtil.isNotEmpty(ids)) {
                        //授权给我的规则 or 全员可见的也要展示 or 自己创建
                        queryWrapper.lambda().in(RuleEngineRuleSet::getId, ids);
                    } else {
                        return new PageResult<>();
                    }
                }

            } else {
                queryWrapper.lambda().eq(RuleEngineRuleSet::getCreateUserId, loginUserId)
                        .eq(RuleEngineRuleSet::getBizCode, RuleEngineBizServiceImpl.getEngineBiz().getBizCode());
            }


        } else {
            //source为null查询所在业务组内数据
            queryWrapper.lambda().eq(RuleEngineRuleSet::getBizCode, RuleEngineBizServiceImpl.getEngineBiz().getBizCode());
        }
        /* 排序 */
        PageUtils.defaultOrder(orders, queryWrapper);
        queryWrapper.lambda().eq(RuleEngineRuleSet::getDeleted, ENABLE.getStatus());
        IPage<RuleEngineRuleSet> pageInfo = ruleEngineRuleSetManager
                .page(new Page<>(page.getPageIndex(), page.getPageSize()), queryWrapper);
        List<RuleEngineRuleSet> tagData = pageInfo.getRecords();
        if (CollUtil.isEmpty(tagData)) {
            return new PageResult<>();
        }
        List<Long> ruleSetIds = tagData.stream().map(RuleEngineRuleSet::getId).collect(Collectors.toList());
        List<RuleEngineEnviromentPublishHistory> publishHistoryList = ruleEngineEnviromentPublishHistoryManager.lambdaQuery()
                .in(RuleEngineEnviromentPublishHistory::getRuleSetId, ruleSetIds).list();
        Map<Long, List<RuleEngineEnviromentPublishHistory>> publishHistoryGroup = new HashMap<>();
        if (CollUtil.isNotEmpty(publishHistoryList)) {
            publishHistoryGroup = publishHistoryList.stream().collect(Collectors.groupingBy(RuleEngineEnviromentPublishHistory::getRuleSetId));
        }
        Map<Long, List<RuleEngineEnviromentPublishHistory>> finalPublishHistoryGroup = publishHistoryGroup;

        List<RuleEngineRuleSetJson> ruleSetJsonList = ruleEngineRuleSetJsonManager.lambdaQuery()
                .eq(RuleEngineRuleSetJson::getIsCurEnv, RuleEnvironmentEnum.CURRENT_ENV.getValue())
                .in(RuleEngineRuleSetJson::getRuleSetId, ruleSetIds).list();
        Map<Long, List<RuleEngineRuleSetJson>> ruleSetJsonGroup = new HashMap<>();
        if (CollUtil.isNotEmpty(ruleSetJsonList)) {
            ruleSetJsonGroup = ruleSetJsonList.stream().collect(Collectors.groupingBy(RuleEngineRuleSetJson::getRuleSetId));
        }
        Map<Long, List<RuleEngineRuleSetJson>> finalRuleSetJsonGroup = ruleSetJsonGroup;

        List<RuleSetResponse> specialRuleListResults = tagData.stream().map(ruleEngineRuleSet -> {
            RuleSetResponse ruleSetResponse = new RuleSetResponse();
            ruleSetResponse.setId(ruleEngineRuleSet.getId().intValue());
            ruleSetResponse.setCreateStatus(ruleEngineRuleSet.getCreateStatus());
            ruleSetResponse.setCode(ruleEngineRuleSet.getCode());
            ruleSetResponse.setName(ruleEngineRuleSet.getName());
            ruleSetResponse.setDescription(ruleEngineRuleSet.getDescription());
            ruleSetResponse.setStatus(ruleEngineRuleSet.getStatus());
            ruleSetResponse.setPreparedVersion(ruleEngineRuleSet.getPreparedVersion());
            ruleSetResponse.setPublishVersion(ruleEngineRuleSet.getPublishVersion());
            ruleSetResponse.setAuthShowAll(ruleEngineRuleSet.getAuthShowAll());
            ruleSetResponse.setEditUserId(ruleEngineRuleSet.getEditUserId());
            ruleSetResponse.setEditUserName(ruleEngineRuleSet.getEditUserName());

            /*默认跳规则定义*/
            ruleSetResponse.setRedirect(RULE_DEF.getStatus());
            /*
             * ruleVersion规则版本list,左边为最高环境的环境+版本 prd>pre>test
             *
             * 右边为版本最高的环境+版本
             */
            List<RuleVersion> ruleVersions = new ArrayList<>();

            /* 判断右边版本是否经过变动 */
            boolean isChange = false;
            List<RuleEngineEnviromentPublishHistory> ruleEngineRuleSetJsons = finalPublishHistoryGroup.get(ruleEngineRuleSet.getId());
            if (CollectionUtil.isNotEmpty(ruleEngineRuleSetJsons)) {
                /*
                 * 先获取最高环境版本
                 */
                CollectionUtil.sort(ruleEngineRuleSetJsons, (RuleEngineEnviromentPublishHistory o1,
                                                             RuleEngineEnviromentPublishHistory o2) -> {
                    if (!o1.getPriority().equals(o2.getPriority())) {
                        return o1.getPriority() - o2.getPriority();
                    } else {
                        /* 最高版本环境相同，取最新版本*/
                        return compareVersion(o1, o2);
                    }
                });

                /* 获取最新版本加入ruleVersionList, 如何多个最新版本，取最高版本*/
                RuleVersion ruleVersion = new RuleVersion();
                ruleVersion.setVersion(ruleEngineRuleSetJsons.get(0).getRuleSetVersion());
                ruleVersion.setEnv(ruleEngineRuleSetJsons.get(0).getEnviromentName().toUpperCase());
                ruleVersions.add(ruleVersion);
                /*
                 * 再取最新版本
                 */
                CollectionUtil.sort(ruleEngineRuleSetJsons, (RuleEngineEnviromentPublishHistory o1,
                                                             RuleEngineEnviromentPublishHistory o2) -> {
                    if (compareVersion(o1, o2) != 0) {
                        return compareVersion(o1, o2);
                    } else {
                        /* 最高版本环境相同，取最新版本*/
                        return o1.getPriority() - o2.getPriority();
                    }
                });


                /*获取最高版本加入VersionList*/
                RuleEngineEnviromentPublishHistory newVersion = ruleEngineRuleSetJsons.get(0);
                /* 如果左边同时是最高版本又是最新版本，那么右边需要取第二个version */
                if (ruleEngineRuleSetJsons.size() >= RULE_SIZE) {
                    if (newVersion.getEnviromentName().equalsIgnoreCase(ruleVersion.getEnv())
                            && newVersion.getRuleSetVersion().equals(ruleVersion.getVersion())) {
                        isChange = true;
                        newVersion = ruleEngineRuleSetJsons.get(1);
                    }
                }
                RuleVersion newRuleVersion = new RuleVersion();
                newRuleVersion.setEnv(newVersion.getEnviromentName().toUpperCase());
                newRuleVersion.setVersion(newVersion.getRuleSetVersion());
                ruleVersions.add(newRuleVersion);
            }
            /* 查看有没有ruleSetJson*/
            List<RuleEngineRuleSetJson> ruleSetList = finalRuleSetJsonGroup.get(ruleEngineRuleSet.getId());
            if (CollectionUtil.isNotEmpty(ruleSetList)) {
                List<RuleEngineRuleSetJson> wait = ruleSetList.stream().filter(e -> e.getPublished().equals(WAITING_PUBLISH.getType())).collect(Collectors.toList());
                if (CollectionUtil.isNotEmpty(wait)) {
                    String waitingVersion = wait.get(0).getRuleVersion();
                    RuleVersion ruleVersion = new RuleVersion();
                    ruleVersion.setVersion(waitingVersion);
                    ruleVersion.setEnv("WAIT");
                    if (CollectionUtil.isEmpty(ruleVersions)) {
                        /* 如果ruleVersions空，直接加入ruleVersions*/
                        ruleVersions.add(ruleVersion);
                    } else {
                        /* 取list里的最后一个元素, 查看待发布版本是否和最新版本相同, 不相同则替换 */
                        if (!waitingVersion.equals(ruleVersions.get(ruleVersions.size() - 1)
                                .getVersion())) {
                            if (ruleVersions.size() == RULE_SIZE) {
                                /* 如果只有两个规则版本 */
                                ruleVersions.set(ruleVersions.size() - 1, ruleVersion);
                            } else {
                                /* 如果只有一个最高规则版本 */
                                ruleVersions.add(ruleVersion);
                            }
                        }

                    }
                    /*
                     * 查看是否有待发布版本，如果有，跳规则预览。
                     * 如果没有，看是否发过prd,发过，跳规则定义，没有，跳规则预览。
                     * 没有待发布和已发布，跳规则定义。
                     */
                    /* 有待发布，跳规则预览*/
                    ruleSetResponse.setRedirect(RULE_VIEW.getStatus());
                } else {
                    /*看是否有test和pre的*/
                    if (CollectionUtil.isNotEmpty(ruleVersions)) {
                        /* 即有且没有prd */
                        boolean hasPrd = false;
                        /* 如果右侧版本没有动，看右侧是否是prd */
                        if (!isChange) {
                            if (ruleVersions.get(ruleVersions.size() - 1).getEnv()
                                    .equalsIgnoreCase(PRD.getName())) {
                                hasPrd = true;
                            }
                        } else {
                            /* 右侧版本变了，看左侧是否是prd */
                            if (ruleVersions.get(0).getEnv().equalsIgnoreCase(PRD.getName())) {
                                hasPrd = true;
                            }
                        }
                        if (!hasPrd) {
                            /* 没有prd的发布，跳规则预览*/
                            ruleSetResponse.setRedirect(RULE_VIEW.getStatus());
                        }
                    }
                }
            }
            /*
             * 如果最新版本和最高版本同时存在，看左右env + version是否相同,相同删除一个。
             */
            if (ruleVersions.size() == RULE_SIZE) {
                RuleVersion left = ruleVersions.get(0);
                RuleVersion right = ruleVersions.get(1);
                if (left.getEnv().equalsIgnoreCase(right.getEnv()) && left.getVersion().equals(right.getVersion())) {
                    ruleVersions.remove(RIGHT_POS.intValue());
                } else if (compareVersionReal(left.getVersion(), right.getVersion()) < 0) {
                    ruleVersions.remove(RIGHT_POS.intValue());
                }
            }
            /* 如果是编辑中，直接跳规则定义 */
            if (ruleEngineRuleSet.getCreateStatus().equals(PublishEnum.DRAFT.getType())) {
                ruleSetResponse.setRedirect(RULE_DEF.getStatus());
            }
            ruleSetResponse.setRuleVersion(ruleVersions);
            return ruleSetResponse;
        }).collect(Collectors.toList());

        PageResult<RuleSetResponse> result = new PageResult<>();
        result.setData(new Rows<>(specialRuleListResults,
                new PageResponse(page.getPageIndex(), page.getPageSize(), pageInfo.getTotal())));
        return result;
    }

    private int compareVersion(RuleEngineEnviromentPublishHistory o1, RuleEngineEnviromentPublishHistory o2) {
        /*比较版本大小 v1.0和v1.1*/
        String version1 = o1.getRuleSetVersion();
        String version2 = o2.getRuleSetVersion();
        /* 先去除v*/
        return compareVersionReal(version1, version2);
    }

    private int compareVersionReal(String version1, String version2) {
        version1 = version1.substring(1);
        version2 = version2.substring(1);
        /* 按点分割，获得大版本和小版本*/
        String[] versions1 = StringUtils.split(version1, ".");
        String[] versions2 = StringUtils.split(version2, ".");
        Integer bigVersion1 = Integer.valueOf(versions1[0]);
        Integer bigVersion2 = Integer.valueOf(versions2[0]);
        Integer smallVersion1 = Integer.valueOf(versions1[1]);
        Integer smallVersion2 = Integer.valueOf(versions2[1]);
        /*先比大版本*/
        if (!bigVersion1.equals(bigVersion2)) {
            return bigVersion2 - bigVersion1;
        } else {
            /*相同再比小版本*/
            return smallVersion2 - smallVersion1;
        }
    }

    @Override
    public Boolean saveDraft(RuleSetSaveDraftRequest ruleSetEditRequest) {

        /*
          存储草稿
        */
        Integer ruleSetId = ruleSetEditRequest.getId();
        //先校验是否被别人锁定了
        ruleLockService.ruleSetValid(ruleSetId);
        RuleEngineRuleSet ruleEngineRuleSet = ruleEngineRuleSetManager.lambdaQuery().eq(RuleEngineRuleSet::getId,
                ruleSetId).one();

        RuleEngineRuleSetJson ruleEngineRuleSetJson = new RuleEngineRuleSetJson();
        ruleEngineRuleSetJson.setRuleSetId(ruleSetId.longValue());
        ruleEngineRuleSetJson.setRuleVersion("0");
        ruleEngineRuleSetJson.setPublished(PublishEnum.DRAFT.getType());
        ruleEngineRuleSetJson.setBizId(ruleEngineRuleSet.getBizId().intValue());
        ruleEngineRuleSetJson.setBizCode(ruleEngineRuleSet.getBizCode());
        ruleEngineRuleSetJson.setBizName(ruleEngineRuleSet.getBizName());
        ruleEngineRuleSetJson.setRuleSetCode(ruleEngineRuleSet.getCode());
        ruleEngineRuleSetJson.setRuleSetJson(JsonUtils.toJsonString(ruleSetEditRequest));

        Set<Integer> conditionList = new HashSet<>();
        if (CollUtil.isNotEmpty(ruleSetEditRequest.getConditionList())) {
            conditionList = new HashSet<>(ruleSetEditRequest.getConditionList());
        }
        /* 添加该规则正在使用的条件*/
        Set<Integer> specialRuleList = new HashSet<>();
        if (CollUtil.isNotEmpty(ruleSetEditRequest.getSpecialRuleList())) {
            specialRuleList = new HashSet<>(ruleSetEditRequest.getSpecialRuleList());
        }
        CountInfo countInfo = new CountInfo();
        countInfo.setConditionList(conditionList);
        countInfo.setSpecialRuleList(specialRuleList);

        saveInfo(ruleSetEditRequest, conditionList, specialRuleList, countInfo);
        List<Integer> typeList = new ArrayList<>();
        typeList.add(PublishEnum.DRAFT.getType());
        typeList.add(WAITING_PUBLISH.getType());
        Wrapper<RuleEngineRuleSetJson> wrapper = new QueryWrapper<RuleEngineRuleSetJson>()
                .in("published", typeList).eq("rule_set_id", ruleSetId);

        ruleEngineRuleSetJsonManager.remove(wrapper);

        /*
         * 更新状态至创建中
         */
        ruleEngineRuleSet.setCreateStatus(PublishEnum.DRAFT.getType());
        /*
         *  更新至创建中状态, 查看版本
         *  **/
        ruleEngineRuleSet.setPreparedVersion("");
        ruleEngineRuleSetManager.updateById(ruleEngineRuleSet);

        ruleEngineRuleSetJson.setCountInfo(JsonUtils.toJsonString(countInfo));
        ruleEngineRuleSetJsonManager.save(ruleEngineRuleSetJson);
        return true;
    }

    private void saveInfo(RuleSetSaveDraftRequest ruleSetEditRequest, Set<Integer> conditionList,
                          Set<Integer> specialRuleList, CountInfo countInfo) {
        /* 查看传入的变量和元素*/
        List<Integer> variableList = ruleSetEditRequest.getVariableList();

        List<Integer> elementList = ruleSetEditRequest.getElementList();

        /* 根据特殊规则，加入对应的condition id 和结果id*/
        if (CollUtil.isNotEmpty(specialRuleList)) {
            List<RuleEngineRule> ruleEngineRuleList = ruleEngineRuleManager.lambdaQuery()
                    .in(RuleEngineRule::getId, specialRuleList).list();
            for (RuleEngineRule ruleEngineRule : ruleEngineRuleList) {
                if (ruleEngineRule.getActionType().equals(ELEMENT.getStatus())) {
                    elementList.add(ruleEngineRule.getActionElementId().intValue());
                }
                if (ruleEngineRule.getActionType().equals(VARIABLE.getStatus())) {
                    variableList.add(ruleEngineRule.getActionVariableId().intValue());
                }
            }
        }
        /* 查出关联的所有conditionGroup*/
        if (CollUtil.isNotEmpty(specialRuleList)) {
            List<RuleEngineRuleConditionGroup> ruleEngineRuleConditionGroupList =
                    ruleEngineRuleConditionGroupManager.lambdaQuery()
                            .in(RuleEngineRuleConditionGroup::getRuleId, specialRuleList)
                            .eq(RuleEngineRuleConditionGroup::getDeleted, ENABLE.getStatus()).list();

            List<Long> ruleEngineConditionGroupIdList = ruleEngineRuleConditionGroupList.stream()
                    .map(RuleEngineRuleConditionGroup::getId).collect(Collectors.toList());
            if (CollUtil.isNotEmpty(ruleEngineConditionGroupIdList)) {
                /* 查出所有conditionGroup关联的condition*/
                List<RuleEngineConditionGroupCondition> ruleEngineConditionGroupConditions =
                        ruleEngineConditionGroupConditionManager.lambdaQuery().in(RuleEngineConditionGroupCondition::getConditionGroupId, ruleEngineConditionGroupIdList)
                                .eq(RuleEngineConditionGroupCondition::getDeleted, ENABLE.getStatus()).list();
                if (CollUtil.isNotEmpty(ruleEngineConditionGroupConditions)) {
                    /* 查询每一个condition enable的id*/
                    List<RuleEngineCondition> ruleEngineConditionInRuleList =
                            ruleEngineConditionManager.lambdaQuery().in(RuleEngineCondition::getId, ruleEngineConditionGroupConditions.stream()
                                    .map(RuleEngineConditionGroupCondition::getConditionId)
                                    .collect(Collectors.toList())).eq(RuleEngineCondition::getDeleted, ENABLE.getStatus()).list();
                    conditionList.addAll(
                            ruleEngineConditionInRuleList.stream().map(e -> e.getId().intValue()).collect(Collectors.toList()));
                }
            }
        }
        /* 查看条件对应的变量和元素 */
        if (CollUtil.isNotEmpty(conditionList)) {
            List<RuleEngineCondition> ruleEngineConditionList = ruleEngineConditionManager.lambdaQuery()
                    .in(RuleEngineCondition::getId, conditionList).list();
            for (RuleEngineCondition ruleEngineCondition : ruleEngineConditionList) {

                if (ruleEngineCondition.getLeftVariableType().equals(VARIABLE.getStatus())) {
                    variableList.add(ruleEngineCondition.getLeftVariableId().intValue());
                }
                if (ruleEngineCondition.getRightVariableType().equals(VARIABLE.getStatus())) {
                    variableList.add(ruleEngineCondition.getRightVariableId().intValue());
                }
                if (ruleEngineCondition.getLeftVariableType().equals(ELEMENT.getStatus())) {
                    elementList.add(ruleEngineCondition.getLeftElementId().intValue());
                }
                if (ruleEngineCondition.getRightVariableType().equals(ELEMENT.getStatus())) {
                    elementList.add(ruleEngineCondition.getRightElementId().intValue());
                }
            }
        }
        List<Integer> ansElementList = new ArrayList<>();
        if (CollUtil.isNotEmpty(elementList)) {
            ansElementList = new ArrayList<>(elementList);
        }
        List<Integer> ansVariableList = new ArrayList<>();
        if (CollUtil.isNotEmpty(variableList)) {
            ansVariableList = new ArrayList<>(variableList);

            /* 递归查询变量中的所有子变量和子元素，并放入对应的list中 */
            for (Integer variableId : variableList) {
                addVariableAndElement(variableId, ansVariableList, ansElementList);
            }
        }
        Set<Integer> distinctElementList = new HashSet<>();
        if (CollUtil.isNotEmpty(ansElementList)) {
            distinctElementList = new HashSet<>(ansElementList);
        }
        Set<Integer> distinctVariableList = new HashSet<>();
        if (CollUtil.isNotEmpty(ansVariableList)) {
            distinctVariableList = new HashSet<>(ansVariableList);
        }
        countInfo.setVariableList(distinctVariableList);
        countInfo.setElementList(distinctElementList);
    }

    private void addVariableAndElement(Integer variableId, List<Integer> variableList, List<Integer> elementList) {
        List<RuleEngineVariableParam> ruleEngineVariableParamList = ruleEngineVariableParamManager.lambdaQuery().eq(RuleEngineVariableParam::getVariableId, variableId).list();
        if (CollUtil.isNotEmpty(ruleEngineVariableParamList)) {
            for (RuleEngineVariableParam ruleEngineVariableParam : ruleEngineVariableParamList) {
                Integer functionParamType = ruleEngineVariableParam.getFunctionParamType();
                if (ObjectUtil.isNotNull(functionParamType) && functionParamType.equals(VARIABLE.getStatus())) {
                    Integer paramVariableId = ruleEngineVariableParam.getFunctionParamVariableId().intValue();
                    variableList.add(paramVariableId);
                    addVariableAndElement(paramVariableId, variableList, elementList);
                } else if (ObjectUtil.isNotNull(functionParamType) && functionParamType.equals(ELEMENT.getStatus())) {
                    elementList.add(ruleEngineVariableParam.getFunctionParamElementId().intValue());
                }
            }
        }
    }

    /**
     * 获取规则草稿
     *
     * @param ruleSetId 规则id
     * @return data
     */
    @Override
    public RuleSetSaveDraftRequest getDraft(Long ruleSetId) {

        RuleEngineRuleSetJson ruleEngineRuleSetJson = ruleEngineRuleSetJsonManager.lambdaQuery().eq(
                RuleEngineRuleSetJson::getRuleSetId, ruleSetId).eq(RuleEngineRuleSetJson::getPublished, PublishEnum.DRAFT.getType()).one();
        if (ruleEngineRuleSetJson == null) {
            return new RuleSetSaveDraftRequest();
        } else {
            RuleSetSaveDraftRequest saveDraftRequest = JsonUtils.parseObject(ruleEngineRuleSetJson.getRuleSetJson(), RuleSetSaveDraftRequest.class);
            //用到的所有的条件
            JSONObject jsonObject = JSON.parseObject(ruleEngineRuleSetJson.getRuleSetJson());
            //解决前端条件统计不全的问题
            List<Integer> conditionIds = (List<Integer>) JSONPath.eval(jsonObject, "$..condition_id");
            if (CollUtil.isEmpty(conditionIds)) {
                return saveDraftRequest;
            }
            List<RuleEngineCondition> engineConditions = ruleEngineConditionManager.lambdaQuery().in(RuleEngineCondition::getId, conditionIds).list();
            //缓存用到的变量以及元素
            RuleAllConditionInfo conditionInfo = new RuleAllConditionInfo();
            //条件中用到的变量
            HashMap<Long, RuleEngineVariable> variableMap = new HashMap<>();
            //条件中用到的元素
            HashMap<Long, RuleEngineElement> elementMap = new HashMap<>();
            ruleEngineConditionService.listCache(engineConditions, variableMap, elementMap);
            conditionInfo.setVariableMap(variableMap);
            conditionInfo.setElementMap(elementMap);
            List<GetRuleEngineConditionResponse> engineConditionResponses = new ArrayList<>();
            for (RuleEngineCondition engineCondition : engineConditions) {
                GetRuleEngineConditionResponse conditionResponse = ruleEngineConditionService.ruleEngineConditionTypeConversion(engineCondition, conditionInfo);
                engineConditionResponses.add(conditionResponse);
            }
            //提供给前端所有用到的元素，让前端去适配
            saveDraftRequest.setConditionMap(engineConditionResponses);
            return saveDraftRequest;
        }
    }

    /**
     * 根据id查询规则集
     *
     * @param ruleSetId 规则集id
     * @return 规则集
     */
    private RuleEngineRuleSet getRuleEngineRuleSet(Long ruleSetId) {
        return Optional.ofNullable(ruleEngineRuleSetManager.getById(ruleSetId))
                .orElseThrow(() -> new ValidException(PARAM_ERROR.code, "找不到对应的ruleSetId"));
    }

    private void setDefaultRule(RuleSetResult ruleSetResult, RuleEngineRule ruleEngineRule) {
        RuleSetResult.DefaultRuleBean defaultRuleBean = new RuleSetResult.DefaultRuleBean();
        RuleSetResult.ActionVariableBean actionVariableBean = new RuleSetResult.ActionVariableBean();
        Integer actionType = ruleEngineRule.getActionType();
        setRuleVariableValue(ruleEngineRule, actionVariableBean, actionType);
        actionVariableBean.setType(actionType);
        defaultRuleBean.setActionVariable(actionVariableBean);
        ruleSetResult.setDefaultRule(defaultRuleBean);
    }

    private void setRuleVariableValue(RuleEngineRule ruleEngineRule, RuleSetResult.ActionVariableBean actionVariableBean, Integer actionType) {
        VariableTypeEnum variableTypeEnum = VariableTypeEnum.getStatus(actionType);
        switch (variableTypeEnum) {
            case VARIABLE:
                Long variableId = ruleEngineRule.getActionVariableId();
                actionVariableBean.setValue(variableId);
                RuleEngineVariable variable = ruleEngineVariableManager.getById(variableId);
                actionVariableBean.setValueName(variable.getName());
                actionVariableBean.setValueType(variable.getValueType());
                break;
            case CONSTANT:
                actionVariableBean.setValue(ruleEngineRule.getActionValue());
                actionVariableBean.setValueName(ruleEngineRule.getActionValue());
                actionVariableBean.setValueType(ruleEngineRule.getActionDataType());
                break;
            case ELEMENT:
                Long elementId = ruleEngineRule.getActionElementId();
                RuleEngineElement element = ruleEngineElementManager.getById(elementId);
                actionVariableBean.setValue(ruleEngineRule.getActionElementId());
                actionVariableBean.setValueName(element.getName());
                actionVariableBean.setValueType(element.getValueType());
                break;
            case RESULT:
                break;
            default:
        }
    }

    private RuleEngineRule saveDefaultRule(RuleSetEditRequest ruleSetEditRequest, RuleSetEditRequest.DefaultRuleBean defaultRuleBean) {
        RuleEngineRule ruleEngineRule = new RuleEngineRule();
        RuleSetEditRequest.ActionVariableBean actionVariableBean = defaultRuleBean.getActionVariable();
        RuleNormalUtils.checkValueType(actionVariableBean.getType(), actionVariableBean.getValueType(), actionVariableBean.getValue());
        saveRule(ruleSetEditRequest.getName() + SUFFIX, ruleEngineRule, actionVariableBean);

        return ruleEngineRule;
    }

    private void saveRule(String name, RuleEngineRule ruleEngineRule, RuleSetEditRequest.ActionVariableBean actionVariableBean) {
        ruleEngineRule.setName(name);
        ruleEngineRule.setRuleType(RuleTypeEnum.NORMAL.getStatus());
        VariableTypeEnum variableTypeEnum = VariableTypeEnum.getStatus(actionVariableBean.getType());
        ruleEngineRule.setActionType(actionVariableBean.getType());
        ruleEngineRule.setActionDataType(actionVariableBean.getValueType());
        switch (variableTypeEnum) {
            case ELEMENT:
                ruleEngineRule.setActionElementId(Long.valueOf(actionVariableBean.getValue() + ""));
                break;
            case VARIABLE:
                ruleEngineRule.setActionVariableId((Long.valueOf(actionVariableBean.getValue() + "")));
                break;
            case CONSTANT:
                ruleEngineRule.setActionValue(actionVariableBean.getValue() + "");
                break;
            case RESULT:
                break;
            default:
        }
        ManageUser loginedUser = WfcRuleContext.getLoginedUser();
        ruleEngineRule.setCreateUserName(loginedUser.getEmployeeName());
        ruleEngineRule.setCreateUserId(loginedUser.getEmployeeId());
        RuleEngineBizBean engineBiz = RuleEngineBizServiceImpl.getEngineBiz();
        ruleEngineRule.setBizName(engineBiz.getBizName());
        ruleEngineRule.setBizCode(engineBiz.getBizCode());
        ruleEngineRule.setBizId(engineBiz.getId());
        try {
            ruleEngineRuleManager.save(ruleEngineRule);
        } catch (Exception e) {
            log.error("新增默认规则失败");
            throw new ValidException(SERVICE_ERROR.code, "新增默认规则失败");
        }
    }

    private void saveSpecialAndNormalRule(RuleSetEditRequest ruleSetEditRequest, RuleEngineRuleSet ruleEngineRuleSet) {
        List<RuleEngineRuleSetRules> ruleEngineRuleSetRulesList = new ArrayList<>();
        /*新增对应普通规则**/
        if (CollUtil.isNotEmpty(ruleSetEditRequest.getNormalRules())) {
            for (RuleSetEditRequest.NormalRulesBean normalRulesBean : ruleSetEditRequest.getNormalRules()) {
                RuleEngineRule ruleEngineRule = new RuleEngineRule();
                RuleSetEditRequest.ActionVariableBean actionVariableBean = normalRulesBean.getActionVariable();
                RuleNormalUtils.checkValueType(actionVariableBean.getType(), actionVariableBean.getValueType(), actionVariableBean.getValue());
                //前置条件集
                List<PreConditionSetBean> conditionSet = normalRulesBean.getPreConditionSet();
                if (CollUtil.isNotEmpty(conditionSet)) {
                    String collect = conditionSet.stream().map(PreConditionSetBean::getName).collect(Collectors.joining(","));
                    ruleEngineRule.setConditionSetNames(collect);
                }
                /*保存规则*/
                saveRule(ruleSetEditRequest.getName() + normalRulesBean.getNormalRuleOrder(), ruleEngineRule, actionVariableBean);
                RuleNormalUtils.saveConditionSet(normalRulesBean.getConditionSet(), ruleSetEditRequest.getName() + NORMAL_RULE, ruleEngineRule.getId());
                // 创建ruleSet_Rule关联实体
                RuleEngineRuleSetRules ruleEngineRuleSetRules = new RuleEngineRuleSetRules();
                ruleEngineRuleSetRules.setRuleId(ruleEngineRule.getId());
                ruleEngineRuleSetRules.setRuleSetId(ruleEngineRuleSet.getId());
                ruleEngineRuleSetRules.setRuleOrderNo(normalRulesBean.getNormalRuleOrder().longValue());
                ruleEngineRuleSetRulesList.add(ruleEngineRuleSetRules);
            }
        }

        /*新增对应特殊规则关联关系**/
        if (CollUtil.isNotEmpty(ruleSetEditRequest.getSpecialRule())) {
            for (RuleSetEditRequest.SpecialRuleBean specialRuleBean : ruleSetEditRequest.getSpecialRule()) {
                RuleEngineRuleSetRules ruleEngineRuleSetRules = new RuleEngineRuleSetRules();
                /*保存特殊规则*/
                SpecialRuleEditParam specialRuleEditParam = new SpecialRuleEditParam();
                specialRuleEditParam.setSpecialPolicy(specialRuleBean.getSpecialPolicyBean());
                specialRuleEditParam.setName("隐藏特殊规则名" + specialRuleBean.getSpecialRuleId());
                specialRuleEditParam.setConditionSet(specialRuleBean.getConditionSet());
                specialRuleEditParam.setPreConditionSet(specialRuleBean.getPreConditionSet());
                Long ruleId = ruleEngineSpecialRuleService.addSpecialRuleInfo(specialRuleEditParam, DISABLE.getStatus());
                saveConditionSet(specialRuleEditParam.getConditionSet(), specialRuleEditParam.getName(), ruleId);
                ruleEngineRuleSetRules.setRuleId(ruleId);
                ruleEngineRuleSetRules.setRuleSetId(ruleEngineRuleSet.getId());
                ruleEngineRuleSetRules.setRuleOrderNo(specialRuleBean.getSpecialRuleOrder().longValue());
                ruleEngineRuleSetRulesList.add(ruleEngineRuleSetRules);
            }
        }
        /*
         * 批量保存关联
         */
        try {
            ruleEngineRuleSetRulesManager.saveBatch(ruleEngineRuleSetRulesList);
        } catch (Exception e) {
            log.error("批量保存rule_ruleSet对应关系失败");
            throw new ValidException(SERVICE_ERROR.code, "批量保存rule_ruleSet对应关系失败");
        }
    }

    private void getRuleSetInfo(RuleSetEditRequest ruleSetEditRequest, RuleEngineRuleSet ruleEngineRuleSet) {
        ruleEngineRuleSet.setHitPolicy(ruleSetEditRequest.getHitPolicy());
        ruleEngineRuleSet.setDefaultRulePolicy(ruleSetEditRequest.getDefaultRulePolicy());
        ruleEngineRuleSet.setNodeRepetitionPolicy(ruleSetEditRequest.getNodeRepetitionPolicy());
        ruleEngineRuleSet.setProcessRepetitionPolicy(ruleSetEditRequest.getProcessRepetitionPolicy());
        ruleEngineRuleSet.setProcessRepetitionNodeName(ruleSetEditRequest.getProcessRepetitionNodeName());

        //加入随机结果判断
        if (ruleSetEditRequest.getRandomResult() != null) {
            if (ruleSetEditRequest.getRandomResult().getResultSwitch()) {
                ruleEngineRuleSet.setRandomSwitch(1);
                ruleEngineRuleSet.setRandomCount(ruleSetEditRequest.getRandomResult().getCount());
            } else {
                ruleEngineRuleSet.setRandomSwitch(0);
                ruleEngineRuleSet.setRandomCount(0);
            }
        }
    }

    private void saveDefaultRule(RuleSetEditRequest ruleSetEditRequest, RuleEngineRuleSet ruleEngineRuleSet) {
        RuleSetEditRequest.DefaultRuleBean defaultRuleBean = ruleSetEditRequest.getDefaultRule();
        if (defaultRuleBean != null && defaultRuleBean.getActionVariable() != null) {
            RuleEngineRule ruleEngineDefaultRule = saveDefaultRule(ruleSetEditRequest, defaultRuleBean);
            Long ruleEngineDefaultRuleId = ruleEngineDefaultRule.getId();
            ruleEngineRuleSet.setDefaultRuleId(ruleEngineDefaultRuleId);
        } else {
            ruleEngineRuleSet.setDefaultRuleId(null);
        }
    }

    private void checkCodeRepeat(String code, Long id) {
        List<RuleEngineRuleSet> ruleEngineRuleSet = ruleEngineRuleSetManager.lambdaQuery()
                .eq(RuleEngineRuleSet::getDeleted, ENABLE.getStatus()).eq(RuleEngineRuleSet::getBizCode,
                        RuleEngineBizServiceImpl.getEngineBiz().getBizCode()).eq(RuleEngineRuleSet::getCode, code).list();
        if (CollectionUtil.isNotEmpty(ruleEngineRuleSet) && !ruleEngineRuleSet.get(0).getId().equals(id)) {
            throw new ValidException( "存在相同code的规则");
        }
    }

}

