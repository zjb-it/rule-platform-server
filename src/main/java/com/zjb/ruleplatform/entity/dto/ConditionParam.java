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

import javax.validation.constraints.NotBlank;
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
public class ConditionParam {

    /**
     * name : 合同编号
     * description : 我是一个条件
     * config : {"left":{"type":2,"value_type":"STRING","value":1},"symbol":">","right":{"type":2,"value_type":"STRING","value":1}}
     */
    private Long id;
    @NotBlank(message = "条件名称不能为空")
    private String name;
    private String description;
    @NotNull(message = "条件配置不能为空")
    private ConfigBean config;

}