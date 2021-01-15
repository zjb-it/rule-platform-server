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
import com.zjb.ruleplatform.entity.common.ListResult;
import com.zjb.ruleplatform.entity.common.PageRequest;
import com.zjb.ruleplatform.entity.common.PageResult;
import com.zjb.ruleplatform.entity.common.PlainResult;
import com.zjb.ruleplatform.entity.dto.*;
import com.zjb.ruleplatform.service.RuleEngineConditionService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.validation.Valid;
import java.util.List;

/**
 * 〈一句话功能简述〉<br>
 * 〈条件〉
 *
 * @author v-dingqianwen.ea
 * @create 2019/8/6
 * @since 1.0.0
 */
@RestController
@Api(tags = "规则引擎条件")
@RequestMapping("/ruleEngine/condition")
public class RuleEngineConditionController {
    @Resource
    private RuleEngineConditionService ruleEngineConditionService;

    /**
     * 分页查询条件
     *
     * @return lis
     */
    @PostMapping("/list")
    @ApiOperation("分页查询条件")
    public PageResult<RuleEngineConditionResponse> list(@RequestBody PageRequest<String> pageRequest) {
        return ruleEngineConditionService.list(pageRequest);
    }


    /**
     * 添加条件
     *
     * @param add 条件参数
     * @return true表示添加成功
     */
    @ApiOperation("添加条件")
    @PostMapping("/add")
    public PlainResult<Boolean> add(@Valid @RequestBody AddRuleEngineConditionParam add) {
        PlainResult<Boolean> plainResult = new PlainResult<>();
        plainResult.setData(ruleEngineConditionService.add(add));
        return plainResult;
    }

    /**
     * 根据id查询
     *
     * @param idRequest 条件id
     * @return data
     */
    @ApiOperation("根据id查询")
    @PostMapping("/get")
    public PlainResult<AddRuleEngineConditionParam> get(@Valid @RequestBody IdLRequest idRequest) {
        PlainResult<AddRuleEngineConditionParam> plainResult = new PlainResult<>();
        plainResult.setData(ruleEngineConditionService.get(idRequest.getId()));
        return plainResult;
    }



    /**
     * 根据Ids批量查询条件
     *
     * @param ids 条件id
     * @return data
     */
    @ApiOperation("根据Ids批量查询条件")
    @PostMapping("/getByIds")
    public ListResult<IdAndName> getByIds(@RequestBody IdsRequest ids) {
        ListResult<IdAndName> result = new ListResult<>();
        List<IdAndName> elementResponses = ruleEngineConditionService.getByIds(ids.getIds());
        result.setData(elementResponses);
        return result;
    }
}