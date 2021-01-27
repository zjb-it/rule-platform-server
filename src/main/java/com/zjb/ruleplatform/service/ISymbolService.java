package com.zjb.ruleplatform.service;


import com.zjb.ruleplatform.entity.dto.SymbolResponse;

import java.util.List;

public interface ISymbolService {
    /**
     * 根据类型选择symbol
     *
     * @param valueDataType 左值类型
     * @return list
     */
    List<SymbolResponse> get(String valueDataType);

    /**
     * 决策表支持x in COLLECTION
     *
     * @param symbolRequest 左值类型
     * @return list
     */
    //List<SymbolResponse> getDecisionSymbol(SymbolRequest symbolRequest);
}
