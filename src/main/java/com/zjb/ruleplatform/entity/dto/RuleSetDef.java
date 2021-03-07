package com.zjb.ruleplatform.entity.dto;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;

/**
 * @author 赵静波
 * Created on 2021-03-07
 */
@Data
public class RuleSetDef {
    /**
     * 主键，
     */
    private Long id;

    /**
     * ，
     */
    private String name;

    /**
     * ，
     */
    private String code;

    /**
     * ，
     */
    private String description;
}
