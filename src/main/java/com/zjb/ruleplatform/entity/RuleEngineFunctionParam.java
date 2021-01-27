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
 * 规则函数参数，影响下游:
 * </p>
 *
 * @author zhaojingbo
 * @since 2021-01-27
 */
@Data
@EqualsAndHashCode(callSuper = false)
public class RuleEngineFunctionParam implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 主键，
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * ，
     */
    private Long functionId;

    /**
     * ，
     */
    private String functionParamCode;

    /**
     * ，
     */
    private String functionParamName;

    /**
     * ，
     */
    private String valueDataType;

    /**
     * 创建时间，
     */
    private Date createTime;

    /**
     * ，
     */
    private Date updateTime;

    /**
     * ，
     */
    @TableLogic
    private Boolean deleted;


}
