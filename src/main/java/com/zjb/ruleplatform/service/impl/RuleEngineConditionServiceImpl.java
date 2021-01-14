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
import com.founder.ego.annotation.UpdateRuleEngine;
import com.founder.ego.common.request.PageBase;
import com.founder.ego.common.request.PageRequest;
import com.founder.ego.common.response.PageResult;
import com.founder.ego.common.response.PlainResult;
import com.founder.ego.common.response.Rows;
import com.founder.ego.enumbean.DataTypeEnum;
import com.founder.ego.enumbean.DeletedEnum;
import com.founder.ego.enumbean.PublishEnum;
import com.founder.ego.enumbean.RuleSetUpdateSourceEnum;
import com.founder.ego.ruleengine.core.enums.DataType;
import com.founder.ego.ruleengine.core.enums.Symbol;
import com.founder.ego.ruleengine.core.enums.VariableTypeEnum;
import com.founder.ego.service.ruleengine.*;
import com.founder.ego.store.bpm.entity.RuleEngineCondition;
import com.founder.ego.store.bpm.entity.RuleEngineElement;
import com.founder.ego.store.bpm.entity.RuleEngineVariable;
import com.founder.ego.store.bpm.manager.RuleEngineConditionManager;
import com.founder.ego.store.bpm.manager.RuleEngineElementManager;
import com.founder.ego.store.bpm.manager.RuleEngineRuleSetManager;
import com.founder.ego.store.bpm.manager.RuleEngineVariableManager;
import com.founder.ego.store.bpm.mapper.CustomRuleEngineRuleSetJsonMapper;
import com.founder.ego.utils.DB;
import com.founder.ego.utils.PageUtils;
import com.founder.ego.utils.check.Check;
import com.founder.ego.vo.ruleengine.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import javax.validation.ValidationException;
import java.util.*;
import java.util.stream.Collectors;

import static com.founder.ego.enumbean.DeletedEnum.ENABLE;

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
    @Resource
    private RuleEngineLoadService ruleEngineLoadService;
    @Resource
    private CustomRuleEngineRuleSetJsonMapper customRuleEngineRuleSetJsonMapper;
    @Resource
    private RuleEngineRuleSetManager ruleEngineRuleSetManager;
    @Resource
    private RuleEngineRuleSetService ruleEngineRuleSetService;
    @Resource
    private RuleLockService ruleLockService;


    /**
     * @param pageRequest 查询条件与分页信息
     * @return RuleEngineConditionResponse
     */
    @Override
    public PageResult<RuleEngineConditionResponse> list(PageRequest<RuleEngineConditionRequest> pageRequest) {
        PageResult<RuleEngineConditionResponse> pageResult = new PageResult<>();
        RuleEngineConditionRequest query = pageRequest.getQuery();
        //获取到分页数据
        PageBase page = pageRequest.getPage();
        //请求参数
        String name = query.getName();
        QueryWrapper<RuleEngineCondition> queryWrapper = new QueryWrapper<>();
        //排序
        PageUtils.defaultOrder(pageRequest.getOrders(), queryWrapper, RuleEngineCondition::getId);
        if (Validator.isNotEmpty(name)) {
            queryWrapper.lambda().like(RuleEngineCondition::getName, name);
        }
        //获取当前业务组
        RuleEngineBizBean engineBiz = RuleEngineBizServiceImpl.getEngineBiz();
        queryWrapper.lambda().eq(RuleEngineCondition::getBizId, engineBiz.getId());
        queryWrapper.lambda().eq(RuleEngineCondition::getDeleted, DeletedEnum.ENABLE.getStatus());
        if (query.getQueryType().equals(0)) {
            //查询公有条件
            queryWrapper.lambda().eq(RuleEngineCondition::getShowed, DeletedEnum.ENABLE.getStatus());
        } else if (query.getQueryType().equals(1)) {
            queryWrapper.lambda()
                    .eq(RuleEngineCondition::getShowed, DeletedEnum.ENABLE.getStatus())
                    .or(o -> o.eq(RuleEngineCondition::getRuleSetId, query.getRuleSetId())
                            .eq(RuleEngineCondition::getRuleSetType, query.getRuleSetType())
                            .eq(RuleEngineCondition::getShowed, DeletedEnum.DISABLE.getStatus())
                            .eq(RuleEngineCondition::getDeleted, ENABLE.getStatus())
                            .like(RuleEngineCondition::getName, name));
        } else if (query.getQueryType().equals(2)) {
            //查询私有的
            queryWrapper.lambda().eq(RuleEngineCondition::getRuleSetId, query.getRuleSetId())
                    .eq(RuleEngineCondition::getRuleSetType, query.getRuleSetType())
                    .eq(RuleEngineCondition::getShowed, DeletedEnum.DISABLE.getStatus());
        }
        //查询条件
        IPage<RuleEngineCondition> iPage = ruleEngineConditionManager.page(new Page<>(page.getPageIndex(), page.getPageSize()), queryWrapper);
        List<RuleEngineCondition> records = iPage.getRecords();
        if (CollUtil.isEmpty(records)) {
            pageResult.setData(new Rows<>(new ArrayList<>(10), PageUtils.getPageResponse(iPage)));
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
            response.setRuleSetId(e.getRuleSetId());
            response.setRuleSetType(e.getRuleSetType());
            String left = StringUtils.EMPTY;
            //判断左边值类型
            if (Objects.equals(e.getLeftVariableType(), VariableTypeEnum.CONSTANT.getStatus())) {
                //固定值
                left = e.getLeftVariableValue();
            } else if (Objects.equals(e.getLeftVariableType(), VariableTypeEnum.VARIABLE.getStatus())) {
                //变量
                left = Check.els(variableMap.get(e.getLeftVariableId()), RuleEngineVariable::getName);
            } else if (Objects.equals(e.getLeftVariableType(), VariableTypeEnum.ELEMENT.getStatus())) {
                //元素
                left = Check.els(elementMap.get(e.getLeftElementId()), RuleEngineElement::getName);
            }
            //判断右边值类型
            String right = StringUtils.EMPTY;
            if (Objects.equals(e.getRightVariableType(), VariableTypeEnum.CONSTANT.getStatus())) {
                //固定值
                right = e.getRightVariableValue();
            } else if (Objects.equals(e.getRightVariableType(), VariableTypeEnum.VARIABLE.getStatus())) {
                //变量
                right = Check.els(variableMap.get(e.getRightVariableId()), RuleEngineVariable::getName);
            } else if (Objects.equals(e.getRightVariableType(), VariableTypeEnum.ELEMENT.getStatus())) {
                //元素
                right = Check.els(elementMap.get(e.getRightElementId()), RuleEngineElement::getName);
            }
            response.setConfig(String.format("%s %s %s", left, e.getSymbol(), right));
            return response;
        }).collect(Collectors.toList());
        pageResult.setData(new Rows<>(collect, PageUtils.getPageResponse(iPage)));
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
            if (record.getLeftVariableId() != null) {
                variableIdSet.add(record.getLeftVariableId());
            }
            if (record.getRightVariableId() != null) {
                variableIdSet.add(record.getRightVariableId());
            }
            if (record.getLeftElementId() != null) {
                elementIdSet.add(record.getLeftElementId());
            }
            if (record.getRightElementId() != null) {
                elementIdSet.add(record.getRightElementId());
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
     * 逻辑删除条件
     *
     * @param id 条件id
     * @return true删除成功
     */
    @Override
    public Boolean delete(Integer id) {
        //判断是否有用到这个条件
        if (useCount(id) > 0) {
            throw new ValidationException("该条件被规则使用中，不可删除!");
        }
        RuleEngineCondition condition = new RuleEngineCondition();
        condition.setDeleted(DeletedEnum.DISABLE.getStatus());
        condition.setId(Long.valueOf(id));
        //规则/决策内多人编辑验证
        RuleEngineCondition ruleEngineCondition = ruleEngineConditionManager.getById(id);
        //如果是决策表
        if (Objects.equals(ruleEngineCondition.getRuleSetType(), DataTypeEnum.DECISION.getDataType())) {
            ruleLockService.decisionValid(ruleEngineCondition.getRuleSetId());
        }
        //如果是规则表
        if (Objects.equals(ruleEngineCondition.getRuleSetType(), DataTypeEnum.RULESET.getDataType())) {
            ruleLockService.ruleSetValid(ruleEngineCondition.getRuleSetId());
        }
        return ruleEngineConditionManager.updateById(condition);
    }

    /**
     * 添加
     *
     * @param add 条件
     * @return true删除成功
     */
    @Override
    public Boolean add(AddRuleEngineConditionParam add) {
        RuleEngineBizBean engineBiz = RuleEngineBizServiceImpl.getEngineBiz();
        PlainResult<Boolean> result = validateUniqName(add.getName());
        if (result.getData()) {
            throw new ValidationException(result.getMessage());
        }
        RuleEngineCondition condition = new RuleEngineCondition();
        condition.setBizId(engineBiz.getId());
        condition.setBizCode(engineBiz.getBizCode());
        condition.setBizName(engineBiz.getBizName());
        condition.setDeleted(0);
        condition.setName(add.getName());
        condition.setDescription(add.getDescription());
        if (add.isNotNull()) {
            condition.setRuleSetId(add.getRuleSetId());
            condition.setRuleSetType(add.getRuleSetType());
            //规则内创建条件无法在基础组件列表展示
            condition.setShowed(DeletedEnum.DISABLE.getStatus());
            //如果是决策表
            if (Objects.equals(add.getRuleSetType(), DataTypeEnum.DECISION.getDataType())) {
                ruleLockService.decisionValid(add.getRuleSetId());
            }
            //如果是规则表
            if (Objects.equals(add.getRuleSetType(), DataTypeEnum.RULESET.getDataType())) {
                ruleLockService.ruleSetValid(add.getRuleSetId());
            }
        }
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
    public void generateConditionValue(RuleEngineCondition condition, AddRuleEngineConditionParam add) {
        //左边值
        String leftType = generateConditionValueLeft(condition, add);
        //右边值
        String rightType = generateConditionValueRight(condition, add);
        //运算符号
        String symbol = add.getConfig().getSymbol();
        DataType byName = DataType.getDataTypeByName(leftType);
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
        List<SymbolResponse> symbolResponses = iSymbolService.get(new SymbolRequest(leftType));
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
     * @param add       add
     * @return leftType
     */
    private String generateConditionValueLeft(RuleEngineCondition condition, AddRuleEngineConditionParam add) {
        String leftType = StringUtils.EMPTY;
        ConfigBean.LeftBean left = add.getConfig().getLeftVariable();
        if (Validator.isEmpty(left.getValue())) {
            throw new ValidationException("左值不能为空");
        }
        Integer type = left.getType();
        condition.setLeftVariableType(type);
        //如果是固定值
        if (Objects.equals(type, VariableTypeEnum.CONSTANT.getStatus())) {
            if (Validator.isEmpty(left.getValueType())) {
                throw new ValidationException("左值类型不能为空");
            }
            addConstantCheck(left.getValueType(), left.getValue());
            condition.setLeftDataType(leftType = left.getValueType());
            condition.setLeftVariableValue(left.getValue());
        } else if (Objects.equals(type, VariableTypeEnum.VARIABLE.getStatus())) {
            //变量
            condition.setLeftVariableId(Long.valueOf(left.getValue()));
            RuleEngineVariable byId = getVariableById(Long.valueOf(left.getValue()));
            if (byId == null) {
                throw new ValidationException("左值变量不存在");
            }
            condition.setLeftDataType(leftType = byId.getValueType());
        } else if (Objects.equals(type, VariableTypeEnum.ELEMENT.getStatus())) {
            //元素
            condition.setLeftElementId(Long.valueOf(left.getValue()));
            RuleEngineElement byId = getElementById(Long.valueOf(left.getValue()));
            if (byId == null) {
                throw new ValidationException("左值元素不存在");
            }
            condition.setLeftDataType(leftType = byId.getValueType());
        } else if (Objects.equals(type, VariableTypeEnum.RESULT.getStatus())) {
            //默认ValueType为COLLECTION
            condition.setLeftDataType(leftType = DataType.COLLECTION.name());
        }
        return leftType;
    }

    /**
     * 右边解析
     *
     * @param condition condition
     * @param add       add
     * @return rightType
     */
    private String generateConditionValueRight(RuleEngineCondition condition, AddRuleEngineConditionParam add) {
        String rightType = StringUtils.EMPTY;
        ConfigBean.RightBean right = add.getConfig().getRightVariable();
        if (Validator.isEmpty(right.getValue())) {
            throw new ValidationException("右值不能为空");
        }
        Integer type1 = right.getType();
        condition.setRightVariableType(type1);
        //固定值
        if (Objects.equals(type1, VariableTypeEnum.CONSTANT.getStatus())) {
            if (Validator.isEmpty(right.getValueType())) {
                throw new ValidationException("右值类型不能为空");
            }
            addConstantCheck(right.getValueType(), right.getValue());
            condition.setRightDataType(rightType = right.getValueType());
            condition.setRightVariableValue(right.getValue());
        } else if (Objects.equals(type1, VariableTypeEnum.VARIABLE.getStatus())) {
            //变量
            condition.setRightVariableId(Long.valueOf(right.getValue()));
            RuleEngineVariable byId = getVariableById(Long.valueOf(right.getValue()));
            if (byId == null) {
                throw new ValidationException("右值变量不存在");
            }
            condition.setRightDataType(rightType = byId.getValueType());
        } else if (Objects.equals(type1, VariableTypeEnum.ELEMENT.getStatus())) {
            //元素
            condition.setRightElementId(Long.valueOf(right.getValue()));
            RuleEngineElement byId = getElementById(Long.valueOf(right.getValue()));
            if (byId == null) {
                throw new ValidationException("右值元素不存在");
            }
            condition.setRightDataType(rightType = byId.getValueType());
        } else if (Objects.equals(type1, VariableTypeEnum.RESULT.getStatus())) {
            //默认ValueType为COLLECTION
            condition.setRightDataType(rightType = DataType.COLLECTION.name());
        }
        return rightType;
    }

    /**
     * 条件更新
     *
     * @param update update
     * @return true
     */
    @Override
    @UpdateRuleEngine
    @Transactional(rollbackFor = Exception.class)
    public GetRuleEngineConditionResponse update(UpdateRuleEngineBizRequest update) {
        RuleEngineCondition byId = getConditionById(update.getId());
        if (byId == null) {
            throw new ValidationException("条件不存在");
        }
        if (!byId.getName().equals(update.getName())) {
            //如果文件名被修改了，验证数据库中是否已经存在此条件了
            PlainResult<Boolean> result = validateUniqName(update.getName());
            if (result.getData()) {
                throw new ValidationException(result.getMessage());
            }
        }
        //规则/决策内多人编辑验证
        //如果是决策表
        if (Objects.equals(byId.getRuleSetType(), DataTypeEnum.DECISION.getDataType())) {
            ruleLockService.decisionValid(byId.getRuleSetId());
        }
        //如果是规则表
        if (Objects.equals(byId.getRuleSetType(), DataTypeEnum.RULESET.getDataType())) {
            ruleLockService.ruleSetValid(byId.getRuleSetId());
        }
        //创建RuleEngineCondition对象
        RuleEngineCondition condition = new RuleEngineCondition();
        condition.setId(update.getId());
        condition.setName(update.getName());
        condition.setDescription(update.getDescription());
        generateConditionValue(condition, update);
        ruleEngineConditionManager.updateById(condition);
        ruleEngineLoadService.createRuleSetJson(update.getId().intValue(), RuleSetUpdateSourceEnum.CONDITION);
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
                List<String> valueTypes = symbolResponse.getValueTypes();
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
    public GetRuleEngineConditionResponse get(Long id) {
        RuleEngineCondition ruleEngineCondition = getConditionById(id);
        if (ruleEngineCondition == null) {
            return null;
        }
        return ruleEngineConditionTypeConversion(ruleEngineCondition, null);
    }

    /**
     * RuleEngineCondition类型转为 GetRuleEngineConditionResponse
     *
     * @param ruleEngineCondition  原始类型
     * @param ruleAllConditionInfo see.. 应对规则配置页面较多规则条件时使用
     * @return 转换后的类型
     */
    public GetRuleEngineConditionResponse ruleEngineConditionTypeConversion(RuleEngineCondition ruleEngineCondition, RuleAllConditionInfo ruleAllConditionInfo) {
        GetRuleEngineConditionResponse conditionResponse = new GetRuleEngineConditionResponse();
        conditionResponse.setId(ruleEngineCondition.getId());
        conditionResponse.setName(ruleEngineCondition.getName());
        conditionResponse.setDescription(ruleEngineCondition.getDescription());
        //配置
        ConfigBean configBean = new ConfigBean();
        //左
        ConfigBean.LeftBean leftBean = new ConfigBean.LeftBean();
        Integer type = ruleEngineCondition.getLeftVariableType();
        leftBean.setType(type);
        if (Objects.equals(type, VariableTypeEnum.CONSTANT.getStatus())) {
            //固定值
            leftBean.setValue(String.valueOf(ruleEngineCondition.getLeftVariableValue()));
            leftBean.setValueName(ruleEngineCondition.getLeftVariableValue());
            leftBean.setValueType(ruleEngineCondition.getLeftDataType());
        } else if (Objects.equals(type, VariableTypeEnum.VARIABLE.getStatus())) {
            RuleEngineVariable variableManagerById;
            if (ruleAllConditionInfo != null) {
                variableManagerById = ruleAllConditionInfo.getVariableMap().get(ruleEngineCondition.getLeftVariableId());
            } else {
                variableManagerById = DB.getAndCache(RuleEngineVariable.class, ruleEngineCondition.getLeftVariableId());
            }
            leftBean.setValue(String.valueOf(ruleEngineCondition.getLeftVariableId()));
            leftBean.setValueName(variableManagerById.getName());
            leftBean.setValueType(variableManagerById.getValueType());
        } else if (Objects.equals(type, VariableTypeEnum.ELEMENT.getStatus())) {
            RuleEngineElement element;
            if (ruleAllConditionInfo != null) {
                element = ruleAllConditionInfo.getElementMap().get(ruleEngineCondition.getLeftElementId());
            } else {
                element = DB.getAndCache(RuleEngineElement.class, ruleEngineCondition.getLeftElementId());
            }
            leftBean.setValue(String.valueOf(ruleEngineCondition.getLeftElementId()));
            leftBean.setValueName(element.getName());
            leftBean.setValueType(element.getValueType());
        }
        configBean.setLeftVariable(leftBean);
        //符号
        configBean.setSymbol(ruleEngineCondition.getSymbol());
        //右
        ConfigBean.RightBean rightBean = new ConfigBean.RightBean();
        Integer rightVariableType = ruleEngineCondition.getRightVariableType();
        rightBean.setType(rightVariableType);
        if (Objects.equals(rightVariableType, VariableTypeEnum.CONSTANT.getStatus())) {
            rightBean.setValue(String.valueOf(ruleEngineCondition.getRightVariableValue()));
            rightBean.setValueName(ruleEngineCondition.getRightVariableValue());
            rightBean.setValueType(ruleEngineCondition.getRightDataType());
        } else if (Objects.equals(rightVariableType, VariableTypeEnum.VARIABLE.getStatus())) {
            RuleEngineVariable variableManagerById;
            if (ruleAllConditionInfo != null) {
                variableManagerById = ruleAllConditionInfo.getVariableMap().get(ruleEngineCondition.getRightVariableId());
            } else {
                variableManagerById = DB.getAndCache(RuleEngineVariable.class, ruleEngineCondition.getRightVariableId());
            }
            rightBean.setValue(String.valueOf(variableManagerById.getId()));
            rightBean.setValueName(variableManagerById.getName());
            rightBean.setValueType(variableManagerById.getValueType());
        } else if (Objects.equals(rightVariableType, VariableTypeEnum.ELEMENT.getStatus())) {
            RuleEngineElement element;
            if (ruleAllConditionInfo != null) {
                element = ruleAllConditionInfo.getElementMap().get(ruleEngineCondition.getRightElementId());
            } else {
                element = DB.getAndCache(RuleEngineElement.class, ruleEngineCondition.getRightElementId());
            }
            rightBean.setValue(String.valueOf(element.getId()));
            rightBean.setValueName(element.getName());
            rightBean.setValueType(element.getValueType());
        }
        configBean.setRightVariable(rightBean);
        conditionResponse.setConfig(configBean);
        return conditionResponse;
    }

    /**
     * @param id id
     * @return RuleEngineCondition
     */
    private RuleEngineCondition getConditionById(Long id) {
        LambdaQueryWrapper<RuleEngineCondition> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(RuleEngineCondition::getId, id);
        queryWrapper.eq(RuleEngineCondition::getDeleted, DeletedEnum.ENABLE.getStatus());
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
        wrapper.eq(RuleEngineElement::getDeleted, DeletedEnum.ENABLE.getStatus());
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
        wrapper.eq(RuleEngineVariable::getDeleted, DeletedEnum.ENABLE.getStatus());
        return ruleEngineVariableManager.getOne(wrapper);
    }


    /**
     * 根据名称查询，是否已经存在此条件
     *
     * @param name 条件name
     * @return true
     */
    @Override
    public PlainResult<Boolean> validateUniqName(String name) {
        PlainResult<Boolean> plainResult = new PlainResult<>();
        if (Validator.isEmpty(name)) {
            plainResult.setMessage("名称不能为空");
            plainResult.setData(true);
        } else {
            RuleEngineBizBean engineBiz = RuleEngineBizServiceImpl.getEngineBiz();
            Integer count = ruleEngineConditionManager.lambdaQuery().eq(RuleEngineCondition::getName, name)
                    .eq(RuleEngineCondition::getBizId, engineBiz.getId())
                    .eq(RuleEngineCondition::getDeleted, DeletedEnum.ENABLE.getStatus()).count();
            if (count != 0) {
                plainResult.setMessage("条件已经存在,请重新输入。");
                plainResult.setData(true);
            } else {
                plainResult.setData(false);
            }
        }
        return plainResult;
    }

    /**
     * 条件被多少规则引用
     *
     * @param id 条件id
     * @return 引用数量
     */
    @Override
    public Integer useCount(Integer id) {
        return customRuleEngineRuleSetJsonMapper.countCondition(RuleEngineLoadServiceImpl.CONDITION_PATH, id, Arrays.asList(PublishEnum.WAITING_PUBLISH.getType(), PublishEnum.PUBLISH.getType(), PublishEnum.DRAFT.getType(), PublishEnum.PUBLISH_OTHER_ENV.getType()));
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

    @Transactional(rollbackFor = Exception.class)
    public void updateConditionName() {
        //升级条件为公共组件
        ruleEngineConditionManager.lambdaUpdate()
                .eq(RuleEngineCondition::getShowed, DeletedEnum.DISABLE.getStatus())
                .eq(RuleEngineCondition::getDeleted, DeletedEnum.ENABLE.getStatus())
                .isNull(RuleEngineCondition::getRuleSetId)
                .set(RuleEngineCondition::getShowed, DeletedEnum.ENABLE.getStatus())
                .update();
        //更新生成条件名称
        List<RuleEngineCondition> conditionList = ruleEngineConditionManager.lambdaQuery()
                .eq(RuleEngineCondition::getDeleted, DeletedEnum.ENABLE.getStatus())
                .isNull(RuleEngineCondition::getName)
                .list();
        List<RuleEngineCondition> collect = conditionList.stream()
                //保险起见，再过滤只更新没有名称的条件
                .filter(f -> f.getName() == null || "".equals(f.getName())).peek(e -> {
                    String left = StringUtils.EMPTY;
                    //判断左边值类型
                    if (Objects.equals(e.getLeftVariableType(), VariableTypeEnum.CONSTANT.getStatus())) {
                        //固定值
                        left = e.getLeftVariableValue();
                    } else if (Objects.equals(e.getLeftVariableType(), VariableTypeEnum.VARIABLE.getStatus())) {
                        //变量
                        left = Check.els(DB.getAndCache(RuleEngineVariable.class, e.getLeftVariableId()), RuleEngineVariable::getName);
                    } else if (Objects.equals(e.getLeftVariableType(), VariableTypeEnum.ELEMENT.getStatus())) {
                        //元素
                        left = Check.els(DB.getAndCache(RuleEngineElement.class, e.getLeftElementId()), RuleEngineElement::getName);
                    }
                    //判断右边值类型
                    String right = StringUtils.EMPTY;
                    if (Objects.equals(e.getRightVariableType(), VariableTypeEnum.CONSTANT.getStatus())) {
                        //固定值
                        right = e.getRightVariableValue();
                    } else if (Objects.equals(e.getRightVariableType(), VariableTypeEnum.VARIABLE.getStatus())) {
                        //变量
                        right = Check.els(DB.getAndCache(RuleEngineVariable.class, e.getRightVariableId()), RuleEngineVariable::getName);
                    } else if (Objects.equals(e.getRightVariableType(), VariableTypeEnum.ELEMENT.getStatus())) {
                        //元素
                        right = Check.els(DB.getAndCache(RuleEngineElement.class, e.getRightElementId()), RuleEngineElement::getName);
                    }
                    e.setName(String.format("%s %s %s", left, e.getSymbol(), right));
                }).collect(Collectors.toList());
        //批量更新
        ruleEngineConditionManager.updateBatchById(collect);
    }
}