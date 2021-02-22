package com.zjb.ruleplatform.entity.common;

/**
 * @author 赵静波
 * @date 2021-01-14 11:00:11
 */
public class PlainResult<T> extends BaseResult {

	private static final long serialVersionUID = 1L;

	/**
	 * 调用返回的数据
	 */
	private T data;

	public T getData() {
		return data;
	}

	public PlainResult() {
	}

	public PlainResult(T data) {
		this.data = data;
	}

	public void setData(T data) {
		this.data = data;
	}

	@Override
	public String toString() {
		return "PlainResult [data=" + data + "]";
	}
	
}
