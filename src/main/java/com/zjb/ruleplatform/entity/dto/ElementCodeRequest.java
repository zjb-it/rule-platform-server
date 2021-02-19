package com.zjb.ruleplatform.entity.dto;


import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @author 赵静波
 * @date 2021-01-14 11:00:11
 */
@Data
public class ElementCodeRequest {

    @ApiModelProperty(value = "元素编码", name = "code")
    //@NotBlank(message = "元素code不能为空")
    //@Pattern(regexp = "^[0-9a-zA-Z_]+$", message = "只能由英文数字下划线组成。"
    private String code;

}
