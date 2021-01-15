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
 * 规则表engine_set_test_case
 * </p>
 *
 * @author zhaojingbo
 * @since 2021-01-14
 */
@Data
  @EqualsAndHashCode(callSuper = false)
    public class RuleEngineRuleSetTestCase implements Serializable {

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
     * 规则的参数json串，
     */
      private String paramJson;

      /**
     * 版本，
     */
      private String ruleVersion;

      /**
     * 期望结果，
     */
      private String expectedResult;

      /**
     * 最后一次执行结果，
     */
      private String lastExecuteResult;

      /**
     * 运行环境，
     */
      private String env;

      /**
     * 测试用例执行状态0:失败1:成功，
     */
      private Integer operStatus;

      /**
     * 创建时间，
     */
      private Date createTime;

    private Date updateTime;

      /**
     * ，
     */
      @TableLogic
    private Integer deleted;


}
