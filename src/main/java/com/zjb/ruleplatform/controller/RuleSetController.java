package com.zjb.ruleplatform.controller;

import com.zjb.ruleplatform.entity.common.PageRequest;
import com.zjb.ruleplatform.entity.common.PageResult;
import com.zjb.ruleplatform.entity.common.PlainResult;
import com.zjb.ruleplatform.entity.dto.*;
import com.zjb.ruleplatform.entity.vo.RuleSetConfig;
import com.zjb.ruleplatform.service.ElementService;
import com.zjb.ruleplatform.service.RuleSetService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.validation.Valid;
import java.util.List;

/**
 * @author 赵静波
 * @date 2021-01-26 10:36:34
 */
@RestController
@RequestMapping("/ruleEngine/ruleSet")
@Api(tags = "规则引擎规则集")
@Slf4j
public class RuleSetController {

    @Resource
    private RuleSetService ruleSetService;

    @ApiOperation("规则集查询")
    @PostMapping("/page")
    public PageResult<RuleSetDef> selectElementPageList(@RequestBody PageRequest<String> pageRequest) {
        return ruleSetService.pageRuleSet(pageRequest);
    }

    /**
     * 新增
     *
     * @return ElementResponse
     */
    @ApiOperation("新增规则集定义")
    @PostMapping("/addDef")
    public PlainResult<Long> addDef(@RequestBody @Valid RuleSetDef ruleSetDef) {

        return new PlainResult<>(ruleSetService.addDef(ruleSetDef));
    }

    /**
     * 新增
     *
     * @return ElementResponse
     */
    @ApiOperation("新增规则集定义")
    @PostMapping("/updateDef")
    public PlainResult<Boolean> updateDef(@RequestBody @Valid RuleSetDef ruleSetDef) {

        return new PlainResult<>(ruleSetService.updateDef(ruleSetDef));
    }

    /**
     * @param id id
     * @return
     */
    @ApiOperation("根据Id查询规则集定义")
    @GetMapping("/getDef")
    public PlainResult<RuleSetDef> getDefById(@RequestParam  Long id) {
        return new PlainResult<>(ruleSetService.getDef(id));
    }


    /**
     * @param id id
     * @return
     */
    @ApiOperation("删除")
    @GetMapping("/delete")
    public PlainResult<Boolean> delete(@RequestParam  Long id) {
        return new PlainResult<>(ruleSetService.deleteRuleSet(id));
    }

    /**
     * @param id id
     * @return
     */
    @ApiOperation("根据Id查询规则集定义")
    @GetMapping("/getRuleSetConfig")
    public PlainResult<RuleSetConfig> getRuleSetConfig(@RequestParam  Long id) {
        return new PlainResult<>(ruleSetService.getRuleSetConfig(id));
    }
    /**
     * @return
     */
    @ApiOperation("增加配置")
    @GetMapping("/addRuleSetConfig")
    public PlainResult<Boolean> addRuleSetConfig(@RequestParam  RuleSetConfig ruleSetConfig) {
        return new PlainResult<>(ruleSetService.addRuleSetConfig(ruleSetConfig));
    }

    /**
     * @return
     */
    @ApiOperation("更新配置")
    @GetMapping("/updateRuleSetConfig")
    public PlainResult<Boolean> updateRuleSetConfig(@RequestParam  RuleSetConfig ruleSetConfig) {
        return new PlainResult<>(ruleSetService.updateRuleSetConfig(ruleSetConfig));
    }
}
