package com.zjb.ruleplatform.entity.vo;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * @author yuzhiji
 */
@NoArgsConstructor
@Data
public class FunctionVo {
    /**
     * id : 7
     * variables : [{"code":"contract_number","name":"合同编号","type":2,"value":1,"value_name":"元素1"},{"code":"contract_body","name":"合同主体","type":1,"value":1,"value_name":"变量1"},{"code":"contract_amount","name":"合同编号","type":0,"value":1,"value_name":"1"}]
     */

    private String name;
    private List<VariablesBean> variables;

    @NoArgsConstructor
    @Data
    public static class VariablesBean {
        /**
         * code : contract_number
         * name : 合同编号
         * type : 2
         * value : 1
         * value_name : 元素1
         */

        private String code;
        private String name;
        private String valueType;
        private String value;
        private String valueName;
        private String valueDataType;

        public VariablesBean(String code, String valueDataType) {
            this.code = code;
            this.valueDataType = valueDataType;
        }
    }
}