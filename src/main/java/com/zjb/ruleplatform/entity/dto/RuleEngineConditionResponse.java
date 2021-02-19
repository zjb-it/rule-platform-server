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

/**
 * @author 赵静波
 * @date 2021-01-14 15:11:14
 */
@Data
public class RuleEngineConditionResponse {
    /**
     * id
     */
    private Long id;
    /**
     * 条件名称
     */
    private String name;
    /**
     * 条件配置
     */
    private String config;
}