package com.zjb.ruleplatform.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.IdType;
import java.util.Date;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import java.io.Serializable;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * <p>
 * 规则表engine_element
 * </p>
 *
 * @author zhaojingbo
 * @since 2021-01-14
 */
@Data
  @EqualsAndHashCode(callSuper = false)
    @TableName("rule_engine_element")
public class EngineElement implements Serializable {

    private static final long serialVersionUID = 1L;

      /**
     * 主键，
     */
        @TableId(value = "id", type = IdType.AUTO)
      private Long id;

    private String name;

    private String code;

    private String valueType;

      /**
     * 创建时间，
     */
      private Date createTime;

    private Date updateTime;

    @TableLogic
    private Boolean deleted;

    private String description;

      /**
     * ，
     */
      private String codeName;


}
