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
package com.zjb.ruleplatform.controller;

import com.zjb.ruleengine.core.enums.DataTypeEnum;
import com.zjb.ruleplatform.entity.common.PageRequest;
import com.zjb.ruleplatform.entity.common.PageResult;
import com.zjb.ruleplatform.entity.common.PlainResult;
import com.zjb.ruleplatform.entity.dto.IdLRequest;
import com.zjb.ruleplatform.entity.dto.IdsRequest;
import com.zjb.ruleplatform.entity.dto.ListRuleEngineVariableRequest;
import com.zjb.ruleplatform.entity.dto.RuleEngineVariableRequest;
import com.zjb.ruleplatform.entity.vo.GetRuleEngineVariableResponse;
import com.zjb.ruleplatform.entity.vo.ListRuleEngineVariableResponse;
import com.zjb.ruleplatform.service.RuleEngineVariableService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.validation.Valid;
import javax.validation.ValidationException;

/**
 * 〈一句话功能简述〉<br>
 * 〈变量〉
 *
 * @author v-dingqianwen.ea
 * @create 2019/8/6
 * @since 1.0.0
 */
@RestController
@Api(tags = "规则引擎变量")
@RequestMapping("/ruleEngine/variable")
@Slf4j
public class RuleEngineVariableController {

    @Resource
    private RuleEngineVariableService ruleEngineVariableService;

    /**
     * 分页查询变量
     *
     * @param pageRequest 分页参数
     * @return list
     */
    @PostMapping("/list")
    @ApiOperation("分页查询变量")
    public PageResult<ListRuleEngineVariableResponse> list(@ApiParam @RequestBody PageRequest<ListRuleEngineVariableRequest> pageRequest) {
        return ruleEngineVariableService.listVariable(pageRequest);
    }


    /**
     * 添加变量
     *
     * @param add
     * @return
     */
    @ApiOperation("添加变量")
    @PostMapping("/add")
    public PlainResult<GetRuleEngineVariableResponse> add(@ApiParam @Valid @RequestBody RuleEngineVariableRequest add) {
        return new PlainResult<>(ruleEngineVariableService.addVariable(add));
    }


    /**
     * @param commonIdParam
     * @return
     */
    @ApiOperation("获取变量")
    @PostMapping("/get")
    public PlainResult<GetRuleEngineVariableResponse> getVariable(@RequestBody IdLRequest commonIdParam) {
        return new PlainResult<>(ruleEngineVariableService.getVariable(commonIdParam.getId()));
    }


}