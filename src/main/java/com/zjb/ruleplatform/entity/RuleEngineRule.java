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
 * 条件规则表
 * </p>
 *
 * @author zhaojingbo
 * @since 2021-01-14
 */
@Data
@EqualsAndHashCode(callSuper = false)
public class RuleEngineRule implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 主键，
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 名称，
     */
    private String name;

    /**
     * 编码，
     */
    private String code;

    /**
     * 描述，
     */
    private String description;
//0 编辑中，1待发布，2已发布
    private Integer status;
    /**
     * 创建用户名称，
     */
    private String createUserName;

    /**
     * 创建用户id，
     */
    private String createUserId;
    private String codeName;


    private String actionValueType;

    private String actionValue;

    private String actionValueDataType;

    /**
     * 创建时间，
     */
    private Date createTime;

    private Date updateTime;

    //@TableLogic
    private Boolean deleted;


}
