package com.zjb.ruleplatform.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.IdType;
import java.util.Date;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableField;
import java.io.Serializable;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * <p>
 * 规则表engine_decision_input
 * </p>
 *
 * @author zhaojingbo
 * @since 2021-01-14
 */
@Data
  @EqualsAndHashCode(callSuper = false)
    @TableName("rule_engine_decision_input")
public class EngineDecisionInput implements Serializable {

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
     * 输入类型  0固定值 ，1变量 ，2元素 ，3 result  4 优先级 5备注，
     */
      private Boolean type;

      /**
     * type=0 value就是用户输入的值 ，type=1,value就是变量id ，type=2,就是元素id，
     */
      private String value;

      /**
     * value 对应的code，
     */
      private String valueCode;

      /**
     * 输入的数据类型，STRING,NUMBER,COLLECTION,BOOLEAN，
     */
      private String valueType;

      /**
     * 0是优先级，1,是条件，2是结果，3是备注，
     */
      private Boolean inputType;

      /**
     * 符号 = > ...，
     */
      private String symbol;

      /**
     * input的顺序，
     */
      private Long orderNo;

      /**
     * input名称，
     */
      private String alias;

      /**
     * 循环是否开启，默认关闭 0-关闭，1-开启，
     */
      @TableField("is_loop")
    private Boolean loop;

      /**
     * loop_Value_id对应的id，
     */
      private String loopValue;

      /**
     * 循环变量类型  0固定值 ，1变量 ，2元素，
     */
      private Integer loopType;

      /**
     * loop_Value_id对应的name，
     */
      private String loopValueName;

      /**
     * 默认值，
     */
      private String defaultValue;

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

      /**
     * 0固定值，1变量，
     */
      private Integer defaultType;

      /**
     * 决策表结果code，
     */
      private String code;

      /**
     * 最高优先级循环，
     */
      private Integer extraConfig;

      /**
     * 全流程去重策略 0-不去重,为默认值;1-去重，
     */
      private Integer processRepetitionPolicy;

      /**
     * 流程去重节点名称,逗号分隔，
     */
      private String processRepetitionNodeName;


}
