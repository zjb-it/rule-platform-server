/**
 * *****************************************************
 * Copyright (C) 2019 zjb.com. All Rights Reserved
 * This file is part of zjb zjb project.
 * Unauthorized copy of this file, via any medium is strictly prohibited.
 * Proprietary and Confidential.
 * ****************************************************
 * <p>
 * History:
 * <author>            <time>          <version>          <desc>
 * 作者姓名           修改时间           版本号            描述
 */
package com.zjb.ruleplatform.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.EnumUtil;
import com.google.common.collect.Lists;
import com.oracle.jrockit.jfr.DataType;
import com.zjb.ruleengine.core.enums.DataTypeEnum;
import com.zjb.ruleengine.core.enums.Symbol;
import com.zjb.ruleplatform.entity.dto.SymbolResponse;
import com.zjb.ruleplatform.service.ISymbolService;
import org.springframework.stereotype.Service;

import javax.validation.ValidationException;
import javax.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * 〈一句话功能简述〉<br>
 * 〈〉
 *
 * @author v-dingqianwen.ea
 * @create 2019/8/7
 * @since 1.0.0
 */
@Service
public class ISymbolServiceImpl implements ISymbolService {
    /**
     * 根据类型选择symbol
     *
     * @return list
     */
    @Override
    public List<SymbolResponse> get(String valueType) {

        List<SymbolResponse> arrayList = new ArrayList<>(10);
        //如果是集合类型
        if (DataTypeEnum.COLLECTION.name().equals(valueType)) {
            List<Symbol> symbols = Symbol.listSymbolsByType(DataTypeEnum.COLLECTION);
            //Map<String, DataType> enumMap = EnumUtil.getEnumMap(DataTypeEnum.class);
            List<String> collection = new ArrayList<>();
            for (Symbol symbol : symbols) {
                SymbolResponse sr = new SymbolResponse();
                sr.setSymbol(symbol.getSymbol());
                //如果左侧是COLLECTION，运算符为=，则右侧只能为COLLECTION
                if (symbol.getType().equals(DataTypeEnum.COLLECTION) && symbol.getSymbol().equals(Symbol.set_eq.getSymbol())) {
                    sr.setValueTypes(Collections.singletonList(DataTypeEnum.COLLECTION.name()));
                } else {
                    sr.setValueTypes(collection);
                }
                arrayList.add(sr);
            }
        } else if (DataTypeEnum.NUMBER.name().equals(valueType)) {
            List<Symbol> symbols = Symbol.listSymbolsByType(DataTypeEnum.NUMBER);
            for (Symbol symbol : symbols) {
                SymbolResponse sr = new SymbolResponse();
                sr.setSymbol(symbol.getSymbol());
                if ("in".equals(symbol.getSymbol()) || "notIn".equals(symbol.getSymbol())) {
                    sr.setValueTypes(Lists.newArrayList(DataTypeEnum.COLLECTION.name()));
                } else {
                    sr.setValueTypes(Lists.newArrayList(DataTypeEnum.NUMBER.name()));
                }
                arrayList.add(sr);
            }
        } else if (DataTypeEnum.STRING.name().equals(valueType)) {
            List<Symbol> symbols = Symbol.listSymbolsByType(DataTypeEnum.STRING);
            for (Symbol symbol : symbols) {
                SymbolResponse sr = new SymbolResponse();
                sr.setSymbol(symbol.getSymbol());
                if ("in".equals(symbol.getSymbol()) || "notIn".equals(symbol.getSymbol())) {
                    sr.setValueTypes(Lists.newArrayList(DataTypeEnum.COLLECTION.name()));
                } else {
                    sr.setValueTypes(Lists.newArrayList(DataTypeEnum.STRING.name()));
                }
                arrayList.add(sr);
            }
        } else if (DataTypeEnum.BOOLEAN.name().equals(valueType)) {
            List<Symbol> symbols = Symbol.listSymbolsByType(DataTypeEnum.BOOLEAN);
            if (CollUtil.isEmpty(symbols)) {
                return arrayList;
            }
            for (Symbol symbol : symbols) {
                SymbolResponse sr = new SymbolResponse();
                sr.setSymbol(symbol.getSymbol());
                if ("in".equals(symbol.getSymbol()) || "notIn".equals(symbol.getSymbol())) {
                    sr.setValueTypes(Lists.newArrayList(DataTypeEnum.COLLECTION.name()));
                } else {
                    sr.setValueTypes(Lists.newArrayList(DataTypeEnum.BOOLEAN.name()));
                }
                arrayList.add(sr);
            }
        } else {
            throw new ValidationException("不支持此类型");
        }
        return arrayList;
    }

    /**
     * 决策表支持x in COLLECTION
     *
     * @param symbolRequest 左值类型
     * @return list
     */
    //@Override
    //public List<SymbolResponse> getDecisionSymbol(SymbolRequest symbolRequest) {
    //    return get(symbolRequest);
  /*      SymbolResponse in = new SymbolResponse();
        in.setSymbol("in");
        in.setValueTypes(Lists.newArrayList(DataTypeEnum.COLLECTION.name()));
        symbolResponses.add(in);

        SymbolResponse notIn = new SymbolResponse();
        notIn.setSymbol("notin");
        notIn.setValueTypes(Lists.newArrayList(DataTypeEnum.COLLECTION.name()));
        symbolResponses.add(notIn);
        return symbolResponses;*/
    //}
}