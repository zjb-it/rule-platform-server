package com.zjb.ruleplatform.entity.dto;


import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * @author v-lixing.ea
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class ElementRequest  {

    private String[] valueTypes;

    private String code;

    private String name;
    //区分规则内创建还是外部创建
    private Boolean isShowed;
}
