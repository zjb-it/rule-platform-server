package com.zjb.ruleplatform.entity.vo;

import java.util.Collection;

/**
 * @author 赵静波
 * Created on 2021-02-09
 */
public interface CollectorValue {
    /**
     * 元素
     * @return 元素ID
     */
    Collection<Long> collectorElement();

    /**
     * 变量
     * @return 变量id
     */
    Collection<Long> collectorVariable();

}
