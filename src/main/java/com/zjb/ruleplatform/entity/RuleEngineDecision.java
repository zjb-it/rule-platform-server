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
 * 规则表engine_decision
 * </p>
 *
 * @author zhaojingbo
 * @since 2021-01-14
 */
@Data
  @EqualsAndHashCode(callSuper = false)
    public class RuleEngineDecision implements Serializable {

    private static final long serialVersionUID = 1L;

      /**
     * 主键
     */
        @TableId(value = "id", type = IdType.AUTO)
      private Long id;

      /**
     * 决策表名称，
     */
      private String name;

      /**
     * 决策表code，
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
     * 流程去重节点名称，逗号分隔，
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
