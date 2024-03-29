package com.zjb.ruleplatform.entity.vo;

import lombok.Data;

import javax.validation.constraints.NotBlank;

/**
 * @author 赵静波
 * @date 2021-01-30 23:15:34
 */
@Data
public class RuleInfo {

    @NotBlank(message = "code不能为空")
    private String code;

    private Long id;

    @NotBlank(message = "名称不能为空")
    private String name;

    private String description;

    private Integer status;

}
