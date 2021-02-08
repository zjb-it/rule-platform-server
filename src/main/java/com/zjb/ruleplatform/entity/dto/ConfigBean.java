package com.zjb.ruleplatform.entity.dto;

import com.zjb.ruleplatform.entity.vo.LeftBean;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author yuzhiji
 */
@NoArgsConstructor
@Data
public class ConfigBean {
    /**
     * left : {"type":0,"value":1,"value_name":"1"}
     * symbol : >
     * right : {"type":2,"value":1,"value_name":"元素1"}
     */

    private LeftBean leftVariable;
    private String symbol;
    private LeftBean rightVariable;


}