package com.zjb.ruleplatform.entity;

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
 * 规则表engine_set_json
 * </p>
 *
 * @author zhaojingbo
 * @since 2021-01-14
 */
@Data
  @EqualsAndHashCode(callSuper = false)
    public class RuleEngineRuleSetJson implements Serializable {

    private static final long serialVersionUID = 1L;

      /**
     * 主键，
     */
        @TableId(value = "id", type = IdType.AUTO)
      private Long id;

      /**
     * 规则id，
     */
      private Long ruleSetId;

      /**
     * ，
     */
      private String ruleSetCode;

      /**
     * ，
     */
      private String ruleSetName;

      /**
     * 规则的json串，
     */
      private String ruleSetJson;

      /**
     * 需要计算的信息，
     */
      private String countInfo;

      /**
     * 版本，
     */
      private String ruleVersion;

      /**
     * 0未发布，1已发布，2草稿，3历史，
     */
      private Integer published;

      /**
     * 创建时间，，
     */
      private Date createTime;

    private Date updateTime;

      /**
     * ，
     */
      @TableLogic
    private Integer deleted;

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
      private String environment;

      /**
     * 是否是当前环境，
     */
      @TableField("is_cur_env")
    private Boolean curEnv;

      /**
     * 租户id
     */
      private String tenantId;


}
