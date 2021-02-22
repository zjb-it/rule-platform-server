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
package com.zjb.ruleplatform.controller.open;

import com.zjb.ruleplatform.entity.common.PageRequest;
import com.zjb.ruleplatform.entity.common.PageResult;
import com.zjb.ruleplatform.entity.common.PlainResult;
import com.zjb.ruleplatform.entity.dto.AddRuleRequest;
import com.zjb.ruleplatform.entity.dto.RuleExecuteParam;
import com.zjb.ruleplatform.entity.dto.RuleTest;
import com.zjb.ruleplatform.entity.vo.RuleDetail;
import com.zjb.ruleplatform.entity.vo.RuleInfo;
import com.zjb.ruleplatform.service.RuleExecuteService;
import com.zjb.ruleplatform.service.RuleService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.validation.Valid;

/**
 * @author 赵静波
 * @date 2021-02-10 20:05:38
 */
@RestController
@Api(tags = "规则执行")
@RequestMapping("/open/ruleEngine/rule")
public class RuleExecuteController {
    @Resource
    private RuleExecuteService executeService;

    /**
     * 分页查询条件
     *
     * @return lis
     */
    @PostMapping("/execute")
    @ApiOperation("规则执行")
    public PlainResult<Object> list(@RequestBody RuleExecuteParam param) {
        return new PlainResult<>(executeService.execute(param));
    }



}