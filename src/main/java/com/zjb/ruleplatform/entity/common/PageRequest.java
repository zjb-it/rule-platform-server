package com.zjb.ruleplatform.entity.common;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

/**
 * @author 赵静波
 * @date 2021-01-14 11:00:11
 */
@Data
@ApiModel
public class PageRequest<T>{

    @ApiModelProperty("查询条件")
    private T query;
    @ApiModelProperty("分页参数")
    private PageBase page = new PageBase();

    @ApiModelProperty("排序参数")
    private List<OrderBy> orders=new ArrayList<>();

    @Data
    @ApiModel
    @NoArgsConstructor
    @AllArgsConstructor
    public class PageBase {
        /**
         * 每页条数
         */
        @ApiModelProperty("每页条数")
        protected int pageSize=10;
        /**
         * 当前页码
         */
        @ApiModelProperty("当前页码")
        protected int pageIndex=1;



    }
    @ApiModel
    public static class OrderBy {

        public OrderBy() {
        }

        public OrderBy(String colName, boolean desc) {
            this.columnName = colName;
            this.desc = desc;
        }

        @ApiModelProperty("排序列名")
        private String columnName;
        @ApiModelProperty("是否倒序")
        private boolean desc;
    }

}