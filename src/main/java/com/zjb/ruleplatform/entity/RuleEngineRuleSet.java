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
 * 规则表engine_set
 * </p>
 *
 * @author zhaojingbo
 * @since 2021-03-07
 */
@Data
  @EqualsAndHashCode(callSuper = false)
    public class RuleEngineRuleSet implements Serializable {

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
     * 0，编辑中 1待发布 2 已发布 
     */
      private Boolean status;

      /**
     * ，
     */
      private String codeName;


}
