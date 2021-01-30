package com.zjb.ruleplatform.controller;

import com.google.common.collect.Lists;
import com.zjb.ruleengine.core.DefaultRuleEngine;
import com.zjb.ruleengine.core.RuleEngine;
import com.zjb.ruleengine.core.config.FunctionHolder;
import com.zjb.ruleengine.core.function.Function;
import com.zjb.ruleplatform.entity.common.ListResult;
import com.zjb.ruleplatform.entity.common.PageRequest;
import com.zjb.ruleplatform.entity.common.PageResult;
import com.zjb.ruleplatform.entity.common.PlainResult;
import com.zjb.ruleplatform.entity.dto.AddHttpFunction;
import com.zjb.ruleplatform.entity.vo.FunctionDetailVo;
import com.zjb.ruleplatform.entity.vo.FunctionVo;
import com.zjb.ruleplatform.service.RuleEngineFunctionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/ruleEngine/function")
public class FunctionController {


    @Autowired
    private RuleEngineFunctionService functionService;

    /**
     * 分页查询条件
     *
     * @return lis
     */
    @GetMapping("/list")
    public ListResult<FunctionDetailVo> list(String name,@RequestParam(defaultValue = "POJO") String valueDataType) {
        final PageResult<FunctionDetailVo> result = functionService.functionLookUp(name,valueDataType);
        return result;
    }

    @PostMapping("/register")
    public PlainResult<Boolean> register(@RequestBody AddHttpFunction addHttpFunction) {
        final Boolean result = functionService.registerHttpFunction(addHttpFunction);
        return new PlainResult<>(result);
    }

        @PostMapping("/update")
    public PlainResult<Boolean> update(@RequestBody AddHttpFunction addHttpFunction) {
        final Boolean result = functionService.updateHttpFunction(addHttpFunction);
        return new PlainResult<>(result);
    }

    @GetMapping("/delete")
    public PlainResult<Boolean> delete(Long id) {
        final Boolean result = functionService.deleteHttpFunction(id);
        return new PlainResult<>(result);
    }
    @PostMapping("/page")
    public PageResult<AddHttpFunction> page(@RequestBody PageRequest<String> pageRequest) {
        return functionService.pageHttpFunction(pageRequest);
    }
}
