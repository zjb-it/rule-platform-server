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
import com.zjb.ruleplatform.entity.dto.*;
import com.zjb.ruleplatform.entity.vo.RuleDetail;
import com.zjb.ruleplatform.entity.vo.RuleInfo;
import com.zjb.ruleplatform.service.RuleService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.validation.Valid;

/**
 * @author 赵静波
 * @date 2021-02-02 17:18:45
 */
@RestController
@Api(tags = "规则引擎-规则")
@RequestMapping("/ruleEngine/rule")
public class RuleController {
    @Resource
    private RuleService ruleService;

    /**
     * 分页查询条件
     *
     * @return lis
     */
    @PostMapping("/page")
    @ApiOperation("分页查询")
    public PageResult<RuleInfo> list(@RequestBody PageRequest<String> pageRequest) {
        return ruleService.pageRule(pageRequest);
    }


    /**
     * 添加
     *
     * @param add 条件参数
     * @return true表示添加成功
     */
    @ApiOperation("添加")
    @PostMapping("/add")
    public PlainResult<Long> add(@Valid @RequestBody AddRuleRequest add) {
        return new PlainResult<>(ruleService.addRule(add));
    }

    /**
     * 添加
     *
     * @param add 条件参数
     * @return true表示添加成功
     */
    @ApiOperation("更新")
    @PostMapping("/update")
    public PlainResult<Long> update(@Valid @RequestBody AddRuleRequest add) {
        return new PlainResult<>(ruleService.updateRule(add));
    }


    @ApiOperation("删除")
    @GetMapping("/delete")
    public PlainResult<Boolean> add(@RequestParam Long id) {
        return new PlainResult<>(ruleService.delRule(id));
    }


    /**
     * 根据id查询
     *
     * @return data
     */
    @ApiOperation("根据id查询")
    @GetMapping("/get")
    public PlainResult<RuleDetail> get(Long id) {
        return new PlainResult<>(ruleService.getRule(id));
    }

    @ApiOperation("测试")
    @PostMapping("/test")
    public PlainResult<Object> test(@RequestBody @Valid RuleTest ruleTest) {
        return new PlainResult<>(ruleService.testRule(ruleTest));
    }

    @ApiOperation("发布")
    @GetMapping("/publish")
    public PlainResult<Object> publish(@RequestParam Long ruleId) {
        return new PlainResult<>(ruleService.publish(ruleId));
    }

    ///**
    // * 根据Ids批量查询条件
    // *
    // * @param ids 条件id
    // * @return data
    // */
    //@ApiOperation("根据Ids批量查询条件")
    //@PostMapping("/getByIds")
    //public ListResult<IdAndName> getByIds(@RequestBody IdsRequest ids) {
    //    ListResult<IdAndName> result = new ListResult<>();
    //    List<IdAndName> elementResponses = ruleEngineConditionService.getByIds(ids.getIds());
    //    result.setData(elementResponses);
    //    return result;
    //}
}