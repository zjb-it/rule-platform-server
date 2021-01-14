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
 * 规则表engine_variable_param
 * </p>
 *
 * @author zhaojingbo
 * @since 2021-01-14
 */
@Data
  @EqualsAndHashCode(callSuper = false)
    @TableName("rule_engine_variable_param")
public class EngineVariableParam implements Serializable {

    private static final long serialVersionUID = 1L;

      /**
     * 主键，
     */
        @TableId(value = "id", type = IdType.AUTO)
      private Long id;

      /**
     * ，
     */
      private Long variableId;

      /**
     * ，
     */
      private Long functionId;

      /**
     * 0固定值 ，1变量 ，2元素 ，3 result ,，
     */
      private Boolean functionParamType;

      /**
     * function参数code，
     */
      private String functionParamName;

      /**
     * function参数code，
     */
      private String functionParamCode;

      /**
     * function参数值 ，如果是固定值就应该有，
     */
      private String functionParamValue;

      /**
     * 参数的数据类型，
     */
      private String functionParamDataType;

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
