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
 * 规则表engine_variable
 * </p>
 *
 * @author zhaojingbo
 * @since 2021-01-14
 */
@Data
  @EqualsAndHashCode(callSuper = false)
    @TableName("rule_engine_variable")
public class EngineVariable implements Serializable {

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
      private String value;

      /**
     * 如果是固定值 ，就是value的type,如果是funciotn，就是function的返回值类型，
     */
      private String valueType;

      /**
     * 0固定值 ，1变量，2 元素，3是reulst，
     */
      private Boolean type;

      /**
     * 创建时间，
     */
      private Date createTime;

    private Date updateTime;

      /**
     * ，
     */
      @TableLogic
    private Boolean deleted;

      /**
     * ，
     */
      private String description;


}
