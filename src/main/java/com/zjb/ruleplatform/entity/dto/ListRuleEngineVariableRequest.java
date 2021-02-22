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
 * @date 2021-01-15 11:24:42
 */
@NoArgsConstructor
@Data
public class ListRuleEngineVariableRequest {

    /**
     * value_type : ["STRING, NUMBER"]
     * name : 合同编号
     */

    private String name;
    private List<String> valueDataType;
}