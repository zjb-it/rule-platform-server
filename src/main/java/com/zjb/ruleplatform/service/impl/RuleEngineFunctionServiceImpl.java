package com.zjb.ruleplatform.service.impl;


import com.google.common.collect.Lists;
import com.zjb.ruleengine.core.DefaultRuleEngine;
import com.zjb.ruleengine.core.config.FunctionHolder;
import com.zjb.ruleengine.core.enums.DataTypeEnum;
import com.zjb.ruleengine.core.function.Function;
import com.zjb.ruleplatform.entity.common.PageRequest;
import com.zjb.ruleplatform.entity.common.PageResult;
import com.zjb.ruleplatform.entity.vo.FunctionDetailVo;
import com.zjb.ruleplatform.entity.vo.FunctionVo;
import com.zjb.ruleplatform.service.RuleEngineFunctionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author yuzhiji
 */
@Service
public class RuleEngineFunctionServiceImpl implements RuleEngineFunctionService {

    @Autowired
    private DefaultRuleEngine ruleEngine;
    private static final String name = "GetPropertyFunction";
    private static final String description = "获取对象属性";
    private static final String object = "对象";
    private static final String fieldName = "对象属性code";

    private static FunctionDetailVo functionVo;

    static {
        functionVo = new FunctionDetailVo();
        functionVo.setDescription(description);
        functionVo.setName(name);

        FunctionDetailVo.VariablesBean var = new FunctionDetailVo.VariablesBean();
        var.setName("object");
        var.setDescription(object);
        var.setValueDataType(DataTypeEnum.JSONOBJECT.name());
        FunctionDetailVo.VariablesBean var1 = new FunctionDetailVo.VariablesBean();
        var1.setName("fieldName");
        var1.setDescription(fieldName);
        var1.setValueDataType(DataTypeEnum.STRING.name());
        functionVo.setVariables(Lists.newArrayList(var, var1));
    }

    @Override
    public PageResult<FunctionDetailVo> functionLookUp(PageRequest<String> pageRequest) {
        final FunctionHolder functionHolder = ruleEngine.getFunctionHolder();
        //todo
        //final Map<String, Function> functions = functionHolder.getFunctions();
        final ArrayList<FunctionDetailVo> objects = Lists.newArrayList(functionVo);
        //functions.forEach((k, v) -> {
        //    final FunctionDetailVo functionVo = new FunctionDetailVo();
        //    functionVo.setDescription(description);
        //    functionVo.setName(k);
        //    final List<Function.Parameter> list = v.listParamters();
        //    final List<FunctionDetailVo.VariablesBean> collect = list.stream().map(p ->
        //            new FunctionDetailVo.VariablesBean(p.getName(), description, p.getDataTypeEnum().name())
        //    ).collect(Collectors.toList());
        //    functionVo.setVariables(collect);
        //    objects.add(functionVo);
        //});
        PageResult<FunctionDetailVo> result = new PageResult<>();
        result.setData(objects);
        return result;

    }
}
