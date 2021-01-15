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

import com.zjb.ruleplatform.entity.common.PlainResult;
import com.zjb.ruleplatform.entity.dto.SymbolRequest;
import com.zjb.ruleplatform.entity.dto.SymbolResponse;
import com.zjb.ruleplatform.service.ISymbolService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import java.util.List;

/**
 * 〈一句话功能简述〉<br>
 * 〈符号〉
 *
 * @author v-dingqianwen.ea
 * @create 2019/8/6
 * @since 1.0.0
 */
@RestController
@Api(tags = "规则引擎符号")
@RequestMapping("/ruleEngine/symbol")
@Slf4j
public class SymbolController {
    @Autowired
    private ISymbolService symbolService;

    /**
     * 根据业务类型选择Symbol
     *
     * @return
     */
    @ApiOperation("根据业务类型选择Symbol")
    @PostMapping("/get")
    public PlainResult<List<SymbolResponse>> get(@ApiParam @RequestBody @Valid SymbolRequest symbolRequest) {
        val plainResult = new PlainResult<List<SymbolResponse>>();
        plainResult.setData(symbolService.get(symbolRequest.getValueType()));
        return plainResult;
    }
}