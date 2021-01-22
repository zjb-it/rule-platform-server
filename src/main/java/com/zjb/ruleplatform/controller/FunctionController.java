package com.zjb.ruleplatform.controller;

import com.google.common.collect.Lists;
import com.zjb.ruleengine.core.DefaultRuleEngine;
import com.zjb.ruleengine.core.RuleEngine;
import com.zjb.ruleengine.core.config.FunctionHolder;
import com.zjb.ruleengine.core.function.Function;
import com.zjb.ruleplatform.entity.common.ListResult;
import com.zjb.ruleplatform.entity.common.PageRequest;
import com.zjb.ruleplatform.entity.common.PageResult;
import com.zjb.ruleplatform.entity.vo.FunctionDetailVo;
import com.zjb.ruleplatform.entity.vo.FunctionVo;
import com.zjb.ruleplatform.service.RuleEngineFunctionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
    @PostMapping("/list")
    public PageResult<FunctionDetailVo> list(@RequestBody PageRequest<String> pageRequest) {
        final PageResult<FunctionDetailVo> result = functionService.functionLookUp(pageRequest);
        return result;
    }
}
