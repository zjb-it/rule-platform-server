package com.zjb.ruleplatform.entity.dto;


import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;

/**
 * 元素新增实体
 *
 * @author v-lixing.ea
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class ElementAddRequest  {

    @NotBlank(message = "元素类型不能为空")
    private String valueType;

    @Pattern(regexp = "^[0-9a-zA-Z_]+$", message = "只能由英文数字下划线组成。")
    @NotBlank(message = "元素code不能为空")
    private String code;

    @NotBlank(message = "元素名称不能为空")
    private String name;

    private String description;

}
