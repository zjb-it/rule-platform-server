package com.zjb.ruleplatform.controller;

import com.zjb.ruleengine.core.enums.DataTypeEnum;
import com.zjb.ruleplatform.entity.common.ListResult;
import com.zjb.ruleplatform.entity.common.PageRequest;
import com.zjb.ruleplatform.entity.common.PageResult;
import com.zjb.ruleplatform.entity.common.PlainResult;
import com.zjb.ruleplatform.entity.dto.*;
import com.zjb.ruleplatform.service.ElementService;
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
@RequestMapping("/ruleEngine/element")
@Api(tags = "规则引擎元素")
@Slf4j
public class ElementController {

    @Resource
    private ElementService elementService;

    @ApiOperation("元素查询")
    @PostMapping("/list")
    public PageResult<ElementResponse> selectElementPageList(@RequestBody(required = false) PageRequest<ListRuleEngineVariableRequest> pageRequest) {
        return elementService.selectElementPageList(pageRequest);
    }

    /**
     * 新增
     *
     * @param elementAddRequest 元素数据
     * @return ElementResponse
     */
    @ApiOperation("新增元素")
    @PostMapping("/add")
    public PlainResult<ElementResponse> add(@RequestBody @Valid ElementAddRequest elementAddRequest) {
        PlainResult<ElementResponse> result = new PlainResult<>();
        ElementResponse addResult = elementService.add(elementAddRequest);
        result.setData(addResult);
        return result;
    }


    /**
     * @param id 条件id
     * @return ElementResponse
     */
    @ApiOperation("根据Id查询元素")
    @GetMapping("/get")
    public PlainResult<ElementResponse> getById(@RequestParam  Long id) {
        PlainResult<ElementResponse> result = new PlainResult<>();
        ElementResponse elementResponse = elementService.get(id);
        result.setData(elementResponse);
        return result;
    }

    @ApiOperation("根据Ids批量查询元素")
    @PostMapping("/getByIds")
    public PlainResult<List<ElementResponse>> getByIds(@RequestBody @Valid IdsRequest ids) {
        PlainResult<List<ElementResponse>> result = new PlainResult<>();
        List<ElementResponse> elementResponses = elementService.listByIds(ids.getIds());
        result.setData(elementResponses);
        return result;
    }


}
