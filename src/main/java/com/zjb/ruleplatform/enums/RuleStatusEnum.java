package com.zjb.ruleplatform.enums;

public enum RuleStatusEnum {
    //0 编辑中，1待发布，2已发布
    editing(0),unpublish(1),published(2);
    /**
     * @author 赵静波
     * @date 2021-02-15 22:55:40
     */
    public int status;

    RuleStatusEnum(int status) {
        this.status = status;
    }
}
