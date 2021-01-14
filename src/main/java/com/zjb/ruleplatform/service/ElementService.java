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
    PageResult<ElementResponse> selectElementPageList(PageRequest<ElementRequest> pageRequest);

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

    /**
     * 逻辑删除元素(要判断是否在用)
     *
     * @param id
     * @return
     */
    Boolean delete(Integer id);

    /**
     * 更新元素信息(只允许更新 名称与描述)
     *
     * @param elementUpdateRequest
     * @return
     */
    Boolean update(ElementUpdateRequest elementUpdateRequest);

    /**
     * 唯一约束(code全局唯一)
     *
     * @param code
     * @return
     */
    PlainResult<Boolean> vaildateUniqCode(String code);



    List<ElementResponse> getByIds(List<Integer> ids);


}
