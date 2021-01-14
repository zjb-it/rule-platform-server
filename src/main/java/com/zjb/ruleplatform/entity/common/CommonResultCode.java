package com.zjb.ruleplatform.entity.common;

import java.io.Serializable;

public enum CommonResultCode implements Serializable {

	SUCCESS("0000", "successful"),
	SUCCESS_EXCEPTION("201", "success but no data found."),
	EXCEPTION("-100", " %s"),
	ILLEGAL_PARAM("-101","参数错误，参数是 %s"),

	//Authentication Exception
	AUTHENTICATION_EXCEPTION("401","authentication exception"),

	//Resource Exception
	RESOURCE_ALREADY_EXIST("409"," %s"),
	RESOURCE_NOT_EXIST("404"," %s"),

	SHARE_ORDER_EXCEPTION("1002","这个订单已经流转结束"),

	ORDER_NOT_EXIST("1003","订单不存在"),
	ORDER_NOT_MYSELF("1004","订单不是自己的");




	public final String code;
	public final String message;

	CommonResultCode(String code, String msg) {
		this.code = code;
		this.message = msg;
	}
}