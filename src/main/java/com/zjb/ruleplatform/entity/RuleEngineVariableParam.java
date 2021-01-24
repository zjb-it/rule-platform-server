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
 * 规则表engine_variable_param
 * </p>
 *
 * @author zhaojingbo
 * @since 2021-01-14
 */
@Data
  @EqualsAndHashCode(callSuper = false)
    public class RuleEngineVariableParam implements Serializable {

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
      private String functionName;

      /**
     * constant,variable,element
     */
      private String functionParamValueType;

      /**
     * function参数code，
     */
      private String functionParamName;

      /**
     * function参数code，
     */
      private String functionParamDescription;
      private String functionParamValueDescription;

      /**
     * function参数值 ，如果是固定值就应该有，
     */
      private String functionParamValue;

      /**
     * 参数的数据类型，
     */
      private String functionParamValueDataType;

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
      //@TableLogic
    private Boolean deleted;


}
