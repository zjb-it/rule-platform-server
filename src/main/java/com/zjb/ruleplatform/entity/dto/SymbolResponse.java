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
package com.zjb.ruleplatform.entity.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * @author 赵静波
 * @date 2021-01-14 14:56:26
 */
@NoArgsConstructor
@Data
public class SymbolResponse {

    /**
     * 符号
     */
    private String symbol;
    /**
     * 数据类型
     */
    private List<String> valueDataTypes;
}