package com.zjb.ruleplatform.entity;

import com.baomidou.mybatisplus.annotation.IdType;

import java.util.Date;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;

import java.io.Serializable;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * <p>
 * 规则表engine_condition
 * </p>
 *
 * @author zhaojingbo
 * @since 2021-01-14
 */
@Data
@EqualsAndHashCode(callSuper = false)
public class RuleEngineCondition implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 主键，
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    private String code;

    private String name;

    private String description;


    private String leftValueType;

    private String leftValue;

    /**
     * NUMBER ,STRING ,BOOLEAN ,COLLECTION，
     */
    private String leftDataType;

    private String rightValueType;

    private String rightValue;

    /**
     * NUMBER ,STRING ,BOOLEAN ,COLLECTION，
     */
    private String rightDataType;

    private String symbol;

    private String symbolName;

    private String symbolType;

    /**
     * 创建时间，
     */
    private Date createTime;

    private Date updateTime;

    @TableLogic
    private Boolean deleted;


}