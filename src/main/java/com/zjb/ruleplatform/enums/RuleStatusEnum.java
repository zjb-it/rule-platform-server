package com.zjb.ruleplatform.enums;

public enum RuleStatusEnum {
    //0 编辑中，1待发布，2已发布
    editing(0),unpublish(1),published(2);
    public int status;

    RuleStatusEnum(int status) {
        this.status = status;
    }
}
