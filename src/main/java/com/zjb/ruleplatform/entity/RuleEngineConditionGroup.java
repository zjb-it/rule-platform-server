package com.zjb.ruleplatform.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import java.util.Date;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import java.io.Serializable;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * @author 赵静波
 * @date 2021-01-26 10:36:34
 */
@Data
  @EqualsAndHashCode(callSuper = false)
    public class RuleEngineConditionGroup implements Serializable {

    private static final long serialVersionUID = 1L;

      /**
     * 主键，
     */
        @TableId(value = "id", type = IdType.AUTO)
      private Long id;

    private Long conditionId;
    private Long ruleId;

    private Integer conditionOrder;

    private Integer conditionGroupOrder;

      /**
     * 创建时间，
     */
      private Date createTime;

    private Date updateTime;

    //@TableLogic
    private Boolean deleted;


}
