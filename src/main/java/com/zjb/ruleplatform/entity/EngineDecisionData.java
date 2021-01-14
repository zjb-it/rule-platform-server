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
 * 规则表engine_decision_data
 * </p>
 *
 * @author zhaojingbo
 * @since 2021-01-14
 */
@Data
  @EqualsAndHashCode(callSuper = false)
    @TableName("rule_engine_decision_data")
public class EngineDecisionData implements Serializable {

    private static final long serialVersionUID = 1L;

      /**
     * 主键，
     */
        @TableId(value = "id", type = IdType.AUTO)
      private Long id;

      /**
     * ，
     */
      private Long decisionId;

      /**
     * 行号，
     */
      private Long orderNo;

      /**
     * 输入的id，
     */
      private Long inputId;

      /**
     * input_type=0 value就是用户输入的值 ，type=1,value就是变量id ，type=2,就是元素id 如果value_type=4, value=null, 则value=0，
     */
      private String value;

      /**
     * 输入的类型，NUMBER,STRING，
     */
      private String valueType;

      /**
     * 输入类型  0固定值 ，1变量 ，2元素 ，
     */
      private Boolean type;

      /**
     *  ，
     */
      private Boolean inputType;

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
