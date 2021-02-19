package com.zjb.ruleplatform.service;


import com.zjb.ruleplatform.entity.common.PageRequest;
import com.zjb.ruleplatform.entity.common.PageResult;
import com.zjb.ruleplatform.entity.common.PlainResult;
import com.zjb.ruleplatform.entity.dto.*;

import java.util.List;

/**
 * @author 赵静波
 * @date 2021-01-26 10:36:34
 */
public interface ElementService {

    /**
     * 分页查询元素
     *
     * @param pageRequest
     * @return
     */
    PageResult<ElementResponse> selectElementPageList(PageRequest<ListRuleEngineVariableRequest> pageRequest);

    /**
     * 根据ID查询
     *
     * @param id
     * @return
     */
    ElementResponse get(Long id);

    /**
     * 新增元素
     *
     * @param elementAddRequest
     * @return
     */
    ElementResponse add(ElementAddRequest elementAddRequest);


    List<ElementResponse> listByIds(List<Integer> ids);


}
