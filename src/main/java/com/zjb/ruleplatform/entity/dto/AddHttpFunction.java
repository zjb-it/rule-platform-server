package com.zjb.ruleplatform.entity.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * @author 赵静波 <wb_zhaojingbo@kuaishou.com>
 * Created on 2021-01-27
 */
@Data
public class AddHttpFunction {
    private Long id;
    private String code;
    private String name;
    private String description;
    private List<Param> params;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Param {
        private String code;
        private String name;
        private String valueDataType;
    }
}
