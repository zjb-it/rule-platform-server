package com.zjb.ruleplatform.entity.dto;


import com.zjb.ruleplatform.util.DataTypeUtils;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @author v-lixing.ea
 * 元素返回体
 */
@Data
public class ElementResponse  {

    @ApiModelProperty(value = "id", name = "id")
    private Long id;
    @ApiModelProperty(value = "元素名称", name = "name")
    private String name;
    @ApiModelProperty(value = "元素编码", name = "code")
    private String code;
    @ApiModelProperty(value = "元素描述", name = "description")
    private String description;
    private String valueDataType;
    private String valueDataTypeDesc;

    public String getValueDataTypeDesc() {
        return DataTypeUtils.getName(valueDataType);
    }

}
