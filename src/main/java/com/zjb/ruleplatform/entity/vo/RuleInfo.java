package com.zjb.ruleplatform.entity.vo;

import lombok.Data;

import javax.validation.constraints.NotBlank;

/**
 * @author 赵静波 <zhaojingbo>
 * Created on 2021-01-30
 */
@Data
public class RuleInfo {

    @NotBlank(message = "code不能为空")
    private String code;

    private Long id;

    @NotBlank(message = "名称不能为空")
    private String name;

    private String description;

    private String status;

}
