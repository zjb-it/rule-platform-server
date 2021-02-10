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

import com.google.common.collect.Sets;
import com.zjb.ruleplatform.entity.vo.CollectorValue;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.util.Collection;
import java.util.HashSet;

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
public class ConditionParam implements CollectorValue {

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

    @Override
    public Collection<Long> collectorElement() {
        final Collection<Long> left = config.getLeftVariable().collectorElement();
        final Collection<Long> right = config.getRightVariable().collectorElement();
        final HashSet<Long> longs = Sets.newHashSet(left);
        longs.addAll(right);
        return longs;
    }

    @Override
    public Collection<Long> collectorVariable() {
        final Collection<Long> left = config.getLeftVariable().collectorVariable();
        final Collection<Long> right = config.getRightVariable().collectorVariable();
        final HashSet<Long> longs = Sets.newHashSet(left);
        longs.addAll(right);
        return longs;
    }
}