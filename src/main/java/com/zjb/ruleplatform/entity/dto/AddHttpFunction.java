package com.zjb.ruleplatform.entity.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * @author 赵静波
 * @date 2021-01-28 11:07:44
 */
@Data
public class AddHttpFunction {
    private Long id;
    private String code;
    private String name;
    private String url;
    private String description;
    private String valueDataType;
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
