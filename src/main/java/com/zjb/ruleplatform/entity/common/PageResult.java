package com.zjb.ruleplatform.entity.common;

/**
 * @author 赵静波
 * @date 2021-01-18 10:00:16
 */
public class PageResult<T> extends ListResult<T>{

    private static final long serialVersionUID = 1L;

    /**
     *分页数
     */

    private Integer pageSize;
    /**
     *页码
     */

    private Integer page;

    /**
     * 记录数
     *
     */
    private Long total;

    public Long getTotal() {
        return total;
    }

    public void setTotal(Long total) {
        this.total = total;
    }

    public Integer getPageSize() {
        return pageSize;
    }

    public void setPageSize(Integer pageSize) {
        this.pageSize = pageSize;
    }

    public Integer getPage() {
        return page;
    }

    public void setPage(Integer page) {
        this.page = page;
    }

	@Override
	public String toString() {
		return "PageResult [pageSize=" + pageSize + ", page=" + page + ", total=" + total + "]";
	}

}
