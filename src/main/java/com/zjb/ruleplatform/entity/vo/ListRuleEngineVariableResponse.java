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
 * 〈一句话功能简述〉<br>
 * 〈〉
 *
 * @author v-dingqianwen.ea
 * @create 2019/8/6
 * @since 1.0.0
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
    private String functionName;
    private String description;

    public String getValueDataType() {
        return DataTypeUtils.getName(valueDataType);
    }

}