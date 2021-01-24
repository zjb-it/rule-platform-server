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

import com.zjb.ruleplatform.entity.vo.FunctionVo;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotNull;

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
public class RuleEngineVariableRequest  {

    /**
     * value_type : STRING
     * name : 合同编号
     * description : 我是一个元素
     * value : 501
     * function : {"id":1,"variables":[{"code":"contract_number","type":2,"value_type":"NUMBER","value":1},{"code":"contract_body","type":1,"value_type":"NUMBER","value":1},{"code":"contract_amount","type":0,"value_type":"NUMBER","value":1}]}
     */
    private Long id;
    private String name;
    private String description;
    private String valueDataType;
    @NotNull
    private FunctionVo function;

}