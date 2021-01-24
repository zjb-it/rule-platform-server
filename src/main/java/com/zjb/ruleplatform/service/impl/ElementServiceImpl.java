package com.zjb.ruleplatform.service.impl;


import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.lang.Validator;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zjb.ruleplatform.entity.*;
import com.zjb.ruleplatform.entity.common.PageRequest;
import com.zjb.ruleplatform.entity.common.PageResult;
import com.zjb.ruleplatform.entity.dto.ElementAddRequest;
import com.zjb.ruleplatform.entity.dto.ElementResponse;
import com.zjb.ruleplatform.entity.dto.ListRuleEngineVariableRequest;
import com.zjb.ruleplatform.manager.*;
import com.zjb.ruleplatform.service.ElementService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.xml.crypto.dsig.keyinfo.PGPData;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author v-lixing.ea
 */
@Slf4j
@Service
public class ElementServiceImpl implements ElementService {

    @Autowired
    private RuleEngineElementManager ruleEngineElementManager;



    @Override
    public PageResult<ElementResponse> selectElementPageList(PageRequest<ListRuleEngineVariableRequest> pageRequest) {
        QueryWrapper<RuleEngineElement> queryWrapper = new QueryWrapper<>();
        //请求参数
        final ListRuleEngineVariableRequest query = pageRequest.getQuery();
        if (query != null) {
            final String name = query.getName();
            if (Validator.isNotEmpty(name)) {
                queryWrapper.lambda().like(RuleEngineElement::getCodeName, name);
            }
            if (CollUtil.isNotEmpty(query.getValueDataType())) {
                queryWrapper.lambda().in(RuleEngineElement::getValueDataType, query.getValueDataType());
            }
        }
        final PageRequest<ListRuleEngineVariableRequest>.PageBase page = pageRequest.getPage();
        queryWrapper.lambda().orderByDesc(RuleEngineElement::getId);
        IPage<RuleEngineElement> pageData = ruleEngineElementManager.page(new Page<>(page.getPageIndex(), page.getPageSize()), queryWrapper);
        List<RuleEngineElement> ruleEngineElements = pageData.getRecords();
        List<ElementResponse> elementResponses = ruleEngineElements.stream().map(e -> {
            ElementResponse elementResponse = new ElementResponse();
            BeanUtils.copyProperties(e,elementResponse);
            return elementResponse;
        }).collect(Collectors.toList());
        PageResult<ElementResponse> result = new PageResult<>();
        result.setData(elementResponses);
        result.setTotal(pageData.getTotal());
        return result;
    }


    @Override
    public ElementResponse add(ElementAddRequest elementAddRequest) {
        String code = elementAddRequest.getCode();
        RuleEngineElement ruleEngineElement = new RuleEngineElement();

        ruleEngineElement.setName(elementAddRequest.getName());
        ruleEngineElement.setCode(code);
        ruleEngineElement.setValueDataType(elementAddRequest.getValueDataType());
        ruleEngineElement.setDescription(elementAddRequest.getDescription());
        ruleEngineElement.setCodeName(code + elementAddRequest.getName());

        ruleEngineElementManager.save(ruleEngineElement);
        ElementResponse elementResponse = new ElementResponse();
        /*新建一个元素返回*/
        BeanUtils.copyProperties(ruleEngineElement,elementResponse);

        return elementResponse;
    }


    @Override
    public ElementResponse get(Integer id) {
        RuleEngineElement ruleEngineElement = ruleEngineElementManager.getById(id);
        ElementResponse elementResponse = new ElementResponse();
        if (Validator.isEmpty(ruleEngineElement)) {
            return elementResponse;
        }
        BeanUtils.copyProperties(ruleEngineElement,elementResponse);
        return elementResponse;
    }

    @Override
    public List<ElementResponse> listByIds(List<Integer> ids) {

        LambdaQueryWrapper<RuleEngineElement> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.in(RuleEngineElement::getId, ids);

        List<RuleEngineElement> ruleEngineElements = ruleEngineElementManager.list(queryWrapper);
        return ruleEngineElements.stream().map(e -> {
            ElementResponse elementResponse = new ElementResponse();
            BeanUtils.copyProperties(e,elementResponse);
            return elementResponse;
        }).collect(Collectors.toList());
    }


}
