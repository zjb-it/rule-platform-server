package com.zjb.ruleplatform.service.impl;


import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.lang.Validator;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zjb.ruleplatform.entity.common.PageRequest;
import com.zjb.ruleplatform.entity.common.PlainResult;
import com.zjb.ruleplatform.entity.dto.ElementAddRequest;
import com.zjb.ruleplatform.entity.dto.ElementRequest;
import com.zjb.ruleplatform.entity.dto.ElementResponse;
import com.zjb.ruleplatform.entity.dto.ElementUpdateRequest;
import com.zjb.ruleplatform.service.ElementService;
import com.zjb.ruleplatform.service.RuleEngineRuleSetService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author v-lixing.ea
 */
@Slf4j
@Service
public class ElementServiceImpl implements ElementService {

    @Autowired
    private RuleEngineElementManager ruleEngineElementManager;
    @Autowired
    private RuleEngineFunctionManager functionManager;
    @Autowired
    private RuleEngineFunctionParamManager functionParamManager;
    @Autowired
    private RuleEngineConditionManager ruleEngineConditionManager;
    @Autowired
    private RuleEngineVariableManager ruleEngineVariableManager;
    @Autowired
    private RuleEngineVariableParamManager ruleEngineVariableParamManager;
    @Resource
    private RuleEngineRuleSetManager ruleEngineRuleSetManager;
    @Resource
    private RuleEngineRuleSetService ruleEngineRuleSetService;
    @Resource
    private RuleLockService ruleLockService;

    /**
     * 元素code保留字段
     */
    private static Set<String> reserveWord = Stream.of(Context.ReserveWord.values()).map(Context.ReserveWord::name).collect(Collectors.toSet());


    @Override
    public PageResult<ElementResponse> selectElementPageList(PageRequest<ElementRequest> pageRequest) {
        ElementRequest query = pageRequest.getQuery();
        //请求参数
        String name = query.getName();
        String code = query.getCode();
        //获取到分页数据
        final PageBase page = pageRequest.getPage();
        QueryWrapper<RuleEngineElement> queryWrapper = new QueryWrapper<>();
        //从缓存中获取
        RuleEngineBizBean ruleEngineBizBean = RuleEngineBizServiceImpl.getEngineBiz();
        queryWrapper.lambda().eq(RuleEngineElement::getBizId, ruleEngineBizBean.getId())
                .eq(RuleEngineElement::getDeleted, ENABLE.getStatus());
        String[] valueType = query.getValueTypes();
        if (valueType != null) {
            List<String> valueTypes = Arrays.asList(valueType);
            if (!CollUtil.isEmpty(valueTypes)) {
                queryWrapper.lambda().in(RuleEngineElement::getValueType, valueTypes);
            }
        }
        if (Validator.isNotEmpty(name) && name.equals(code)) {
            //组件内模糊查询用的
            queryWrapper.lambda().like(RuleEngineElement::getCodeName, code);
        } else {
            if (Validator.isNotEmpty(name)) {
                queryWrapper.lambda().like(RuleEngineElement::getName, name);
            }
            if (Validator.isNotEmpty(code)) {
                queryWrapper.lambda().like(RuleEngineElement::getCode, code);
            }
        }
        if (query.getQueryType().equals(0)) {
            //查询公有条件
            queryWrapper.lambda().eq(RuleEngineElement::getShowed, DeletedEnum.ENABLE.getStatus());
        } else if (query.getQueryType().equals(1)) {
            queryWrapper.lambda()
                    .eq(RuleEngineElement::getShowed, DeletedEnum.ENABLE.getStatus())
                    .or(o -> {
                        LambdaQueryWrapper<RuleEngineElement> wrapper = o.eq(RuleEngineElement::getRuleSetId, query.getRuleSetId())
                                .eq(RuleEngineElement::getRuleSetType, query.getRuleSetType())
                                .eq(RuleEngineElement::getShowed, DeletedEnum.DISABLE.getStatus())
                                .eq(RuleEngineElement::getDeleted, ENABLE.getStatus());
                        if (valueType != null && CollUtil.isNotEmpty(Arrays.asList(valueType))) {
                            wrapper.in(RuleEngineElement::getValueType, Arrays.asList(valueType));
                        }
                        return wrapper.like(RuleEngineElement::getCodeName, name);
                    });
        } else if (query.getQueryType().equals(2)) {
            //查询私有的
            queryWrapper.lambda().eq(RuleEngineElement::getRuleSetId, query.getRuleSetId());
            queryWrapper.lambda().eq(RuleEngineElement::getRuleSetType, query.getRuleSetType());
            queryWrapper.lambda().eq(RuleEngineElement::getShowed, DeletedEnum.DISABLE.getStatus());
        }
        //排序
        PageUtils.defaultOrder(pageRequest.getOrders(), queryWrapper, RuleEngineElement::getId);
        IPage<RuleEngineElement> pageData = ruleEngineElementManager.page(new Page<>(page.getPageIndex(), page.getPageSize()), queryWrapper);
        List<RuleEngineElement> ruleEngineElements = pageData.getRecords();
        List<ElementResponse> elementResponses = new ArrayList<>();
        if (CollUtil.isNotEmpty(ruleEngineElements)) {
            //获取相关的循环策略信息
            elementResponses = ruleEngineElements.stream().map(e -> {
                ElementResponse elementResponse = new ElementResponse();
                elementResponse.setId(e.getId().intValue());
                elementResponse.setName(e.getName());
                elementResponse.setCode(e.getCode());
                elementResponse.setValueType(e.getValueType());
                elementResponse.setDescription(e.getDescription());
                elementResponse.setShowed(e.getShowed());
                return elementResponse;
            }).collect(Collectors.toList());
        }
        PageResult<ElementResponse> result = new PageResult<>();
        result.setData(new Rows<>(elementResponses, new PageResponse(page.getPageIndex(), page.getPageSize(), pageData.getTotal())));
        return result;
    }


    @Override
    public ElementResponse add(ElementAddRequest elementAddRequest) {
        String code = elementAddRequest.getCode();
        if (reserveWord.contains(code)) {
            throw new ValidException("此元素Code：{}为预留字段", code);
        }
        RuleEngineElement ruleEngineElement = new RuleEngineElement();
        PlainResult<Boolean> result = this.vaildateUniqCode(code);
        if (result.getData()) {
            throw new RuleEngineException(result.getMessage());
        }
        ruleEngineElement.setName(elementAddRequest.getName());
        ruleEngineElement.setCode(code);
        ruleEngineElement.setValueType(elementAddRequest.getValueType());
        ruleEngineElement.setDescription(elementAddRequest.getDescription());
        //从缓存中获取
        RuleEngineBizBean ruleEngineBizBean = RuleEngineBizServiceImpl.getEngineBiz();
        ruleEngineElement.setBizId(ruleEngineBizBean.getId().intValue());
        ruleEngineElement.setBizCode(ruleEngineBizBean.getBizCode());
        ruleEngineElement.setBizName(ruleEngineBizBean.getBizName());
        ruleEngineElement.setCodeName(code + elementAddRequest.getName());
        if (elementAddRequest.isNotNull()) {
            ruleEngineElement.setRuleSetId(elementAddRequest.getRuleSetId());
            ruleEngineElement.setRuleSetType(elementAddRequest.getRuleSetType());
            ruleEngineElement.setShowed(DeletedEnum.DISABLE.getStatus());
            //如果是决策表
            if (Objects.equals(elementAddRequest.getRuleSetType(), DataTypeEnum.DECISION.getDataType())) {
                ruleLockService.decisionValid(elementAddRequest.getRuleSetId());
            }
            //如果是规则表
            if (Objects.equals(elementAddRequest.getRuleSetType(), DataTypeEnum.RULESET.getDataType())) {
                ruleLockService.ruleSetValid(elementAddRequest.getRuleSetId());
            }
        } else {
            ruleEngineElement.setShowed(ENABLE.getStatus());
        }
        ruleEngineElementManager.save(ruleEngineElement);
        ElementResponse elementResponse = new ElementResponse();
        /*新建一个元素返回*/
        elementResponse.setCode(ruleEngineElement.getCode());
        elementResponse.setDescription(ruleEngineElement.getDescription());
        elementResponse.setId(ruleEngineElement.getId().intValue());
        elementResponse.setName(ruleEngineElement.getName());
        elementResponse.setValueType(ruleEngineElement.getValueType());
        if (elementAddRequest.isNotNull()) {
            elementResponse.setRuleSetId(ruleEngineElement.getRuleSetId());
            elementResponse.setRuleSetType(ruleEngineElement.getRuleSetType());
        }
        elementResponse.setShowed(ruleEngineElement.getShowed());
        return elementResponse;
    }


    @Override
    public ElementResponse get(Integer id) {
        RuleEngineElement ruleEngineElement = ruleEngineElementManager.getById(id.getId());
        if (Validator.isEmpty(ruleEngineElement)) {
            return null;
        }
        ElementResponse elementResponse = new ElementResponse();
        elementResponse.setId(ruleEngineElement.getId().intValue());
        elementResponse.setName(ruleEngineElement.getName());
        elementResponse.setValueType(ruleEngineElement.getValueType());
        elementResponse.setCode(ruleEngineElement.getCode());
        elementResponse.setDescription(ruleEngineElement.getDescription());
        elementResponse.setShowed(ruleEngineElement.getShowed());
        return elementResponse;
    }

    @Override
    public List<ElementResponse> getByIds(List<Integer> ids) {
        List<ElementResponse> result;
        if (CollUtil.isEmpty(ids)) {
            throw new RuntimeException("查询id不能为空");
        }
        LambdaQueryWrapper<RuleEngineElement> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.in(RuleEngineElement::getId, ids);
        queryWrapper.eq(RuleEngineElement::getDeleted, ENABLE.getStatus());
        List<RuleEngineElement> ruleEngineElements = ruleEngineElementManager.list(queryWrapper);
        result = ruleEngineElements.stream().map(e -> {
            ElementResponse elementResponse = new ElementResponse();
            elementResponse.setId(e.getId().intValue());
            elementResponse.setName(e.getName());
            elementResponse.setValueType(e.getValueType());
            elementResponse.setCode(e.getCode());
            elementResponse.setDescription(e.getDescription());
            elementResponse.setShowed(e.getShowed());
            return elementResponse;
        }).collect(Collectors.toList());
        return result;
    }



    /**
     * 根据rule_engine_rule 关联的delete的删除标识进行删除
     *
     * @param id
     * @return
     */
    @Override
    public Boolean delete(Integer elementId) {
        RuleEngineElement byId = ruleEngineElementManager.getById(elementId);
        if (byId == null) {
            throw new ValidationException("元素不存在");
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
        //逻辑删除(先判断条件是否启用该元素)
        LambdaQueryWrapper<RuleEngineCondition> isUsed = new LambdaQueryWrapper<>();
        isUsed.eq(RuleEngineCondition::getDeleted, ENABLE.getStatus())
                //只查询0 创建的条件  1的是在规则页面创建的，但是删除规则时没有删除条件，导致被统计到
                //.eq(RuleEngineCondition::getShowed, 0)
                .and(e -> e.eq(RuleEngineCondition::getLeftElementId, elementId)
                        .or()
                        .eq(RuleEngineCondition::getRightElementId, elementId));
        int count = ruleEngineConditionManager.count(isUsed);
        if (count > 0) {
            throw new RuleEngineException("该元素被使用中，不可删除！");
        }

        //查询应用此元素的的变量
        LambdaQueryWrapper<RuleEngineVariableParam> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(RuleEngineVariableParam::getFunctionParamElementId, elementId)
                .eq(RuleEngineVariableParam::getFunctionParamType, 2)
                .eq(RuleEngineVariableParam::getDeleted, ENABLE.getStatus());
        List<RuleEngineVariableParam> ruleEngineVariableParams = ruleEngineVariableParamManager.list(queryWrapper);
        if (CollUtil.isNotEmpty(ruleEngineVariableParams)) {
            //查询使用到此VariableParam的Variable
            Set<Long> variableIds = ruleEngineVariableParams.stream().map(RuleEngineVariableParam::getVariableId).collect(Collectors.toSet());
            List<RuleEngineVariable> list = ruleEngineVariableManager.list(new LambdaQueryWrapper<RuleEngineVariable>()
                    .in(RuleEngineVariable::getId, variableIds)
                    .eq(RuleEngineVariable::getDeleted, ENABLE.getStatus()));
            if (CollUtil.isNotEmpty(list)) {
                throw new RuleEngineException("该元素被使用中，不可删除！");
            }

        }
        CountVo countVo = countElement(Long.valueOf(elementId));
        if (countVo.getCount() > 0) {
            throw new RuleEngineException("该元素被使用中，不可删除！");
        }
        RuleEngineElement ruleEngineElement = new RuleEngineElement();
        ruleEngineElement.setDeleted(DeletedEnum.DISABLE.getStatus());
        LambdaQueryWrapper<RuleEngineElement> deleteWrapper = new LambdaQueryWrapper<>();
        //从缓存中获取
        RuleEngineBizBean ruleEngineBizBean = RuleEngineBizServiceImpl.getEngineBiz();
        deleteWrapper.eq(RuleEngineElement::getId, elementId).eq(RuleEngineElement::getBizId, ruleEngineBizBean.getId());
        return ruleEngineElementManager.update(ruleEngineElement, deleteWrapper);
    }

    @Override
    public Boolean update(ElementUpdateRequest elementUpdateRequest) {
        RuleEngineElement ruleEngineElement = new RuleEngineElement();
        ruleEngineElement.setId(Long.valueOf(elementUpdateRequest.getId()));
        ruleEngineElement.setName(elementUpdateRequest.getName());
        ruleEngineElement.setDescription(elementUpdateRequest.getDescription());
        RuleEngineElement byId = ruleEngineElementManager.getById(elementUpdateRequest.getId());
        //规则/决策内多人编辑验证
        //如果是决策表
        if (Objects.equals(byId.getRuleSetType(), DataTypeEnum.DECISION.getDataType())) {
            ruleLockService.decisionValid(byId.getRuleSetId());
        }
        //如果是规则表
        if (Objects.equals(byId.getRuleSetType(), DataTypeEnum.RULESET.getDataType())) {
            RuleEngineRuleSet ruleEngineRuleSet = ruleEngineRuleSetManager.getById(byId.getRuleSetId());
            ruleLockService.ruleSetValid(byId.getRuleSetId());
        }
        // 查询到code+新传的name
        ruleEngineElement.setCodeName(byId.getCode() + elementUpdateRequest.getName());
        boolean updateById = ruleEngineElementManager.updateById(ruleEngineElement);
        DB.invalidate(RuleEngineElement.class, byId.getId());
        return updateById;
    }

    /**
     * 判断启用中的元素是否重复
     *
     * @param code
     * @return
     */
    @Override
    public PlainResult<Boolean> vaildateUniqCode(String code) {
        PlainResult<Boolean> result = new PlainResult<>();
        if (Validator.isEmpty(code)) {
            result.setData(true);
            result.setMessage("Code不能为空");
            return result;
        }
        /*
         *8.26前段要求修改为...
         */
        Pattern compile = compile("^[0-9a-zA-Z_]+$");
        Matcher matcher = compile.matcher(code);
        if (!matcher.matches()) {
            result.setData(true);
            result.setMessage("Code只能由英文数字下划线组成。");
            return result;
        }
        //获取业务组信息
        RuleEngineBizBean bizInfo = RuleEngineBizServiceImpl.getEngineBiz();
        LambdaQueryWrapper<RuleEngineElement> lambdaQueryWrapper = new LambdaQueryWrapper<>();
        lambdaQueryWrapper.eq(RuleEngineElement::getDeleted, ENABLE.getStatus()).eq(RuleEngineElement::getCode, code).eq(RuleEngineElement::getBizId, bizInfo.getId());
        int count = ruleEngineElementManager.count(lambdaQueryWrapper);
        if (count > 0) {
            result.setData(true);
            result.setMessage("该元素code已存在,请重新输入。");
        } else {
            result.setData(false);
        }
        return result;
    }


}
