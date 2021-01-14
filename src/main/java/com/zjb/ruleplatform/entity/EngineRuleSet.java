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
 * 规则表engine_set
 * </p>
 *
 * @author zhaojingbo
 * @since 2021-01-14
 */
@Data
  @EqualsAndHashCode(callSuper = false)
    @TableName("rule_engine_rule_set")
public class EngineRuleSet implements Serializable {

    private static final long serialVersionUID = 1L;

      /**
     * 主键，
     */
        @TableId(value = "id", type = IdType.AUTO)
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

      /**
     * ，
     */
      private Long bizId;

      /**
     * ，
     */
      private String bizCode;

      /**
     * ，
     */
      private String bizName;

      /**
     * ，
     */
      private Long defaultRuleId;

      /**
     * ，
     */
      private String createUserName;

      /**
     * ，
     */
      private String createUserId;

      /**
     * ，
     */
      private String updateUserName;

      /**
     * ，
     */
      private String updateUserId;

      /**
     *  命中策略,
     * 0-执行第一个命中的规则后结束, 默认值
     * 1-逐个执行全部命中规则
，
     */
      private Integer hitPolicy;

      /**
     *  默认规则策略,0-无论是否有规则命中都在最后执行,
   1-当无规则命中时执行,有规则命中则不执行
，
     */
      private Integer defaultRulePolicy;

      /**
     *  *节点去重策略,
     * 0-不进行节点去重, 默认值
     * 1-前去重
     * 2-后去重
，
     */
      private Integer nodeRepetitionPolicy;

      /**
     *      * 全流程去重策略
     * 0-不去重， 默认值
     * 1-去重
，
     */
      private Integer processRepetitionPolicy;

      /**
     * 流程去重节点名称,逗号分隔，
     */
      private String processRepetitionNodeName;

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
     * 0表示启用，1表示禁用，
     */
      private Boolean status;

      /**
     * ，
     */
      private Integer processRepetitionPolicyVariableId;

      /**
     * 0 待发布 1 已发布 2草稿，
     */
      private Boolean createStatus;

      /**
     * 规则大版本，
     */
      private String preparedVersion;

      /**
     * ，
     */
      private String publishVersion;

      /**
     * ，
     */
      private String codeName;

      /**
     * 是否开启随机分发策略，
     */
      private Boolean randomSwitch;

      /**
     * 随机分发数量，
     */
      private Integer randomCount;

      /**
     * 前置条件集策略 0关闭 1 开启，
     */
      private Boolean preConditionPolicy;

      /**
     * //0表示全员可见，1表示关，
     */
      private Boolean authShowAll;

      /**
     * 编辑中人员工号，
     */
      private String editUserId;

      /**
     * 编辑中人员姓名，
     */
      private String editUserName;

      /**
     * 租户id
     */
      private String tenantId;


}
