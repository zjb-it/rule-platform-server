package com.zjb.ruleplatform.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.additional.query.impl.LambdaQueryChainWrapper;
import com.founder.ego.common.request.PageBase;
import com.founder.ego.common.request.PageRequest;
import com.founder.ego.common.response.PageResponse;
import com.founder.ego.common.response.PageResult;
import com.founder.ego.common.response.Rows;
import com.founder.ego.service.ruleengine.RuleEngineFunctionService;
import com.founder.ego.store.bpm.entity.RuleEngineFunction;
import com.founder.ego.store.bpm.entity.RuleEngineFunctionParam;
import com.founder.ego.store.bpm.manager.RuleEngineFunctionManager;
import com.founder.ego.store.bpm.manager.RuleEngineFunctionParamManager;
import com.founder.ego.vo.ruleengine.CommonFunctionParam;
import com.founder.ego.vo.ruleengine.RuleEngineFunctionResponse;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static com.founder.ego.enumbean.DeletedEnum.ENABLE;

/**
 * @author yuzhiji
 */
@Service
public class RuleEngineFunctionServiceImpl implements RuleEngineFunctionService {

    @Resource
    private RuleEngineFunctionManager ruleEngineFunctionManager;

    @Resource
    private RuleEngineFunctionParamManager ruleEngineFunctionParamManager;

    @Override
    public  PageResult<RuleEngineFunctionResponse> functionLookUp(PageRequest<CommonFunctionParam> pageRequest){
        PageBase page= pageRequest.getPage();
        String name = pageRequest.getQuery().getName();
        String resultValueType = pageRequest.getQuery().getFunctionReturnType();
        IPage<RuleEngineFunction> pageInfo;
        LambdaQueryChainWrapper<RuleEngineFunction> temp = ruleEngineFunctionManager.lambdaQuery().eq(RuleEngineFunction::getDeleted, ENABLE.getStatus()).eq(RuleEngineFunction::getShowed, ENABLE.getStatus());
        if(StringUtils.isNotEmpty(name)){
            temp.like(RuleEngineFunction::getFunctionName, name);
        }
        if(StringUtils.isNotEmpty(resultValueType)){
            List<String> functionTypeList = Arrays.asList(resultValueType.split(","));
            temp.in(RuleEngineFunction::getResultValueType, functionTypeList);
        }
        pageInfo = temp.page(new Page<>(page.getPageIndex(), page.getPageSize()));

        List<RuleEngineFunction> tagData= pageInfo.getRecords();
        List<RuleEngineFunctionParam> ruleEngineFunctionParamList =
                ruleEngineFunctionParamManager.lambdaQuery().eq(RuleEngineFunctionParam::getDeleted, ENABLE.getStatus()).list();
        List<RuleEngineFunctionResponse> ruleEngineFunctionResponseList = tagData.stream().map(function ->{
            Long functionId = function.getId();
            RuleEngineFunctionResponse ruleEngineFunctionResponse = new RuleEngineFunctionResponse();
            ruleEngineFunctionResponse.setId(functionId.intValue());
            ruleEngineFunctionResponse.setName(function.getFunctionName());
            ruleEngineFunctionResponse.setResultValueType(function.getResultValueType());
            List<RuleEngineFunctionResponse.VariableBean> variableBeanList = new ArrayList<>();
            for(RuleEngineFunctionParam ruleEngineFunctionParam : ruleEngineFunctionParamList){
                if(ruleEngineFunctionParam.getFunctionId().equals(functionId)){
                    RuleEngineFunctionResponse.VariableBean variableBean = new RuleEngineFunctionResponse.VariableBean();
                    variableBean.setCode(ruleEngineFunctionParam.getFunctionParamCode());
                    variableBean.setName(ruleEngineFunctionParam.getFunctionParamName());
                    variableBean.setValueType(ruleEngineFunctionParam.getType());
                    variableBeanList.add(variableBean);
                }
            }
            ruleEngineFunctionResponse.setVariables(variableBeanList);
            return ruleEngineFunctionResponse;
        }).collect(Collectors.toList());
        PageResult<RuleEngineFunctionResponse> result = new PageResult<>();
        result.setData(new Rows<>(ruleEngineFunctionResponseList,new PageResponse(page.getPageIndex(),page.getPageSize(),pageInfo.getTotal())));
        return result;
    }
}
