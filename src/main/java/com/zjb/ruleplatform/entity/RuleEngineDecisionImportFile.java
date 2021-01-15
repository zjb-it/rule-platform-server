package com.zjb.ruleplatform.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import java.util.Date;
import com.baomidou.mybatisplus.annotation.TableId;
import java.io.Serializable;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * <p>
 * 
 * </p>
 *
 * @author zhaojingbo
 * @since 2021-01-14
 */
@Data
  @EqualsAndHashCode(callSuper = false)
    public class RuleEngineDecisionImportFile implements Serializable {

    private static final long serialVersionUID = 1L;

      @TableId(value = "id", type = IdType.AUTO)
      private Long id;

      /**
     * 文件名称
     */
      private String fileName;

      /**
     * 决策表id
     */
      private Long decisionId;

    private String decisionCode;

      /**
     * 用户上传的文件路径
     */
      private String fileUrl;

      /**
     * 如果解析失败，后台上传到文件服务器的url
     */
      private String errorFileUrl;

      /**
     * 0导入中，1数据处理中，2失败，3成功
     */
      private Integer status;

    private Date createTime;

    private Date updateTime;

      /**
     * 创建人工号
     */
      private String uploadUserId;

      /**
     * 创建人姓名
     */
      private String uploadUserName;

      /**
     * 头像
     */
      private String uploadUserAvator;


}
