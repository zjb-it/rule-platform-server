package com.zjb.ruleplatform.entity.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author yuzhiji
 */
@NoArgsConstructor
@Data
public  class ConfigBean {
    /**
     * left : {"type":0,"value":1,"value_name":"1"}
     * symbol : >
     * right : {"type":2,"value":1,"value_name":"元素1"}
     */

    private LeftBean leftVariable;
    private String symbol;
    private LeftBean rightVariable;

    @NoArgsConstructor
    @Data
    @AllArgsConstructor
    public static class LeftBean {
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

}