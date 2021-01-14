package com.zjb.ruleplatform.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.collection.CollectionUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.baomidou.mybatisplus.core.toolkit.StringPool;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.founder.ego.common.request.PageBase;
import com.founder.ego.common.request.PageRequest;
import com.founder.ego.common.response.PageResponse;
import com.founder.ego.common.response.PageResult;
import com.founder.ego.common.response.Rows;
import com.founder.ego.enumbean.DataTypeEnum;
import com.founder.ego.enumbean.DeletedEnum;
import com.founder.ego.enumbean.RuleSetUpdateSourceEnum;
import com.founder.ego.exception.ValidException;
import com.founder.ego.ruleengine.core.enums.VariableTypeEnum;
import com.founder.ego.service.ruleengine.RuleEngineLoadService;
import com.founder.ego.service.ruleengine.RuleEngineRuleSetService;
import com.founder.ego.service.ruleengine.RuleEngineVariableService;
import com.founder.ego.service.ruleengine.RuleLockService;
import com.founder.ego.service.ruleengine.decisiontable.impl.RuleEngineDecisionLoadServiceImpl;
import com.founder.ego.store.bpm.entity.*;
import com.founder.ego.store.bpm.manager.*;
import com.founder.ego.utils.DB;
import com.founder.ego.utils.PageUtils;
import com.founder.ego.utils.RuleNormalUtils;
import com.founder.ego.vo.ruleengine.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.*;
import java.util.stream.Collectors;

import static com.founder.ego.enumbean.DeletedEnum.DISABLE;
import static com.founder.ego.enumbean.DeletedEnum.ENABLE;
import static com.founder.ego.enumbean.ResponseCode.PARAM_ERROR;
import static com.founder.ego.enumbean.ResponseCode.SERVICE_ERROR;
import static com.founder.ego.ruleengine.core.enums.VariableTypeEnum.CONSTANT;
import static com.founder.ego.ruleengine.core.enums.VariableTypeEnum.VARIABLE;

/**
 * @author yuzhiji
 */

@Service
@Slf4j
public class RuleEngineVariableServiceImpl implements RuleEngineVariableService {

    @Resource
    private RuleEngineVariableManager ruleEngineVariableManager;
    @Resource
    private RuleEngineFunctionManager ruleEngineFunctionManager;
    @Resource
    private RuleEngineFunctionParamManager ruleEngineFunctionParamManager;
    @Resource
    private RuleEngineVariableParamManager ruleEngineVariableParamManager;
    @Resource
    private RuleEngineElementManager ruleEngineElementManager;
    @Resource
    private RuleEngineConditionManager ruleEngineConditionManager;
    @Resource
    private RuleEngineRuleManager ruleEngineRuleManager;
    @Autowired
    private RuleEngineLoadService ruleEngineLoadService;
    @Resource
    private RuleEngineDecisionLoadServiceImpl ruleEngineDecisionLoadServiceImpl;
    @Resource
    private RuleEngineRuleSetManager ruleEngineRuleSetManager;
    @Resource
    private RuleEngineRuleSetService ruleEngineRuleSetService;
    @Resource
    private RuleLockService ruleLockService;

    private static final String FUNCTION = "FUNCTION";

    private static final String DELETE_VARIABLE_MSG = "该变量正被使用中, 不可删除";

    @Override
    @Transactional
    public RuleEngineVariableList getVariableList(CommonIdListParam commonIdListParam) {
        List<Long> commonIdList = commonIdListParam.getIdList();
        /* 获取所有variable */
        List<GetRuleEngineVariableResponse> variableResponses = new ArrayList<>();
        List<RuleEngineVariable> variableList = ruleEngineVariableManager.lambdaQuery()
                .eq(RuleEngineVariable::getBizCode, RuleEngineBizServiceImpl.getEngineBiz().getBizCode())
                .in(RuleEngineVariable::getId, commonIdList).list();

        /* 获取所有的函数functionList*/
        List<RuleEngineFunction> functionList = ruleEngineFunctionManager.list();
        Map<Long, RuleEngineFunction> functionMap = functionList.stream().collect(Collectors.toMap(RuleEngineFunction::getId, a -> a, (k1, k2) -> k1));

        for (RuleEngineVariable ruleEngineVariable : variableList) {
            GetRuleEngineVariableResponse ruleEngineVariableResponse = new GetRuleEngineVariableResponse();
            ruleEngineVariableResponse.setId(ruleEngineVariable.getId().intValue());
            ruleEngineVariableResponse.setDescription(ruleEngineVariable.getDescription());
            ruleEngineVariableResponse.setName(ruleEngineVariable.getName());
            if (ruleEngineVariable.getType().equals(CONSTANT.getStatus())) {
                ruleEngineVariableResponse.setValue(ruleEngineVariable.getValue());
                ruleEngineVariableResponse.setValueType(ruleEngineVariable.getValueType());
                variableResponses.add(ruleEngineVariableResponse);
            } else {
                ruleEngineVariableResponse.setValueType(FUNCTION);
                RuleEngineFunction ruleEngineFunction = functionMap.get(ruleEngineVariable.getId());
                ruleEngineVariableResponse.setValue(ruleEngineFunction != null ? ruleEngineFunction.getFunctionName() : StringPool.EMPTY);
                ruleEngineVariableResponse.setResultValueType(ruleEngineFunction != null ? ruleEngineFunction.getResultValueType() : StringPool.EMPTY);
                variableResponses.add(ruleEngineVariableResponse);
            }
        }
        RuleEngineVariableList ruleEngineVariableList = new RuleEngineVariableList();
        ruleEngineVariableList.setRuleEngineVariableResponseList(variableResponses);
        return ruleEngineVariableList;
    }

    @Override
    @Transactional
    public Boolean deleteVariable(CommonIdParam commonIdParam) {
        Long commandId = commonIdParam.getId();
        RuleEngineVariable ruleEngineVariable = checkVariableId(commandId);
        //规则/决策内多人编辑验证
        //如果是决策表
        if (Objects.equals(ruleEngineVariable.getRuleSetType(), DataTypeEnum.DECISION.getDataType())) {
            ruleLockService.decisionValid(ruleEngineVariable.getRuleSetId());
        }
        //如果是规则表
        if (Objects.equals(ruleEngineVariable.getRuleSetType(), DataTypeEnum.RULESET.getDataType())) {
            ruleLockService.ruleSetValid(ruleEngineVariable.getRuleSetId());
        }
        /* 查询是否存在作为函数参数的上一级变量*/
        List<Long> variableIdList = new ArrayList<>();
        getFatherVariableIdListRecursive(variableIdList, commandId);
        /*看变量有没有被别的变量引用*/
        if (CollUtil.isNotEmpty(variableIdList)) {
            List<RuleEngineVariable> ruleEngineVariableList = ruleEngineVariableManager.lambdaQuery()
                    .eq(RuleEngineVariable::getDeleted, ENABLE.getStatus())
                    .in(RuleEngineVariable::getId, variableIdList).list();
            /*分可见变量和不可见变量 如果是可见变量则说明已经需要先删除父变量*/
            for (RuleEngineVariable variable : ruleEngineVariableList) {
                if (variable.getShowed().equals(ENABLE.getStatus())) {
                    throw new ValidException(SERVICE_ERROR.code, DELETE_VARIABLE_MSG);
                }
            }
        }
        variableIdList.add(commandId);
        List<RuleEngineCondition> ruleEngineConditionList = ruleEngineConditionManager.lambdaQuery()
                .eq(RuleEngineCondition::getDeleted, ENABLE.getStatus())
                //.eq(RuleEngineCondition::getShowed, 0)
                .and(e -> e.in(RuleEngineCondition::getLeftVariableId, variableIdList)
                        .or()
                        .in(RuleEngineCondition::getRightVariableId, variableIdList)).list();
        /* 部分变量没有因为条件删除而删除  */
        if (CollUtil.isNotEmpty(ruleEngineConditionList)) {
            /* 获取list的conditionId */
            throw new ValidException(DELETE_VARIABLE_MSG);
            /* 查看对应条件的特殊规则 */
        }
        /* 查看变量相关的规则 */
        List<RuleEngineRule> ruleEngineRuleList = new ArrayList<>();
        if (CollUtil.isNotEmpty(variableIdList)) {
            ruleEngineRuleList = ruleEngineRuleManager.lambdaQuery()
                    .eq(RuleEngineRule::getDeleted, ENABLE.getStatus())
                    .in(RuleEngineRule::getActionVariableId, variableIdList).list();
        }
        if (CollUtil.isNotEmpty(ruleEngineRuleList)) {
            /* 是否被特殊规则引用 */
            List<RuleEngineRule> specialRuleList = new ArrayList<>();
            /* 部门action_variable_id或condition没有因为特殊规则的删除而删除 */
            if (CollUtil.isNotEmpty(specialRuleList)) {
                /* 如果是特殊规则 */
                throw new ValidException(DELETE_VARIABLE_MSG);
            }
        }
        //统计变量是否被规则引用
        Integer count = countVariable(commandId).getCount();
        if (count > 0) {
            throw new ValidException("当前变量被使用，不可被删除");
        }
        /* 变量被规则集使用 */
        ruleEngineVariable.setDeleted(DISABLE.getStatus());
        return ruleEngineVariableManager.updateById(ruleEngineVariable);
    }

    @Override
    public Boolean validateUniqName(CommonNameParam commonNameParam) {
        String commonName = commonNameParam.getName();
        int count = ruleEngineVariableManager.lambdaQuery()
                .eq(RuleEngineVariable::getDeleted, ENABLE.getStatus()).eq(RuleEngineVariable::getBizCode,
                        RuleEngineBizServiceImpl.getEngineBiz().getBizCode())
                .eq(RuleEngineVariable::getName, commonName).count();
        return count != 0;
    }

    @Override
    public CountVo countVariable(Long variableId) {
        CountVo countVo = new CountVo();
        //统计决策表中变量引用的数量
        int count = ruleEngineVariableManager.getCounts(variableId);
        List<RuleEngineDecisionData> dataCount = DB.lQuery(RuleEngineDecisionData.class)
                .select(RuleEngineDecisionData::getDecisionId)
                .eq(RuleEngineDecisionData::getType, VARIABLE.getStatus())
                .eq(RuleEngineDecisionData::getValue, variableId).list();
        List<RuleEngineDecisionInput> inputCount = DB.lQuery(RuleEngineDecisionInput.class)
                .select(RuleEngineDecisionInput::getDecisionId)
                .eq(RuleEngineDecisionInput::getType, VARIABLE.getStatus())
                .eq(RuleEngineDecisionInput::getValue, variableId)
                .or()
                .eq(RuleEngineDecisionInput::getDefaultType, VARIABLE.getStatus())
                .eq(RuleEngineDecisionInput::getDefaultValue, variableId)
                .list();
        //如果被一个决策表多次引用去重复
        HashSet<Long> longs = new HashSet<>();
        if (CollUtil.isNotEmpty(dataCount)) {
            Set<Long> collect = dataCount.stream().map(RuleEngineDecisionData::getDecisionId).collect(Collectors.toSet());
            longs.addAll(collect);
        }
        if (CollUtil.isNotEmpty(inputCount)) {
            Set<Long> collect = inputCount.stream().map(RuleEngineDecisionInput::getDecisionId).collect(Collectors.toSet());
            longs.addAll(collect);
        }
        countVo.setCount(longs.size() + count);
        return countVo;
    }


    @Override
    @Transactional(rollbackFor = Exception.class)
    public GetRuleEngineVariableResponse addVariable(RuleEngineVariableRequest param) {
        /* 新增返回变量详情*/
        GetRuleEngineVariableResponse variableResponse = new GetRuleEngineVariableResponse();
        variableResponse.setDescription(param.getDescription());
        variableResponse.setName(param.getName());
        variableResponse.setValue(param.getValue());

        checkNameRepeat(param.getName(), null);
        RuleEngineVariable ruleEngineVariable = new RuleEngineVariable();
        ruleEngineVariable.setName(param.getName());
        ruleEngineVariable.setValue(param.getValue());
        if (param.getIsShowed() == null || param.getIsShowed()) {
            ruleEngineVariable.setShowed(ENABLE.getStatus());
        } else {
            ruleEngineVariable.setShowed(DISABLE.getStatus());
        }
        if (param.isNotNull()) {
            ruleEngineVariable.setRuleSetId(param.getRuleSetId());
            ruleEngineVariable.setRuleSetType(param.getRuleSetType());
            //规则内创建变量无法在基础组件列表展示
            ruleEngineVariable.setShowed(DISABLE.getStatus());
            //如果是决策表
            if (Objects.equals(param.getRuleSetType(), DataTypeEnum.DECISION.getDataType())) {
                ruleLockService.decisionValid(param.getRuleSetId());
            }
            //如果是规则表
            if (Objects.equals(param.getRuleSetType(), DataTypeEnum.RULESET.getDataType())) {
                ruleLockService.ruleSetValid(param.getRuleSetId());
            }
        }
        if (param.getValueType().equals(FUNCTION)) {
            RuleEngineFunction fun = ruleEngineFunctionManager.getById(param.getFunction().getId());
            ruleEngineVariable.setValueType(fun.getResultValueType());
            variableResponse.setFunction(param.getFunction());
            variableResponse.setValueType(FUNCTION);
            variableResponse.setResultValueType(fun.getResultValueType());
        } else {
            ruleEngineVariable.setValueType(param.getValueType());
            variableResponse.setValueType(param.getValueType());
        }
        ruleEngineVariable.setDescription(param.getDescription());
        RuleEngineBizBean engineBiz = RuleEngineBizServiceImpl.getEngineBiz();
        ruleEngineVariable.setBizId(engineBiz.getId());
        ruleEngineVariable.setBizCode(engineBiz.getBizCode());
        ruleEngineVariable.setBizName(engineBiz.getBizName());
        FunctionBean function = param.getFunction();
        if (function != null && function.getVariables() != null) {
            List<RuleEngineFunctionParam> ruleEngineFunctionParamList = ruleEngineFunctionParamManager.lambdaQuery().list();
            ruleEngineVariable.setType(VARIABLE.getStatus());
            Integer functionId = function.getId();
            ruleEngineVariable.setFunctionId(functionId);
            try {
                ruleEngineVariableManager.save(ruleEngineVariable);
            } catch (Exception e) {
                log.error("无法增加变量");
                throw new ValidException(SERVICE_ERROR.code, "无法增加变量异常");
            }
            List<FunctionBean.VariablesBean> variablesBeanList = function.getVariables();
            saveBatchVariableParam(ruleEngineVariable, ruleEngineFunctionParamList, functionId, variablesBeanList);
        } else {
            ruleEngineVariable.setType(CONSTANT.getStatus());
            try {
                ruleEngineVariableManager.save(ruleEngineVariable);
            } catch (Exception e) {
                log.error("无法增加变量");
                throw new ValidException(SERVICE_ERROR.code, "无法增加变量异常");
            }
        }
        variableResponse.setId(ruleEngineVariable.getId().intValue());
        return variableResponse;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean updateVariable(RuleEngineVariableRequest ruleEngineVariableRequest) {
        long currentTimeMillis = System.currentTimeMillis();
        Long variableId = ruleEngineVariableRequest.getId();
        RuleEngineVariable ruleEngineVariable = checkVariableId(variableId);
        //规则/决策内多人编辑验证
        //如果是决策表
        if (Objects.equals(ruleEngineVariable.getRuleSetType(), DataTypeEnum.DECISION.getDataType())) {
            ruleLockService.decisionValid(ruleEngineVariable.getRuleSetId());
        }
        //如果是规则表
        if (Objects.equals(ruleEngineVariable.getRuleSetType(), DataTypeEnum.RULESET.getDataType())) {
            ruleLockService.ruleSetValid(ruleEngineVariable.getRuleSetId());
        }
        if (ruleEngineVariable.getDeleted().equals(DISABLE.getStatus())) {
            throw new ValidException( "该变量已经被删除，无法被更新");
        }
        String name = ruleEngineVariableRequest.getName();
        checkNameRepeat(name, ruleEngineVariableRequest.getId());
        ruleEngineVariable.setName(name);
        if (ruleEngineVariableRequest.getIsShowed() == null || ruleEngineVariableRequest.getIsShowed()) {
            ruleEngineVariable.setShowed(ENABLE.getStatus());
        } else {
            ruleEngineVariable.setShowed(DISABLE.getStatus());
        }

        if (ruleEngineVariableRequest.isNotNull()) {
            ruleEngineVariable.setRuleSetId(ruleEngineVariableRequest.getRuleSetId());
            ruleEngineVariable.setRuleSetType(ruleEngineVariableRequest.getRuleSetType());
            //规则内创建变量无法在基础组件列表展示
            ruleEngineVariable.setShowed(DISABLE.getStatus());
        }
        ruleEngineVariable.setUpdateTime(null);
        ruleEngineVariable.setValue(ruleEngineVariableRequest.getValue());
        ruleEngineVariable.setDescription(ruleEngineVariableRequest.getDescription());
        if (ruleEngineVariableRequest.getValueType().equals(FUNCTION)) {
            Integer functionId = ruleEngineVariableRequest.getFunction().getId();
            ruleEngineVariable.setValueType(ruleEngineFunctionManager.getById(functionId).getResultValueType());
        } else {
            ruleEngineVariable.setValueType(ruleEngineVariableRequest.getValueType());
        }
        FunctionBean function = ruleEngineVariableRequest.getFunction();
        if (function != null && function.getVariables() != null) {
            ruleEngineVariable.setType(VARIABLE.getStatus());
            List<RuleEngineFunctionParam> ruleEngineFunctionParamList =
                    ruleEngineFunctionParamManager.lambdaQuery().eq(RuleEngineFunctionParam::getDeleted,
                            ENABLE.getStatus()).eq(RuleEngineFunctionParam::getFunctionId, function.getId()).list();
            Integer functionId = function.getId();
            Integer rawFunctionId = ruleEngineVariable.getFunctionId();
            ruleEngineVariable.setFunctionId(functionId);
            try {
                ruleEngineVariableManager.updateById(ruleEngineVariable);
            } catch (Exception e) {
                log.error("无法更新变量");
                throw new ValidException(SERVICE_ERROR.code, "更新变量异常");
            }
            List<FunctionBean.VariablesBean> variablesBeanList = function.getVariables();
            deleteBatchVariableParam(ruleEngineVariable, rawFunctionId);
            saveBatchVariableParam(ruleEngineVariable, ruleEngineFunctionParamList, functionId, variablesBeanList);
        } else {
            ruleEngineVariable.setType(CONSTANT.getStatus());
            try {
                ruleEngineVariableManager.updateById(ruleEngineVariable);
            } catch (Exception e) {
                log.error("无法更新变量");
                throw new ValidException(SERVICE_ERROR.code, "更新变量异常");
            }
        }
        DB.invalidate(RuleEngineVariable.class, ruleEngineVariable.getId());
        log.info("本地请求处理完成，处理时间：{}，开始调用更新远程环境规则", System.currentTimeMillis() - currentTimeMillis);
        ruleEngineLoadService.createRuleSetJson(variableId.intValue(), RuleSetUpdateSourceEnum.VARIABLE);
        //决策表生成小版本，重新发布
        log.info("本地请求处理完成，处理时间：{}，开始调用更新远程环境策略表", System.currentTimeMillis() - currentTimeMillis);
        ruleEngineDecisionLoadServiceImpl.createRuleSetJson(variableId.intValue(), RuleSetUpdateSourceEnum.VARIABLE);
        return true;
    }

    @Override
    public PageResult<ListRuleEngineVariableResponse> listVariable(PageRequest<ListRuleEngineVariableRequest> pageRequest) {
        ListRuleEngineVariableRequest ruleEngineVariableRequest = pageRequest.getQuery();
        PageBase page = pageRequest.getPage();
        QueryWrapper<RuleEngineVariable> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda().eq(RuleEngineVariable::getDeleted, ENABLE.getStatus())
                .eq(RuleEngineVariable::getBizCode, RuleEngineBizServiceImpl.getEngineBiz().getBizCode());
        List<String> valueTypeList = ruleEngineVariableRequest.getValueTypes();
        boolean hasFunction;
        if (pageRequest.getQuery().getHasFunction() == null) {
            hasFunction = true;
        } else {
            hasFunction = pageRequest.getQuery().getHasFunction();
        }
        if (CollectionUtils.isNotEmpty(valueTypeList)) {
            if (hasFunction) {
                queryWrapper.lambda().and(
                        q -> {
                            //先查出所有的函数
                            List<RuleEngineFunction> ruleEngineFunctionList = ruleEngineFunctionManager.lambdaQuery()
                                    .eq(RuleEngineFunction::getDeleted, ENABLE.getStatus())
                                    .eq(RuleEngineFunction::getShowed, ENABLE.getStatus()).list();

                            for (int i = 0; i < valueTypeList.size(); i++) {
                                if (valueTypeList.get(i).equals(FUNCTION)) {
                                    q = q.eq(RuleEngineVariable::getType, VARIABLE.getStatus());
                                    String returnNameType = ruleEngineVariableRequest.getFunctionReturnType();
                                    if (StringUtils.isNotEmpty(returnNameType)) {
                                        String[] returnNameArray = ruleEngineVariableRequest.getFunctionReturnType().split(",");
                                        List<String> returnNameList = Arrays.asList(returnNameArray);
                                        if (CollUtil.isNotEmpty(returnNameList)) {
                                            List<Long> ruleEngineFunctionId = ruleEngineFunctionList.stream()
                                                    .filter(e -> returnNameList.contains(e.getResultValueType())).map(
                                                            RuleEngineFunction::getId
                                                    ).collect(Collectors.toList());
                                            q = q.in(RuleEngineVariable::getFunctionId, ruleEngineFunctionId);
                                        }
                                    }
                                } else {
                                    q = q.eq(RuleEngineVariable::getValueType,
                                            valueTypeList.get(i));
                                }
                                if (i != valueTypeList.size() - 1) {
                                    q = q.or();
                                }
                            }
                            return q;
                        });
            } else {
                queryWrapper.lambda().in(RuleEngineVariable::getValueType, valueTypeList).ne(RuleEngineVariable::getType, VARIABLE.getStatus());
            }

        }
        String queryName = ruleEngineVariableRequest.getName();
        if (StringUtils.isNotBlank(queryName)) {
            queryWrapper.lambda().like(RuleEngineVariable::getName, queryName);
        }
        if (ruleEngineVariableRequest.getQueryType().equals(0)) {
            //查询公有变量
            queryWrapper.lambda().eq(RuleEngineVariable::getShowed, ENABLE.getStatus());
        } else if (ruleEngineVariableRequest.getQueryType().equals(1)) {
            queryWrapper.lambda()
                    .eq(RuleEngineVariable::getShowed, DeletedEnum.ENABLE.getStatus())
                    .or(o -> {
                        LambdaQueryWrapper<RuleEngineVariable> wrapper = o.eq(RuleEngineVariable::getRuleSetId, ruleEngineVariableRequest.getRuleSetId())
                                .eq(RuleEngineVariable::getRuleSetType, ruleEngineVariableRequest.getRuleSetType())
                                .eq(RuleEngineVariable::getShowed, DISABLE.getStatus())
                                .eq(RuleEngineVariable::getDeleted, ENABLE.getStatus())
                                .like(RuleEngineVariable::getName, queryName);
                        if (CollectionUtils.isNotEmpty(valueTypeList)) {
                            wrapper.in(RuleEngineVariable::getValueType, valueTypeList);
                        }
                        return wrapper;
                    });
        } else if (ruleEngineVariableRequest.getQueryType().equals(2)) {
            //查询私有的
            queryWrapper.lambda().eq(RuleEngineVariable::getRuleSetId, ruleEngineVariableRequest.getRuleSetId());
            queryWrapper.lambda().eq(RuleEngineVariable::getRuleSetType, ruleEngineVariableRequest.getRuleSetType());
            queryWrapper.lambda().eq(RuleEngineVariable::getShowed, DISABLE.getStatus());
        }
        //排序
        PageUtils.defaultOrder(pageRequest.getOrders(), queryWrapper, RuleEngineVariable::getId);
        IPage<RuleEngineVariable> pageInfo = ruleEngineVariableManager.page(new Page<>(page.getPageIndex(), page.getPageSize()), queryWrapper);
        List<RuleEngineVariable> ruleEngineVariableList = pageInfo.getRecords();
        Map<Long, RuleEngineFunction> functionMap = ruleEngineFunctionManager.lambdaQuery()
                .list().stream().collect(Collectors.toMap(RuleEngineFunction::getId, e -> e, (k1, k2) -> k1));
        //获取相关的循环策略信息
        List<ListRuleEngineVariableResponse> ruleEngineVariableResponses = ruleEngineVariableList.stream()
                .map(ruleEngineVariable -> {
                    ListRuleEngineVariableResponse response = new ListRuleEngineVariableResponse();
                    response.setId(ruleEngineVariable.getId().intValue());
                    response.setName(ruleEngineVariable.getName());
                    /*关联循环函数信息*/
                    if (hasFunction) {
                        if (ruleEngineVariable.getType().equals(CONSTANT.getStatus())) {
                            response.setValue(ruleEngineVariable.getValue());
                            response.setValueType(ruleEngineVariable.getValueType());
                        } else {
                            response.setValueType(FUNCTION);
                            RuleEngineFunction ruleEngineFunction = functionMap.get(ruleEngineVariable.getFunctionId().longValue());

                            response.setValue(ruleEngineFunction != null ? ruleEngineFunction.getFunctionName() : StringPool.EMPTY);
                            response.setResultValueType(ruleEngineFunction != null ? ruleEngineFunction.getResultValueType() : StringPool.EMPTY);

                        }
                    } else {
                        response.setValue(ruleEngineVariable.getValue());
                        response.setValueType(ruleEngineVariable.getValueType());
                    }
                    return response;
                }).collect(Collectors.toList());
        PageResult<ListRuleEngineVariableResponse> pageResult = new PageResult<>();
        pageResult.setData(new Rows<>(ruleEngineVariableResponses, new PageResponse(page.getPageIndex(), page.getPageSize(), pageInfo.getTotal())));
        return pageResult;
    }


    @Override
    @Transactional(rollbackFor = Exception.class)
    public GetRuleEngineVariableResponse getVariable(CommonIdParam commonIdParam) {
        Long variableId = commonIdParam.getId();
        RuleEngineVariable ruleEngineVariable = checkVariableId(variableId);
        GetRuleEngineVariableResponse ruleEngineVariableResponse = new GetRuleEngineVariableResponse();
        ruleEngineVariableResponse.setId(ruleEngineVariable.getId().intValue());
        ruleEngineVariableResponse.setDescription(ruleEngineVariable.getDescription());
        ruleEngineVariableResponse.setName(ruleEngineVariable.getName());
        if (ruleEngineVariable.getType().equals(CONSTANT.getStatus())) {
            ruleEngineVariableResponse.setValue(ruleEngineVariable.getValue());
            ruleEngineVariableResponse.setValueType(ruleEngineVariable.getValueType());
            return ruleEngineVariableResponse;
        } else {
            ruleEngineVariableResponse.setValueType(FUNCTION);
            Integer functionId = ruleEngineVariable.getFunctionId();
            RuleEngineFunction ruleEngineFunction = ruleEngineFunctionManager.getById(functionId);
            ruleEngineVariableResponse.setValue(ruleEngineFunction.getFunctionName());
            ruleEngineVariableResponse.setResultValueType(ruleEngineFunction.getResultValueType());
            FunctionBean functionBean = new FunctionBean();
            functionBean.setId(ruleEngineFunction.getId().intValue());
            List<RuleEngineVariableParam> ruleEngineFunctionParamList =
                    ruleEngineVariableParamManager.lambdaQuery().eq(RuleEngineVariableParam::getDeleted, ENABLE.getStatus())
                            .eq(RuleEngineVariableParam::getFunctionId, functionId)
                            .eq(RuleEngineVariableParam::getVariableId, variableId).list();
            List<FunctionBean.VariablesBean> variableBeanList =
                    ruleEngineFunctionParamList.stream().map(ruleEngineFunctionParam -> {
                        FunctionBean.VariablesBean variablesBean = new
                                FunctionBean.VariablesBean();
                        variablesBean.setCode(ruleEngineFunctionParam.getFunctionParamCode());
                        variablesBean.setName(ruleEngineFunctionParam.getFunctionParamName());
                        variablesBean.setValueType(ruleEngineFunctionParam.getFunctionParamDataType());
                        Integer functionParamType = ruleEngineFunctionParam.getFunctionParamType();
                        variablesBean.setType(functionParamType);
                        VariableTypeEnum variableTypeEnum = VariableTypeEnum.getStatus(functionParamType);
                        switch (variableTypeEnum) {
                            case VARIABLE:
                                variablesBean.setValueName(ruleEngineFunctionParam.getFunctionParamVariableName());
                                variablesBean.setValue(String.valueOf(ruleEngineFunctionParam.getFunctionParamVariableId()));
                                break;
                            case ELEMENT:
                                variablesBean.setValueName(ruleEngineFunctionParam.getFunctionParamElementName());
                                variablesBean.setValue(String.valueOf(ruleEngineFunctionParam.getFunctionParamElementId()));
                                break;
                            case CONSTANT:
                                variablesBean.setValue(ruleEngineFunctionParam.getFunctionParamValue());
                                variablesBean.setValueName(ruleEngineFunctionParam.getFunctionParamValue());
                                break;
                            default:
                        }
                        return variablesBean;
                    }).collect(Collectors.toList());
            functionBean.setVariables(variableBeanList);
            ruleEngineVariableResponse.setFunction(functionBean);
            return ruleEngineVariableResponse;
        }
    }

    private void saveBatchVariableParam(RuleEngineVariable ruleEngineVariable, List<RuleEngineFunctionParam> ruleEngineFunctionParamList, Integer functionId, List<FunctionBean.VariablesBean> variablesBeanList) {
        List<RuleEngineVariableParam> ruleEngineVariableParamList = new ArrayList<>();
        RuleEngineBizBean engineBiz = RuleEngineBizServiceImpl.getEngineBiz();
        Map<Long, String> elementNameMap = ruleEngineElementManager.lambdaQuery()
                .eq(RuleEngineElement::getDeleted, ENABLE.getStatus()).eq(RuleEngineElement::getBizCode,
                        engineBiz.getBizCode()).list().stream().collect(Collectors.toMap(
                        RuleEngineElement::getId, RuleEngineElement::getName, (oldValue, newValue) -> oldValue
                ));
        Map<Long, String> variableNameMap = ruleEngineVariableManager.lambdaQuery().eq(RuleEngineVariable::getBizCode,
                engineBiz.getBizCode())
                .eq(RuleEngineVariable::getDeleted, ENABLE.getStatus()).list().stream().collect(Collectors.toMap(
                        RuleEngineVariable::getId, RuleEngineVariable::getName, (oldValue, newValue) -> oldValue
                ));
        if (variablesBeanList != null) {
            for (FunctionBean.VariablesBean variablesBean : variablesBeanList) {
                Object value = variablesBean.getValue();
                boolean checkResult = RuleNormalUtils.checkValueType(variablesBean.getType(),
                        variablesBean.getValueType(), value);
                if (!checkResult) {
                    throw new ValidException( "变量的参数value不合法");
                }
                RuleEngineVariableParam ruleEngineVariableParam = new RuleEngineVariableParam();
                ruleEngineVariableParam.setFunctionId(functionId.longValue());
                ruleEngineVariableParam.setVariableId(ruleEngineVariable.getId());
                ruleEngineVariableParam.setFunctionParamType(variablesBean.getType());
                ruleEngineVariableParam.setFunctionParamCode(variablesBean.getCode());
                ruleEngineVariableParam.setFunctionParamDataType(variablesBean.getValueType());

                for (RuleEngineFunctionParam functionParam : ruleEngineFunctionParamList) {
                    if (functionParam.getFunctionId().equals(functionId.longValue()) &&
                            functionParam.getFunctionParamCode().equals(variablesBean.getCode())) {
                        ruleEngineVariableParam.setFunctionParamName(functionParam.getFunctionParamName());
                        VariableTypeEnum variableTypeEnum = VariableTypeEnum.getStatus(variablesBean.getType());
                        switch (variableTypeEnum) {
                            case ELEMENT:
                                String elementObject = value + "";
                                Long elementId = Long.valueOf(elementObject);
                                ruleEngineVariableParam.setFunctionParamElementId(elementId);
                                ruleEngineVariableParam.setFunctionParamElementName(elementNameMap.get(elementId));
                                break;
                            case VARIABLE:
                                String variableObject = value + "";
                                Long variableId = Long.valueOf(variableObject);
                                ruleEngineVariableParam.setFunctionParamVariableId(variableId);
                                ruleEngineVariableParam.setFunctionParamVariableName(variableNameMap.get(variableId));
                                break;
                            case CONSTANT:
                                ruleEngineVariableParam.setFunctionParamValue(String.valueOf(value));
                                break;
                            case RESULT:
                                break;
                            default:
                        }
                    }
                }
                ruleEngineVariableParamList.add(ruleEngineVariableParam);
            }
        }
        ruleEngineVariableParamManager.saveBatch(ruleEngineVariableParamList);

    }

    private RuleEngineVariable checkVariableId(Long variableId) {
        return Optional.ofNullable(ruleEngineVariableManager.getById(variableId))
                .orElseThrow(() -> new ValidException(PARAM_ERROR.code, "不存在对应变量id"));
    }


    private void checkNameRepeat(String name, Long id) {
        List<RuleEngineVariable> ruleEngineVariableList = ruleEngineVariableManager.lambdaQuery()
                .eq(RuleEngineVariable::getDeleted, ENABLE.getStatus()).eq(RuleEngineVariable::getBizCode,
                        RuleEngineBizServiceImpl.getEngineBiz().getBizCode())
                .eq(RuleEngineVariable::getName, name).list();
        if (CollectionUtil.isNotEmpty(ruleEngineVariableList)) {
            if (id == null) {
                throw new ValidException( "存在相同名字的变量");
            }
            if (!ruleEngineVariableList.get(0).getId().equals(id)) {
                throw new ValidException( "存在相同名字的变量");
            }
        }
    }

    private void deleteBatchVariableParam(RuleEngineVariable ruleEngineVariable, Integer functionId) {
        List<RuleEngineVariableParam> ruleEngineVariableParamList = ruleEngineVariableParamManager.lambdaQuery()
                .eq(RuleEngineVariableParam::getVariableId, ruleEngineVariable.getId())
                .eq(RuleEngineVariableParam::getFunctionId, functionId)
                .eq(RuleEngineVariableParam::getDeleted, ENABLE.getStatus()).list();
        ruleEngineVariableParamList.forEach(e -> e.setDeleted(DISABLE.getStatus()));
        if (CollectionUtil.isNotEmpty(ruleEngineVariableParamList)) {
            try {
                ruleEngineVariableParamManager.updateBatchById(ruleEngineVariableParamList);
            } catch (Exception e) {
                log.error("批量更新变量异常");
                throw new ValidException(SERVICE_ERROR.code, "批量更新变量异常");
            }
        }
    }

    private void getFatherVariableIdListRecursive(List<Long> commonIdList, Long currentId) {
        List<RuleEngineVariableParam> ruleEngineVariableParamList = ruleEngineVariableParamManager.lambdaQuery()
                .eq(RuleEngineVariableParam::getFunctionParamVariableId, currentId)
                .eq(RuleEngineVariableParam::getDeleted, ENABLE.getStatus()).list();
        if (CollUtil.isNotEmpty(ruleEngineVariableParamList)) {
            List<Long> ruleEngineVariableList = ruleEngineVariableParamList.stream().map(RuleEngineVariableParam::getVariableId)
                    .distinct().collect(Collectors.toList());
            commonIdList.addAll(ruleEngineVariableList);
        }
    }


}

