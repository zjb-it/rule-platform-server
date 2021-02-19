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

import com.zjb.ruleplatform.entity.common.PageRequest;
import com.zjb.ruleplatform.entity.common.PageResult;
import com.zjb.ruleplatform.entity.common.PlainResult;
import com.zjb.ruleplatform.entity.dto.ListRuleEngineVariableRequest;
import com.zjb.ruleplatform.entity.dto.RuleEngineVariableRequest;
import com.zjb.ruleplatform.entity.vo.GetRuleEngineVariableResponse;
import com.zjb.ruleplatform.entity.vo.ListRuleEngineVariableResponse;
import com.zjb.ruleplatform.service.VariableService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.validation.Valid;

/**
 * @author 赵静波
 * @date 2021-01-26 10:36:34
 */
@RestController
@Api(tags = "规则引擎变量")
@RequestMapping("/ruleEngine/variable")
@Slf4j
public class VariableController {

    @Resource
    private VariableService variableService;

    /**
     * 分页查询变量
     *
     * @param pageRequest 分页参数
     * @return list
     */
    @PostMapping("/list")
    @ApiOperation("分页查询变量")
    public PageResult<ListRuleEngineVariableResponse> list(@ApiParam @RequestBody PageRequest<ListRuleEngineVariableRequest> pageRequest) {
        return variableService.listVariable(pageRequest);
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
        return new PlainResult<>(variableService.addVariable(add));
    }


    /**
     * @return
     */
    @ApiOperation("获取变量")
    @GetMapping("/get")
    public PlainResult<GetRuleEngineVariableResponse> getVariable(@RequestParam Long id) {
        return new PlainResult<>(variableService.getVariable(id));
    }


}