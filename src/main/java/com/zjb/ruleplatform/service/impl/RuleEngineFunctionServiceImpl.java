package com.zjb.ruleplatform.service.impl;


import com.google.common.collect.ConcurrentHashMultiset;
import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.zjb.ruleengine.core.config.FunctionHolder;
import com.zjb.ruleengine.core.enums.DataTypeEnum;
import com.zjb.ruleengine.core.function.Function;
import com.zjb.ruleplatform.entity.common.PageRequest;
import com.zjb.ruleplatform.entity.common.PageResult;
import com.zjb.ruleplatform.entity.vo.FunctionDetailVo;
import com.zjb.ruleplatform.service.RuleEngineFunctionService;
import com.zjb.ruleplatform.util.DataTypeUtils;
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
    private FunctionHolder functionHolder;

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
    public PageResult<FunctionDetailVo> functionLookUp(String name,String valueDataType) {
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
        PageResult<FunctionDetailVo> result = new PageResult<>();
        result.setData(data);
        return result;

    }
}
