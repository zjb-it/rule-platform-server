package com.zjb.ruleplatform.entity.vo;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * @author 赵静波
 * @date 2021-01-18 15:17:34
 */
@NoArgsConstructor
@Data
public class FunctionDetailVo {

    private String name;
    private String description;
    private List<VariablesBean> variables;

    @NoArgsConstructor
    @Data
    public static class VariablesBean {
        private String name;
        private String description;
        private String valueDataType;

        public VariablesBean(String name, String description, String valueDataType) {
            this.name = name;
            this.description = description;
            this.valueDataType = valueDataType;
        }
    }


}