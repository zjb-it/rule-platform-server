package com.zjb.ruleplatform.entity.dto;

import com.zjb.ruleengine.core.value.Value;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import java.util.List;

/**
 * @author 赵静波 <wb_zhaojingbo@kuaishou.com>
 * Created on 2021-01-30
 */
@Data
public class AddRuleRequest {
    @NotBlank(message = "code不能为空")
    private String code;

    @NotBlank(message = "名称不能为空")
    private String name;

    private String description;

    private ConfigBean.LeftBean action;

    @Data
    public static class ConditionGroup {
        private List<Long> conditionIds;
        private Integer order;
    }


}
