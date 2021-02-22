package com.zjb.ruleplatform.entity.dto;

import com.zjb.ruleengine.core.value.Value;
import com.zjb.ruleplatform.entity.vo.LeftBean;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import java.util.List;

/**
 * @author 赵静波
 * @date 2021-01-30 23:08:23
 */
@Data
public class AddRuleRequest {
    @NotBlank(message = "code不能为空")
    private String code;

    private Long id;

    @NotBlank(message = "名称不能为空")
    private String name;

    private String description;

    private LeftBean action;
    private List<ConditionGroup> conditionGroups;

    @Data
    public static class ConditionGroup {
        private List<Long> conditionIds;
        private Integer order;
    }


}
