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
import cn.hutool.core.exceptions.ValidateException;
import cn.hutool.core.lang.Validator;
import cn.hutool.core.util.NumberUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zjb.ruleengine.core.enums.DataTypeEnum;
import com.zjb.ruleengine.core.enums.Symbol;
import com.zjb.ruleengine.core.enums.ValueTypeEnum;
import com.zjb.ruleplatform.entity.RuleEngineCondition;
import com.zjb.ruleplatform.entity.RuleEngineElement;
import com.zjb.ruleplatform.entity.RuleEngineVariable;
import com.zjb.ruleplatform.entity.common.PageRequest;
import com.zjb.ruleplatform.entity.common.PageResult;
import com.zjb.ruleplatform.entity.dto.*;
import com.zjb.ruleplatform.entity.vo.LeftBean;
import com.zjb.ruleplatform.manager.RuleEngineConditionManager;
import com.zjb.ruleplatform.manager.RuleEngineElementManager;
import com.zjb.ruleplatform.manager.RuleEngineVariableManager;
import com.zjb.ruleplatform.service.ISymbolService;
import com.zjb.ruleplatform.service.RuleEngineConditionService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import javax.validation.ValidationException;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 〈一句话功能简述〉<br>
 * 〈〉
 *
 * @author v-dingqianwen.ea
 * @create 2019/8/6
 * @since 1.0.0
 */
@Transactional(rollbackFor = Exception.class)
@Service
@Slf4j
public class RuleEngineConditionServiceImpl implements RuleEngineConditionService {
    @Resource
    private RuleEngineConditionManager ruleEngineConditionManager;
    @Resource
    private RuleEngineElementManager ruleEngineElementManager;
    @Resource
    private RuleEngineVariableManager ruleEngineVariableManager;
    @Resource
    private ISymbolService iSymbolService;


    /**
     * @param pageRequest 查询条件与分页信息
     * @return RuleEngineConditionResponse
     */
    @Override
    public PageResult<RuleEngineConditionResponse> list(PageRequest<String> pageRequest) {
        PageResult<RuleEngineConditionResponse> pageResult = new PageResult<>();
        String name = pageRequest.getQuery();
        //获取到分页数据
        PageRequest.PageBase page = pageRequest.getPage();
        //请求参数
        QueryWrapper<RuleEngineCondition> queryWrapper = new QueryWrapper<>();
        if (Validator.isNotEmpty(name)) {
            queryWrapper.lambda().like(RuleEngineCondition::getName, name);
        }
        queryWrapper.lambda().orderByDesc(RuleEngineCondition::getId);
        //查询条件
        IPage<RuleEngineCondition> iPage = ruleEngineConditionManager.page(new Page<>(page.getPageIndex(), page.getPageSize()), queryWrapper);
        List<RuleEngineCondition> records = iPage.getRecords();
        pageResult.setTotal(iPage.getTotal());
        if (CollUtil.isEmpty(records)) {
            pageResult.setData(Collections.emptyList());
            return pageResult;
        }
        HashMap<Long, RuleEngineVariable> variableMap = new HashMap<>(page.getPageSize());
        HashMap<Long, RuleEngineElement> elementMap = new HashMap<>(page.getPageSize());
        //查询所需缓存数据
        listCache(records, variableMap, elementMap);
        //类型转换
        List<RuleEngineConditionResponse> collect = records.stream().map(e -> {
            RuleEngineConditionResponse response = new RuleEngineConditionResponse();
            response.setId(e.getId());
            response.setName(e.getName());
            String left = "";
            //判断左边值类型
            final String leftValueType = e.getLeftValueType();
            if (Objects.equals(leftValueType, ValueTypeEnum.CONSTANT.name())) {
                //固定值
                left = e.getLeftValue();
            } else if (Objects.equals(leftValueType, ValueTypeEnum.VARIABLE.name())) {
                //变量
                left = variableMap.get(Long.parseLong(e.getLeftValue())).getName();
            } else if (Objects.equals(leftValueType, ValueTypeEnum.ELEMENT.name())) {
                //元素
                left = elementMap.get(Long.parseLong(e.getLeftValue())).getName();

            }
            //判断右边值类型
            String right = "";
            final String rightValueType = e.getRightValueType();
            if (Objects.equals(rightValueType, ValueTypeEnum.CONSTANT.name())) {
                //固定值
                right = e.getRightValue();
            } else if (Objects.equals(rightValueType, ValueTypeEnum.VARIABLE.name())) {
                //变量
                right = variableMap.get(Long.parseLong(e.getRightValue())).getName();
            } else if (Objects.equals(rightValueType, ValueTypeEnum.ELEMENT.name())) {
                //元素
                right = elementMap.get(Long.parseLong(e.getRightValue())).getName();
            }
            response.setConfig(String.format("%s %s %s", left, e.getSymbol(), right));
            return response;
        }).collect(Collectors.toList());
        pageResult.setData(collect);
        return pageResult;
    }

    /**
     * 调用list接口所需要的缓存数据
     *
     * @param records     查询的数据
     * @param variableMap 变量缓存
     * @param elementMap  元素缓存
     */
    public void listCache(List<RuleEngineCondition> records, HashMap<Long, RuleEngineVariable> variableMap,
                          HashMap<Long, RuleEngineElement> elementMap) {
        Set<Long> variableIdSet = new HashSet<>(11);
        Set<Long> elementIdSet = new HashSet<>(11);
        for (RuleEngineCondition record : records) {
            final String leftValueType = record.getLeftValueType();
            if (ValueTypeEnum.VARIABLE.name().equals(leftValueType)) {
                variableIdSet.add(Long.parseLong(record.getLeftValue()));
            } else if (ValueTypeEnum.ELEMENT.name().equals(leftValueType)) {
                elementIdSet.add(Long.parseLong(record.getLeftValue()));
            }
            final String rightValueType = record.getRightValueType();
            if (ValueTypeEnum.VARIABLE.name().equals(rightValueType)) {
                variableIdSet.add(Long.parseLong(record.getRightValue()));
            } else if (ValueTypeEnum.ELEMENT.name().equals(rightValueType)) {
                elementIdSet.add(Long.parseLong(record.getRightValue()));
            }
        }
        //需要用到的左边值放入到map
        if (!variableIdSet.isEmpty()) {
            LambdaQueryWrapper<RuleEngineVariable> wrapper = new LambdaQueryWrapper<>();
            wrapper.in(RuleEngineVariable::getId, variableIdSet);
            List<RuleEngineVariable> leftList = ruleEngineVariableManager.list(wrapper);
            Map<Long, RuleEngineVariable> leftMap = leftList.stream().collect(Collectors.toMap(RuleEngineVariable::getId, v -> v));
            variableMap.putAll(leftMap);

        }
        //需要用到的右边值放入到map
        if (!elementIdSet.isEmpty()) {
            LambdaQueryWrapper<RuleEngineElement> wrapper = new LambdaQueryWrapper<>();
            wrapper.in(RuleEngineElement::getId, elementIdSet);
            List<RuleEngineElement> rightList = ruleEngineElementManager.list(wrapper);
            Map<Long, RuleEngineElement> rightMap = rightList.stream().collect(Collectors.toMap(RuleEngineElement::getId, v -> v));
            elementMap.putAll(rightMap);
        }
    }


    /**
     * 添加
     *
     * @param add 条件
     * @return true删除成功
     */
    @Override
    public Boolean add(ConditionParam add) {
        RuleEngineCondition condition = new RuleEngineCondition();
        condition.setName(add.getName());
        condition.setDescription(add.getDescription());

        //生成值与符号
        generateConditionValue(condition, add);
        return ruleEngineConditionManager.save(condition);
    }

    /**
     * 生成值与符号
     *
     * @param condition condition
     * @param add       add
     */
    @Override
    public void generateConditionValue(RuleEngineCondition condition, ConditionParam add) {
        //左边值
        String leftType = generateConditionValueLeft(condition, add.getConfig().getLeftVariable());
        //右边值
        String rightType = generateConditionValueRight(condition, add.getConfig().getRightVariable());
        //运算符号
        String symbol = add.getConfig().getSymbol();

        final DataTypeEnum byName = DataTypeEnum.valueOf(leftType);
        if (byName == null) {
            throw new ValidationException(String.format("%s类型不存在", leftType));
        }
        Symbol symbolType = Symbol.getSymbolByDataType(byName, symbol);
        //例如：collection 中没有>,<....
        if (symbolType == null) {
            throw new ValidationException("值与符号不匹配");
        }
        condition.setSymbol(symbol);
        condition.setSymbolName(symbolType.getName());
        condition.setSymbolType(symbolType.getType().name());

        //根据左值类型查询所支持的符号以及类型
        List<SymbolResponse> symbolResponses = iSymbolService.get(leftType);
        //检查是否匹配
        boolean addCheckExecute = addCheckExecute(symbolResponses, symbol, rightType);
        if (!addCheckExecute) {
            throw new ValidationException("左值类型与右值类型不匹配");
        }
    }

    /**
     * 左边解析
     *
     * @param condition condition
     * @return leftType
     */
    private String generateConditionValueLeft(RuleEngineCondition condition, LeftBean left) {

        if (Validator.isEmpty(left.getValue())) {
            throw new ValidationException("左值不能为空");
        }
        String type = left.getValueDataType();
        condition.setLeftValueType(left.getValueType());
        condition.setLeftValueDataType(type);
        condition.setLeftValue(left.getValue());
        //如果是固定值
        if (Objects.equals(type, ValueTypeEnum.CONSTANT.name())) {
            if (Validator.isEmpty(left.getValueType())) {
                throw new ValidationException("左值类型不能为空");
            }
            addConstantCheck(left.getValueType(), left.getValue());
        } else if (Objects.equals(type, ValueTypeEnum.VARIABLE.name())) {
            //变量
            RuleEngineVariable byId = getVariableById(Long.valueOf(left.getValue()));
            if (byId == null) {
                throw new ValidationException("左值变量不存在");
            }
            condition.setLeftValueDataType(byId.getValueDataType());
        } else if (Objects.equals(type, ValueTypeEnum.ELEMENT.name())) {
            //元素
            RuleEngineElement byId = getElementById(Long.valueOf(left.getValue()));
            if (byId == null) {
                throw new ValidationException("左值元素不存在");
            }
            condition.setLeftValueDataType(byId.getValueDataType());
        } /*else if (Objects.equals(type, VariableTypeEnum.RESULT.getStatus())) {
            //默认ValueType为COLLECTION
            condition.setLeftDataType(leftType = DataType.COLLECTION.name());
        }*/
        return left.getValueDataType();
    }

    private String generateConditionValueRight(RuleEngineCondition condition, LeftBean left) {

        if (Validator.isEmpty(left.getValue())) {
            throw new ValidationException("左值不能为空");
        }
        String type = left.getValueDataType();
        condition.setRightValueType(left.getValueType());
        condition.setRightValueDataType(type);
        condition.setRightValue(left.getValue());
        //如果是固定值
        if (Objects.equals(type, ValueTypeEnum.CONSTANT.name())) {
            if (Validator.isEmpty(left.getValueType())) {
                throw new ValidationException("左值类型不能为空");
            }
            addConstantCheck(left.getValueType(), left.getValue());
        } else if (Objects.equals(type, ValueTypeEnum.VARIABLE.name())) {
            //变量
            RuleEngineVariable byId = getVariableById(Long.valueOf(left.getValue()));
            if (byId == null) {
                throw new ValidationException("左值变量不存在");
            }
            condition.setRightValueDataType(byId.getValueDataType());
        } else if (Objects.equals(type, ValueTypeEnum.ELEMENT.name())) {
            //元素
            RuleEngineElement byId = getElementById(Long.valueOf(left.getValue()));
            if (byId == null) {
                throw new ValidationException("左值元素不存在");
            }
            condition.setRightValueDataType(byId.getValueDataType());
        } /*else if (Objects.equals(type, VariableTypeEnum.RESULT.getStatus())) {
            //默认ValueType为COLLECTION
            condition.setLeftDataType(leftType = DataType.COLLECTION.name());
        }*/
        return left.getValueDataType();
    }
    /**
     * 条件更新
     *
     * @param update update
     * @return true
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public ConditionParam update(ConditionParam update ) {
        RuleEngineCondition byId = getConditionById(update.getId());
        if (byId == null) {
            throw new ValidationException("条件不存在");
        }
        //if (!byId.getName().equals(update.getName())) {
        //    //如果文件名被修改了，验证数据库中是否已经存在此条件了
        //    PlainResult<Boolean> result = validateUniqName(update.getName());
        //    if (result.getData()) {
        //        throw new ValidationException(result.getMessage());
        //    }
        //}

        //创建RuleEngineCondition对象
        RuleEngineCondition condition = new RuleEngineCondition();
        condition.setId(update.getId());
        condition.setName(update.getName());
        condition.setDescription(update.getDescription());
        generateConditionValue(condition, update);
        ruleEngineConditionManager.updateById(condition);

        return ruleEngineConditionTypeConversion(condition, null);
    }

    /**
     * 检验类型与值
     *
     * @param valueType 值类型
     * @param value     值
     */
    private void addConstantCheck(String valueType, String value) {
        boolean boo;
        switch (valueType) {
            case "NUMBER":
                boo = NumberUtil.isNumber(value);
                break;
            case "BOOLEAN":
                boo = "false".equals(value) || "true".equals(value);
                break;
            default:
                return;
        }
        if (!boo) {
            throw new ValidationException("值与类型不匹配");
        }
    }

    /**
     * 验证类型是否匹配
     *
     * @param symbolResponses symbolResponses
     * @param symbol          symbol
     * @param valueType       valueType
     */
    public static boolean addCheckExecute(List<SymbolResponse> symbolResponses, String symbol, String valueType) {
        for (SymbolResponse symbolResponse : symbolResponses) {
            String responseSymbol = symbolResponse.getSymbol();
            if (responseSymbol.equals(symbol)) {
                //如果符号匹配
                List<String> valueTypes = symbolResponse.getValueDataTypes();
                if (valueTypes.contains(valueType)) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * 根据id查询条件
     *
     * @param id 条件id
     * @return 条件
     */
    @Override
    public ConditionParam get(Long id) {
        RuleEngineCondition ruleEngineCondition = getConditionById(id);
        if (ruleEngineCondition == null) {
            return null;
        }
        return ruleEngineConditionTypeConversion(ruleEngineCondition, null);
    }

    /**
     * RuleEngineCondition类型转为 GetRuleEngineConditionResponse
     *
     * @param condition  原始类型
     * @param ruleAllConditionInfo see.. 应对规则配置页面较多规则条件时使用
     * @return 转换后的类型
     */
    public ConditionParam ruleEngineConditionTypeConversion(RuleEngineCondition condition, RuleAllConditionInfo ruleAllConditionInfo) {
        ConditionParam conditionResponse = new ConditionParam();
        conditionResponse.setId(condition.getId());
        conditionResponse.setName(condition.getName());
        conditionResponse.setDescription(condition.getDescription());
        //配置
        ConfigBean configBean = new ConfigBean();
        //左
        final LeftBean leftBean = new LeftBean(condition.getLeftValueDataType(),condition.getLeftValue(),"",condition.getLeftValueType());
        getLeftBean(leftBean, ruleAllConditionInfo);
        configBean.setLeftVariable(leftBean);
        //符号
        configBean.setSymbol(condition.getSymbol());
        final LeftBean rightBean = new LeftBean(condition.getRightValueDataType(), condition.getRightValue(), "", condition.getRightValueType());
        getLeftBean(rightBean, ruleAllConditionInfo);
        configBean.setRightVariable(rightBean);
        conditionResponse.setConfig(configBean);
        return conditionResponse;
    }

    private void getLeftBean(LeftBean leftBean, RuleAllConditionInfo ruleAllConditionInfo) {
        String type = leftBean.getValueDataType();
        final String value = leftBean.getValue();
        if (Objects.equals(type, ValueTypeEnum.CONSTANT.name())) {
            //固定值
            leftBean.setValueName(value);
        } else if (Objects.equals(type, ValueTypeEnum.VARIABLE.name())) {
            RuleEngineVariable variableManagerById;
            if (ruleAllConditionInfo != null) {
                variableManagerById = ruleAllConditionInfo.getVariableMap().get(Long.parseLong(value));
            } else {
                variableManagerById = ruleEngineVariableManager.getById(value);
            }
            leftBean.setValueName(variableManagerById.getName());
        } else if (Objects.equals(type, ValueTypeEnum.ELEMENT.name())) {
            RuleEngineElement element;
            if (ruleAllConditionInfo != null) {
                element = ruleAllConditionInfo.getElementMap().get(Long.parseLong(value));
            } else {
                element = ruleEngineElementManager.getById(value);
            }
            leftBean.setValueName(element.getName());
        }

    }

    /**
     * @param id id
     * @return RuleEngineCondition
     */
    private RuleEngineCondition getConditionById(Long id) {
        LambdaQueryWrapper<RuleEngineCondition> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(RuleEngineCondition::getId, id);
        //queryWrapper.eq(RuleEngineCondition::getDeleted, DeletedEnum.ENABLE.getStatus());
        return ruleEngineConditionManager.getOne(queryWrapper);
    }

    /**
     * 根据id查询元素，同时绑定业务组
     *
     * @param id id
     * @return RuleEngineElement
     */
    private RuleEngineElement getElementById(Long id) {
        LambdaQueryWrapper<RuleEngineElement> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(RuleEngineElement::getId, id);
        //wrapper.eq(RuleEngineElement::getDeleted, DeletedEnum.ENABLE.getStatus());
        return ruleEngineElementManager.getOne(wrapper);
    }

    /**
     * 根据id查询变量，同时绑定业务组
     *
     * @param id id
     * @return RuleEngineVariable
     */
    private RuleEngineVariable getVariableById(Long id) {
        LambdaQueryWrapper<RuleEngineVariable> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(RuleEngineVariable::getId, id);
        //wrapper.eq(RuleEngineVariable::getDeleted, DeletedEnum.ENABLE.getStatus());
        return ruleEngineVariableManager.getOne(wrapper);
    }




    /**
     * 根据Ids批量查询条件
     *
     * @param ids 条件id
     * @return data
     */
    @Override
    public List<IdAndName> getByIds(List<Integer> ids) {
        if (CollUtil.isEmpty(ids)) {
            throw new ValidateException("查询条件id不能为空");
        }
        List<RuleEngineCondition> ruleEngineConditions = ruleEngineConditionManager.lambdaQuery().in(RuleEngineCondition::getId, ids).list();
        if (CollUtil.isEmpty(ruleEngineConditions)) {
            return new ArrayList<>();
        }
        return ruleEngineConditions.stream().map(m -> {
            IdAndName idAndName = new IdAndName();
            idAndName.setId(m.getId());
            idAndName.setName(m.getName());
            return idAndName;
        }).collect(Collectors.toList());
    }
}