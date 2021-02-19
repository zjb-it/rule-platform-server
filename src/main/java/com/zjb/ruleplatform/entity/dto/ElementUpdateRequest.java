package com.zjb.ruleplatform.entity.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

/**
 * @author 赵静波
 * @date 2021-01-14 11:00:11
 */
@Data
public class ElementUpdateRequest {


    @NotNull
    private Integer id;
    @NotBlank(message = "元素名称不能为空")
    private String name;
    private String description;


}
