package com.zjb.ruleplatform.entity.common;

import java.io.Serializable;

public enum CommonResultCode implements Serializable {

    SUCCESS("0000", "successful"),
    SUCCESS_EXCEPTION("201", "success but no data found."),
    EXCEPTION("-100", " %s"),
    ILLEGAL_PARAM("-101", "参数错误，参数是 %s"),

    //Authentication Exception
    AUTHENTICATION_EXCEPTION("401", "authentication exception"),

    //Resource Exception
    RESOURCE_ALREADY_EXIST("409", " %s"),
    RESOURCE_NOT_EXIST("404", " %s"),

    ;


    /**
     * @author 赵静波
     * @date 2021-01-14 11:00:11
     */
    public final String code;
    public final String message;

    CommonResultCode(String code, String msg) {
        this.code = code;
        this.message = msg;
    }
}