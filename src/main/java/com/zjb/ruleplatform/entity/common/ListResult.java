package com.zjb.ruleplatform.entity.common;

import java.util.List;

/**
 * @author 赵静波
 * @date 2021-01-18 10:00:16
 */
public class ListResult<T> extends BaseResult {

	private static final long serialVersionUID = 1L;

	private List<T> data;

	private int count;

	public List<T> getData() {
		return data;
	}

	public void setData(List<T> data) {
		this.data = data;
	}

	public int getCount() {
		return count;
	}

	public void setCount(int count) {
		this.count = count;
	}

	public ListResult(List<T> data) {
		this.data = data;
	}

	public ListResult() {
	}

	@Override
	public String toString() {
		return "ListResult [data=" + data + ", count=" + count + "]";
	}

}
