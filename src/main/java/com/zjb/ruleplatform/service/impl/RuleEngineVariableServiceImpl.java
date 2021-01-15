package com.zjb.ruleplatform.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.collection.CollectionUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.baomidou.mybatisplus.core.toolkit.StringPool;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zjb.ruleengine.core.RuleEngine;
import com.zjb.ruleengine.core.config.FunctionHolder;
import com.zjb.ruleengine.core.enums.DataTypeEnum;
import com.zjb.ruleengine.core.function.Function;
import com.zjb.ruleplatform.entity.*;
import com.zjb.ruleplatform.entity.common.PageRequest;
import com.zjb.ruleplatform.entity.common.PageResult;
import com.zjb.ruleplatform.entity.dto.ListRuleEngineVariableRequest;
import com.zjb.ruleplatform.entity.dto.RuleEngineVariableRequest;
import com.zjb.ruleplatform.entity.vo.FunctionBean;
import com.zjb.ruleplatform.entity.vo.GetRuleEngineVariableResponse;
import com.zjb.ruleplatform.entity.vo.ListRuleEngineVariableResponse;
import com.zjb.ruleplatform.manager.*;
import com.zjb.ruleplatform.service.RuleEngineVariableService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import javax.validation.ValidationException;
import java.util.*;
import java.util.stream.Collectors;


/**
 * @author yuzhiji
 */

@Service
@Slf4j
public class RuleEngineVariableServiceImpl implements RuleEngineVariableService {

    //@Autowired
    private FunctionHolder functionHolder= new FunctionHolder();
    @Resource
    private RuleEngineVariableManager ruleEngineVariableManager;
    @Resource
    private RuleEngineVariableParamManager ruleEngineVariableParamManager;


    @Override
    @Transactional(rollbackFor = Exception.class)
    public GetRuleEngineVariableResponse addVariable(RuleEngineVariableRequest param) {

        RuleEngineVariable ruleEngineVariable = new RuleEngineVariable();
        ruleEngineVariable.setName(param.getName());
        ruleEngineVariable.setValue(param.getValue());
        ruleEngineVariable.setDescription(param.getDescription());
        final Function function1 = functionHolder.getFunction(param.getValue());

        FunctionBean function = param.getFunction();
        ruleEngineVariableManager.save(ruleEngineVariable);
        saveBatchVariableParam(ruleEngineVariable, function, function1);

        /* 新增返回变量详情*/
        GetRuleEngineVariableResponse variableResponse = new GetRuleEngineVariableResponse();
        variableResponse.setDescription(param.getDescription());
        variableResponse.setName(param.getName());
        variableResponse.setValue(param.getValue());
        variableResponse.setId(ruleEngineVariable.getId().intValue());
        variableResponse.setValueDataType(function1.getFunctionResultType().getSimpleName());
        return variableResponse;
    }


    @Override
    public PageResult<ListRuleEngineVariableResponse> listVariable(PageRequest<ListRuleEngineVariableRequest> pageRequest) {
        ListRuleEngineVariableRequest ruleEngineVariableRequest = pageRequest.getQuery();
        PageRequest.PageBase page = pageRequest.getPage();
        QueryWrapper<RuleEngineVariable> queryWrapper = new QueryWrapper<>();

        List<String> valueDataType = ruleEngineVariableRequest.getValueDataType();

        if (CollectionUtils.isNotEmpty(valueDataType)) {
            queryWrapper.lambda().in(RuleEngineVariable::getValueDataType, valueDataType);
        }
        String queryName = ruleEngineVariableRequest.getName();
        if (StringUtils.isNotBlank(queryName)) {
            queryWrapper.lambda().like(RuleEngineVariable::getName, queryName);
        }

        //排序
        IPage<RuleEngineVariable> pageInfo = ruleEngineVariableManager.page(new Page<>(page.getPageIndex(), page.getPageSize()), queryWrapper);
        List<RuleEngineVariable> ruleEngineVariableList = pageInfo.getRecords();

        //获取相关的循环策略信息
        List<ListRuleEngineVariableResponse> ruleEngineVariableResponses = ruleEngineVariableList.stream()
                .map(ruleEngineVariable -> {
                    ListRuleEngineVariableResponse response = new ListRuleEngineVariableResponse();
                    response.setId(ruleEngineVariable.getId());
                    response.setName(ruleEngineVariable.getName());
                    response.setValue(ruleEngineVariable.getValue());
                    response.setValueDataType(ruleEngineVariable.getValueDataType());
                    return response;
                }).collect(Collectors.toList());
        PageResult<ListRuleEngineVariableResponse> pageResult = new PageResult<>();
        pageResult.setData(ruleEngineVariableResponses);
        return pageResult;
    }


    @Override
    @Transactional(rollbackFor = Exception.class)
    public GetRuleEngineVariableResponse getVariable(Long variableId) {
        RuleEngineVariable ruleEngineVariable = checkVariableId(variableId);
        GetRuleEngineVariableResponse ruleEngineVariableResponse = new GetRuleEngineVariableResponse();
        ruleEngineVariableResponse.setId(ruleEngineVariable.getId().intValue());
        ruleEngineVariableResponse.setDescription(ruleEngineVariable.getDescription());
        ruleEngineVariableResponse.setName(ruleEngineVariable.getName());

        ruleEngineVariableResponse.setValueDataType(ruleEngineVariable.getValueDataType());
        ruleEngineVariableResponse.setValue(ruleEngineVariable.getValue());

        FunctionBean functionBean = new FunctionBean();
        functionBean.setName(ruleEngineVariable.getValue());
        List<RuleEngineVariableParam> ruleEngineFunctionParamList = ruleEngineVariableParamManager
                .lambdaQuery()
                .eq(RuleEngineVariableParam::getVariableId, variableId)
                .list();
        List<FunctionBean.VariablesBean> variableBeanList = ruleEngineFunctionParamList.stream().map(ruleEngineFunctionParam -> {
            FunctionBean.VariablesBean variablesBean = new FunctionBean.VariablesBean();
            variablesBean.setCode(ruleEngineFunctionParam.getFunctionParamCode());
            variablesBean.setName(ruleEngineFunctionParam.getFunctionParamName());
            variablesBean.setValueDataType(ruleEngineFunctionParam.getFunctionParamDataType());
            final String functionParamType = ruleEngineFunctionParam.getFunctionParamType();
            variablesBean.setValueType(functionParamType);
            variablesBean.setValue(ruleEngineFunctionParam.getFunctionParamValue());
            variablesBean.setValueName(ruleEngineFunctionParam.getFunctionParamValue());

            return variablesBean;
        }).collect(Collectors.toList());
        functionBean.setVariables(variableBeanList);
        ruleEngineVariableResponse.setFunction(functionBean);
        return ruleEngineVariableResponse;

    }

    private void saveBatchVariableParam(RuleEngineVariable ruleEngineVariable, FunctionBean functionParam, Function function1) {
        List<RuleEngineVariableParam> ruleEngineVariableParamList = new ArrayList<>();
        final List<FunctionBean.VariablesBean> variablesBeanList = functionParam.getVariables();
        final List<Function.Parameter> funParameters = function1.listParamters();
        //todo 校验入参和函数的参数是否相对应
        if (variablesBeanList != null) {
            for (FunctionBean.VariablesBean variablesBean : variablesBeanList) {
                String value = variablesBean.getValue();

                RuleEngineVariableParam ruleEngineVariableParam = new RuleEngineVariableParam();
                ruleEngineVariableParam.setFunctionName(functionParam.getName());
                ruleEngineVariableParam.setVariableId(ruleEngineVariable.getId());
                ruleEngineVariableParam.setFunctionParamType(variablesBean.getValueType());
                ruleEngineVariableParam.setFunctionParamCode(variablesBean.getCode());
                ruleEngineVariableParam.setFunctionParamValue(value);

                ruleEngineVariableParam.setFunctionParamDataType(variablesBean.getValueDataType());

                ruleEngineVariableParamList.add(ruleEngineVariableParam);
            }
            ruleEngineVariableParamManager.saveBatch(ruleEngineVariableParamList);
        }

    }

    private RuleEngineVariable checkVariableId(Long variableId) {
        return Optional.ofNullable(ruleEngineVariableManager.getById(variableId))
                .orElseThrow(() -> new ValidationException("不存在对应变量id"));
    }


}

