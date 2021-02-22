package com.zjb.ruleplatform.controller;

import com.zjb.ruleplatform.entity.common.ListResult;
import com.zjb.ruleplatform.entity.common.PageRequest;
import com.zjb.ruleplatform.entity.common.PageResult;
import com.zjb.ruleplatform.entity.common.PlainResult;
import com.zjb.ruleplatform.entity.dto.AddHttpFunction;
import com.zjb.ruleplatform.entity.vo.FunctionDetailVo;
import com.zjb.ruleplatform.service.FunctionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * @author 赵静波
 * @date 2021-01-28 11:07:44
 */
@RestController
@RequestMapping("/ruleEngine/function")
public class FunctionController {


    @Autowired
    private FunctionService functionService;

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
