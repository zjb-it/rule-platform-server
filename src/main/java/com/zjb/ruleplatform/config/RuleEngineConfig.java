package com.zjb.ruleplatform.config;

import cn.hutool.core.collection.CollUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.zjb.ruleengine.core.DefaultRuleEngine;
import com.zjb.ruleengine.core.config.FunctionHolder;
import com.zjb.ruleengine.core.enums.DataTypeEnum;
import com.zjb.ruleengine.core.function.Function;
import com.zjb.ruleengine.core.function.HttpFunction;
import com.zjb.ruleengine.core.function.HttpJsonObjectFunction;
import com.zjb.ruleplatform.entity.RuleEngineFunction;
import com.zjb.ruleplatform.entity.RuleEngineFunctionParam;
import com.zjb.ruleplatform.entity.common.PageResult;
import com.zjb.ruleplatform.entity.vo.FunctionDetailVo;
import com.zjb.ruleplatform.manager.RuleEngineFunctionManager;
import com.zjb.ruleplatform.manager.RuleEngineFunctionParamManager;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @author 赵静波
 * Created on 2021-01-18
 */
@Component
public class RuleEngineConfig {

    @Autowired
    private RuleEngineFunctionManager functionManager;
    @Autowired
    private RuleEngineFunctionParamManager functionParamManager;

    @Bean
    public DefaultRuleEngine ruleEngine() {
        return new DefaultRuleEngine();
    }

    @Bean
    public FunctionHolder functionHolder(DefaultRuleEngine ruleEngine, ApplicationContext applicationContext) {

        final FunctionHolder functionHolder = ruleEngine.getFunctionHolder();
        //注册系统带的
        final Map<String, Function> beansOfType = applicationContext.getBeansOfType(Function.class);
        if (CollUtil.isNotEmpty(beansOfType)) {
            beansOfType.values().stream().forEach(function -> {
                functionHolder.registerFunction(function);
            });
        }

        final List<RuleEngineFunction> ruleEngineFunctions = functionManager.list();
        if (CollUtil.isEmpty(ruleEngineFunctions)) {
            return functionHolder;
        }
        //注册http function
        final Map<Long, List<RuleEngineFunctionParam>> funParamMap = functionParamManager.lambdaQuery()
                .list()
                .stream()
                .collect(Collectors.groupingBy(RuleEngineFunctionParam::getFunctionId));

        ruleEngineFunctions.stream().forEach(fun -> {
            functionHolder.registerFunction(getFunction(fun, funParamMap.get(fun.getId())));
        });

        //ruleEngineFunctions.stream().forEach(fun -> {
        //    final FunctionDetailVo vo = new FunctionDetailVo();
        //    vo.setName(fun.getCode());
        //    vo.setDescription(fun.getName());
        //    if (funParamMap.containsKey(fun.getId())) {
        //        final List<FunctionDetailVo.VariablesBean> params = funParamMap.get(fun.getId()).stream().map(param -> {
        //            final FunctionDetailVo.VariablesBean bean = new FunctionDetailVo.VariablesBean();
        //            bean.setValueDataType(param.getValueDataType());
        //            bean.setDescription(param.getFunctionParamName());
        //            bean.setName(param.getFunctionParamCode());
        //            return bean;
        //        }).collect(Collectors.toList());
        //        vo.setVariables(params);
        //
        //    }
        //    data.add(vo);
        //});
        return functionHolder;
    }

    private Function getFunction(RuleEngineFunction function, List<RuleEngineFunctionParam> functionParams) {
        if (function.getValueDataType().equals(DataTypeEnum.JSONOBJECT.name())) {
            return new HttpJsonObjectFunction() {
                @Override
                protected String getUrl() {
                    return function.getUrl();
                }

                @Override
                public String getName() {
                    return function.getCode();
                }

                @Override
                public List<Parameter> getParamters() {
                    if (CollUtil.isEmpty(functionParams)) {
                        return Collections.EMPTY_LIST;
                    }
                    return functionParams.stream()
                            .map(param -> new Parameter(DataTypeEnum.getDataTypeByName(param.getValueDataType()), param.getFunctionParamCode()))
                            .collect(Collectors.toList());
                }

                @Override
                public Class getParameterClass() {
                    return Map.class;
                }
            };
        }
        return parseFunction(function,functionParams);
    }

    private Function parseFunction(RuleEngineFunction function, List<RuleEngineFunctionParam> functionParams) {
        return new HttpFunction() {
            @Override
            protected String getUrl() {
                return function.getUrl();
            }

            @Override
            public String getName() {
                return function.getCode();
            }

            @Override
            public List<Parameter> getParamters() {
                if (CollUtil.isEmpty(functionParams)) {
                    return Collections.EMPTY_LIST;
                }
                return functionParams.stream()
                        .map(param -> new Parameter(DataTypeEnum.getDataTypeByName(param.getValueDataType()), param.getFunctionParamCode()))
                        .collect(Collectors.toList());
            }

            @Override
            public Class getResultClass() {
                return DataTypeEnum.getDataTypeByName(function.getValueDataType()).getClazz();
            }

            @Override
            public Class getParameterClass() {
                return Map.class;
            }
        };
    }

}
