package com.zjb.ruleplatform.entity.common;

import java.io.Serializable;

@SuppressWarnings("unchecked")
public class BaseResult implements Serializable {

	private static final long serialVersionUID = 1L;

	/**
	 * 标识本次调用是否返回
	 */
	private boolean success;

	/**
	 * 本次调用返回code，一般为错误代码
	 */
	private String code;

	/**
	 * 本次调用返回的消息，一般为错误消息
	 */
	private String message;

	/**
	 * 请求Id
	 */
	private String requestId;

	public BaseResult() {
		this.code = CommonResultCode.SUCCESS.code;
		this.success = true;
		this.message = CommonResultCode.SUCCESS.message;
	}

	/**
	 * 成功但没数据
	 *
	 */
	public <R extends BaseResult> R setSuccessException() {
		this.code = CommonResultCode.SUCCESS_EXCEPTION.code;
		this.success = true;
		this.message = CommonResultCode.SUCCESS_EXCEPTION.message;
		return (R) this;
	}

	/**
	 * 设置成功信息
	 *
	 * @param rc
	 * @param args
	 * @return
	 * @see CommonResultCode
	 */
	public <R extends BaseResult> R setSuccess(CommonResultCode rc, Object... args) {
		this.code = rc.code;
		this.success = true;
		if (args == null || args.length == 0) {
			this.message = rc.message;
		} else {
			this.message = String.format(rc.message, args);
		}
		return (R) this;
	}

	/**
	 * 设置错误信息
	 * 
	 * @param code
	 * @param message
	 */
	public <R extends BaseResult> R setErrorMessage(String code, String message) {
		this.code = code;
		this.success = false;
		this.message = message;
		return (R) this;
	}

	/**
	 * 设置错误信息
	 * 
	 * @param rc
	 * @param args
	 * @return
	 * @see CommonResultCode
	 */
	public <R extends BaseResult> R setError(CommonResultCode rc, Object... args) {
		this.code = rc.code;
		this.success = false;
		if (args == null || args.length == 0) {
			this.message = rc.message;
		} else {
			this.message = String.format(rc.message, args);
		}
		return (R) this;
	}

	public boolean isSuccess() {
		return success;
	}

	public void setSuccess(boolean success) {
		this.success = success;
	}

	public String getCode() {
		return code;
	}

	public void setCode(String code) {
		this.code = code;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	public String getRequestId() {
		return requestId;
	}

	public void setRequestId(String requestId) {
		this.requestId = requestId;
	}

	@Override
	public String toString() {
		return "BaseResult [success=" + success + ", code=" + code + ", message=" + message + ", requestId=" + requestId + "]";
	}

}
