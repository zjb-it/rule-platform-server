package com.zjb.ruleplatform.controller;

import com.google.common.collect.Lists;
import com.zjb.ruleplatform.entity.common.ListResult;
import com.zjb.ruleplatform.entity.common.PageRequest;
import com.zjb.ruleplatform.entity.common.PageResult;
import com.zjb.ruleplatform.entity.vo.FunctionVo;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/ruleEngine/function")
public class FunctionController {

    /**
     * 分页查询条件
     *
     * @return lis
     */
    @PostMapping("/list")
    public ListResult<FunctionVo> list(@RequestBody PageRequest<String> pageRequest) {
        FunctionVo functionVo = new FunctionVo();
        functionVo.setName("函数1");
        functionVo.setVariables(Lists.newArrayList(new FunctionVo.VariablesBean("arg1", "OBJECT"), new FunctionVo.VariablesBean("arg2", "STRING")));

        FunctionVo functionVo1 = new FunctionVo();
        functionVo1.setName("函数2");
        functionVo1.setVariables(Lists.newArrayList(new FunctionVo.VariablesBean("arg3", "OBJECT"), new FunctionVo.VariablesBean("arg4", "STRING")));

        return new ListResult<>(Lists.newArrayList(functionVo,functionVo1));
    }
}
