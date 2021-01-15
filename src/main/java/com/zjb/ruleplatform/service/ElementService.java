package com.zjb.ruleplatform.service;


import com.zjb.ruleplatform.entity.common.PageRequest;
import com.zjb.ruleplatform.entity.common.PageResult;
import com.zjb.ruleplatform.entity.common.PlainResult;
import com.zjb.ruleplatform.entity.dto.ElementAddRequest;
import com.zjb.ruleplatform.entity.dto.ElementRequest;
import com.zjb.ruleplatform.entity.dto.ElementResponse;
import com.zjb.ruleplatform.entity.dto.ElementUpdateRequest;

import java.util.List;

/**
 * 元素服务层
 *
 * @author v-lixing.ea
 */
public interface ElementService {

    /**
     * 分页查询元素
     *
     * @param pageRequest
     * @return
     */
    PageResult<ElementResponse> selectElementPageList(PageRequest<String> pageRequest);

    /**
     * 根据ID查询
     *
     * @param id
     * @return
     */
    ElementResponse get(Integer id);

    /**
     * 新增元素
     *
     * @param elementAddRequest
     * @return
     */
    ElementResponse add(ElementAddRequest elementAddRequest);


    List<ElementResponse> listByIds(List<Integer> ids);


}
