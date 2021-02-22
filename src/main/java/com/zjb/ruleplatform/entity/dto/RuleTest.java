package com.zjb.ruleplatform.entity.dto;

import lombok.Data;

import javax.validation.constraints.NotNull;
import java.util.List;
import java.util.Map;

/**
 * @author 赵静波
 * @date 2021-02-09 22:41:14
 */
@Data
public class RuleTest {

    private Map<String, Object> ruleParam;
    private Object ruleResult;
    @NotNull
    private Long ruleId;
}
