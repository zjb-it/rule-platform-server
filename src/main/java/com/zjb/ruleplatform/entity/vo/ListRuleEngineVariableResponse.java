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
package com.zjb.ruleplatform.entity.vo;

import com.zjb.ruleplatform.util.DataTypeUtils;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author 赵静波
 * @date 2021-01-26 10:36:34
 */
@NoArgsConstructor
@Data
public class ListRuleEngineVariableResponse {

    /**
     * id : 1
     * name : 合同编号
     * value_type : STRING
     * value : 10010
     */

    private Long id;
    private String name;
    private String valueDataType;
    private String valueDataTypeDesc;
    private String functionName;
    private String description;

    public String getValueDataTypeDesc() {
        return DataTypeUtils.getName(valueDataType);
    }

}