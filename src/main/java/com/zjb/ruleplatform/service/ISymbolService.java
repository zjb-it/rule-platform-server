package com.zjb.ruleplatform.service;

import com.founder.ego.vo.ruleengine.SymbolRequest;
import com.founder.ego.vo.ruleengine.SymbolResponse;

import java.util.List;

public interface ISymbolService {
    /**
     * 根据类型选择symbol
     *
     * @param symbolRequest 左值类型
     * @return list
     */
    List<SymbolResponse> get(SymbolRequest symbolRequest);

    /**
     * 决策表支持x in COLLECTION
     *
     * @param symbolRequest 左值类型
     * @return list
     */
    List<SymbolResponse> getDecisionSymbol(SymbolRequest symbolRequest);
}
