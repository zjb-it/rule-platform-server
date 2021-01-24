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

import com.fasterxml.jackson.annotation.JsonInclude;
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
@JsonInclude(JsonInclude.Include.NON_NULL)
public class GetRuleEngineVariableResponse {

    /**
     * id : 1
     * value_type : FUNCTION
     * result_value_type : STRING
     * name : 合同编号
     * description : 我是一个元素
     * value : 获取部门及其子部门
     * function : {"id":7,"variables":[{"code":"contract_number","name":"合同编号","type":2,"value":1,"value_name":"元素1"},{"code":"contract_body","name":"合同主体","type":1,"value":1,"value_name":"变量1"},{"code":"contract_amount","name":"合同编号","type":0,"value":1,"value_name":"1"}]}
     */

    private int id;
    private String valueDataType;
    private String name;
    private String description;
    private FunctionVo function;

}