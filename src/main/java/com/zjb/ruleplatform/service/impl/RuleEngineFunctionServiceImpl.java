package com.zjb.ruleplatform.service.impl;

import java.util.*;


import cn.hutool.core.collection.CollUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.conditions.query.LambdaQueryChainWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.zjb.ruleengine.core.config.FunctionHolder;
import com.zjb.ruleengine.core.enums.DataTypeEnum;
import com.zjb.ruleengine.core.function.Function;
import com.zjb.ruleplatform.entity.RuleEngineFunction;
import com.zjb.ruleplatform.entity.RuleEngineFunctionParam;
import com.zjb.ruleplatform.entity.common.PageRequest;
import com.zjb.ruleplatform.entity.common.PageResult;
import com.zjb.ruleplatform.entity.dto.AddHttpFunction;
import com.zjb.ruleplatform.entity.vo.FunctionDetailVo;
import com.zjb.ruleplatform.manager.RuleEngineFunctionManager;
import com.zjb.ruleplatform.manager.RuleEngineFunctionParamManager;
import com.zjb.ruleplatform.service.RuleEngineFunctionService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.stream.Collectors;

/**
 * @author yuzhiji
 */
@Service
public class RuleEngineFunctionServiceImpl implements RuleEngineFunctionService {

    @Autowired
    private FunctionHolder functionHolder;
    @Autowired
    private RuleEngineFunctionManager functionManager;
    @Autowired
    private RuleEngineFunctionParamManager functionParamManager;

    //funciton的别名，可以自己起
    private static final Map<String, String> functionDesc = Maps.newHashMap();
    private static final HashBasedTable<String, String, String> functionPropertyDesc = HashBasedTable.create();

    static {
        functionDesc.put("GetObjectPropertyFunction", "获取java对象属性");
        functionDesc.put("GetJsonPropertyFunction", "获取json对象属性");

        functionPropertyDesc.put("GetObjectPropertyFunction", "object", "java对象");
        functionPropertyDesc.put("GetObjectPropertyFunction", "fieldName", "对象属性code");
        functionPropertyDesc.put("GetJsonPropertyFunction", "jsonNode", "JSON对象");
        functionPropertyDesc.put("GetJsonPropertyFunction", "fieldName", "对象属性code");
    }

    @Override
    public PageResult<FunctionDetailVo> functionLookUp(String name, String valueDataType) {
        final DataTypeEnum dataTypeByName = DataTypeEnum.getDataTypeByName(valueDataType);
        final Map<String, Function> functions = functionHolder.getFunctions();
        final ArrayList<FunctionDetailVo> data = Lists.newArrayList();
        functions.forEach((k, v) -> {
            if (DataTypeEnum.getDataTypeByClass(v.getResultClass()).getClazz().isAssignableFrom(dataTypeByName.getClazz())) {
                final FunctionDetailVo functionVo = new FunctionDetailVo();
                functionVo.setDescription(functionDesc.get(k));
                functionVo.setName(k);
                final List<Function.Parameter> list = v.getParamters();
                final List<FunctionDetailVo.VariablesBean> collect = list.stream().map(p ->
                        new FunctionDetailVo.VariablesBean(p.getName(), functionPropertyDesc.get(k, p.getName()), p.getDataTypeEnum().name())
                ).collect(Collectors.toList());
                functionVo.setVariables(collect);
                data.add(functionVo);
            }
        });
        FunctionDetailVo testFun = new FunctionDetailVo();
        testFun.setDescription("测试funtion");
        testFun.setName("TestFuntion");
        List<FunctionDetailVo.VariablesBean> testFunPar = Lists.newArrayList();
        testFunPar.add(new FunctionDetailVo.VariablesBean("boolean", "布尔", DataTypeEnum.BOOLEAN.name()));
        testFunPar.add(new FunctionDetailVo.VariablesBean("collection", "集合", DataTypeEnum.COLLECTION.name()));
        testFunPar.add(new FunctionDetailVo.VariablesBean("json", "json", DataTypeEnum.JSONOBJECT.name()));
        testFun.setVariables(testFunPar);
        data.add(testFun);
        PageResult<FunctionDetailVo> result = new PageResult<>();
        result.setData(data);
        return result;
    }

    @Override
    public Boolean registerHttpFunction(AddHttpFunction function) {
        final RuleEngineFunction ruleEngineFunction = new RuleEngineFunction();
        BeanUtils.copyProperties(function, ruleEngineFunction);
        ruleEngineFunction.setCodeName(function.getCode() + function.getName());
        functionManager.save(ruleEngineFunction);
        saveParams(function, ruleEngineFunction);
        return true;
    }

    private void saveParams(AddHttpFunction function, RuleEngineFunction ruleEngineFunction) {
        final List<RuleEngineFunctionParam> collect = function.getParams().stream().map(param -> {
            final RuleEngineFunctionParam functionParam = new RuleEngineFunctionParam();
            functionParam.setFunctionId(ruleEngineFunction.getId());
            functionParam.setFunctionParamCode(param.getCode());
            functionParam.setFunctionParamName(param.getName());
            functionParam.setValueDataType(param.getValueDataType());
            return functionParam;
        }).collect(Collectors.toList());

        functionParamManager.saveBatch(collect);
    }

    @Override
    public Boolean updateHttpFunction(AddHttpFunction function) {
        Objects.requireNonNull(function.getId());
        final RuleEngineFunction ruleEngineFunction = new RuleEngineFunction();
        BeanUtils.copyProperties(function, ruleEngineFunction);
        functionManager.updateById(ruleEngineFunction);
        deleteParam(function.getId());
        this.saveParams(function, ruleEngineFunction);
        return true;
    }

    @Override
    public PageResult<AddHttpFunction> pageHttpFunction(PageRequest<String> pageResult) {
        LambdaQueryWrapper<RuleEngineFunction> queryWrapper = new LambdaQueryWrapper<>();
        final String query = pageResult.getQuery();
        if (StringUtils.isNotBlank(query)) {
            queryWrapper.like(RuleEngineFunction::getCodeName, query);
        }
        final Page<RuleEngineFunction> page = functionManager.page(new Page<>(pageResult.getPage().getPageIndex(), pageResult.getPage().getPageSize()), queryWrapper);
        if (CollUtil.isEmpty(page.getRecords())) {
            return new PageResult<>();
        }
        final Set<Long> funIds = page.getRecords().stream().map(RuleEngineFunction::getId).collect(Collectors.toSet());
        final Map<Long, List<RuleEngineFunctionParam>> params = functionParamManager.lambdaQuery().in(RuleEngineFunctionParam::getFunctionId, funIds)
                .list().stream().collect(Collectors.groupingBy(RuleEngineFunctionParam::getFunctionId));

        PageResult<AddHttpFunction> result = new PageResult<>();
        result.setTotal(page.getTotal());
        final List<AddHttpFunction> collect = page.getRecords().stream().map(record -> {
            AddHttpFunction function = new AddHttpFunction();
            BeanUtils.copyProperties(record, function);
            function.setParams(params.get(record.getId()).stream()
                    .map(param -> new AddHttpFunction.Param(param.getFunctionParamCode(), param.getFunctionParamName(), param.getValueDataType()))
                    .collect(Collectors.toList()));
            return function;
        }).collect(Collectors.toList());
        result.setData(collect);

        return result;
    }

    @Override
    public Boolean deleteHttpFunction(Long id) {
        return functionManager.removeById(id) && deleteParam(id);
    }

    private Boolean deleteParam(Long id) {
        final LambdaQueryWrapper<RuleEngineFunctionParam> eq = new QueryWrapper<RuleEngineFunctionParam>()
                .lambda()
                .eq(RuleEngineFunctionParam::getFunctionId, id);
        return  functionParamManager.remove(eq);
    }
}
