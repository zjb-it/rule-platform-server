package com.zjb.ruleplatform.entity.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@Data
@AllArgsConstructor
public class LeftBean {
    /**
     * type : 0
     * value : 1
     * value_name : 1
     */

    private String valueDataType;
    private String value;
    private String valueName;
    private String valueType;
}
