package com.zjb.ruleplatform.entity.common;

public class PlainResult<T> extends BaseResult {

	private static final long serialVersionUID = 1L;

	/**
	 * 调用返回的数据
	 */
	private T data;

	public T getData() {
		return data;
	}

	public void setData(T data) {
		this.data = data;
	}

	@Override
	public String toString() {
		return "PlainResult [data=" + data + "]";
	}
	
}
