///**
// * *****************************************************
// * Copyright (C) 2019 zjb.com. All Rights Reserved
// * This file is part of zjb zjb project.
// * Unauthorized copy of this file, via any medium is strictly prohibited.
// * Proprietary and Confidential.
// * ****************************************************
// * <p>
// * History:
// * <author>            <time>          <version>          <desc>
// * 作者姓名           修改时间           版本号            描述
// */
//package com.zjb.ruleplatform.service.impl;
//
//import cn.hutool.core.collection.CollUtil;
//import cn.hutool.core.date.DateUtil;
//import cn.hutool.core.lang.Validator;
//import cn.hutool.crypto.SecureUtil;
//import cn.hutool.http.HttpUtil;
//import com.alibaba.fastjson.JSON;
//import com.alibaba.fastjson.JSONObject;
//import com.founder.ego.enumbean.DeletedEnum;
//import com.founder.ego.enumbean.PublishEnum;
//import com.founder.ego.exception.ValidException;
//import com.founder.ego.service.ruleengine.RuleEngineMultipleEnvExecutor;
//import com.founder.ego.store.bpm.entity.*;
//import com.founder.ego.store.bpm.manager.*;
//import com.founder.ego.vo.ruleengine.*;
//import com.founder.ego.vo.ruleengine.RuleEngineParam;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.beans.factory.annotation.Value;
//import org.springframework.context.annotation.Primary;
//import org.springframework.stereotype.Service;
//import org.springframework.transaction.annotation.Transactional;
//
//import javax.annotation.Resource;
//import javax.validation.ValidationException;
//import java.util.*;
//import java.util.stream.Collectors;
//
//import static com.founder.ego.enumbean.DeletedEnum.ENABLE;
//
///**
// * 〈一句话功能简述〉<br>
// * 〈〉
// *
// * @author v-dingqianwen.ea
// * @create 2019/11/14
// * @since 1.0.0
// */
//@Slf4j
//@Service
//@Primary
//public class RuleEngineRuleMultipleEnvExecutor implements RuleEngineMultipleEnvExecutor {
//
//    @Value("${wfc-rule.app.appId}")
//    private String appId;
//    @Value("${wfc-rule.app.secret}")
//    private String appSecret;
//    @Value("${wfc-rule.run.environment}")
//    protected String runEnvironment;
//
//    @Resource
//    private RuleEngineEnviromentPublishHistoryManager ruleEngineEnviromentPublishHistoryManager;
//    @Resource
//    private RuleEngineEnviromentManager ruleEngineEnviromentManager;
//    @Resource
//    private RuleEngineRuleSetTestCaseManager ruleEngineRuleSetTestCaseManager;
//    @Resource
//    private RuleEngineRuleSetJsonManager ruleSetJsonManager;
//    @Resource
//    private RuleEngineBizManager bizManager;
//
//    /**
//     * 判断规则版本在某个环境是否已经发布
//     *
//     * @param ruleSetId   规则id
//     * @param version     版本
//     * @param environment 环境
//     * @return true 已经发布
//     */
//    @Override
//    public boolean isEnvPublish(Integer ruleSetId, String version, String environment) {
//        RuleEngineEnviromentPublishHistory one = ruleEngineEnviromentPublishHistoryManager.lambdaQuery().eq(RuleEngineEnviromentPublishHistory::getRuleSetId, ruleSetId)
//                .eq(RuleEngineEnviromentPublishHistory::getRuleSetVersion, version)
//                .eq(RuleEngineEnviromentPublishHistory::getEnviromentName, environment)
//                .eq(RuleEngineEnviromentPublishHistory::getDeleted, DeletedEnum.ENABLE.getStatus()).one();
//        return one != null;
//    }
//
//    /**
//     * 规则发布
//     *
//     * @param ruleEnginePublishParam 发布所使用的参数/json数据
//     */
//    @Override
//    public void publish(RuleEnginePublishParam ruleEnginePublishParam) {
//        //发布到哪个环境
//        String environment = ruleEnginePublishParam.getEnvironment();
//        RuleEngineEnviroment ruleEngineEnviroment = ruleEngineEnviromentManager.lambdaQuery().eq(RuleEngineEnviroment::getName, environment)
//                .eq(RuleEngineEnviroment::getDeleted, DeletedEnum.ENABLE.getStatus()).one();
//        if (ruleEngineEnviroment == null) {
//            throw new ValidationException(String.format("不存在的发布环境%s", environment));
//        }
//        ruleEnginePublishParam.setIsCurrentEnv(isCurrentEnvProcess(runEnvironment, environment));
//        ruleEnginePublishParam.setPriority(ruleEngineEnviroment.getPriority());
//        String loadRuleSetUrl = ruleEngineEnviroment.getLoadRuleSetUrl();
//        String bizParams = JSONObject.toJSONString(ruleEnginePublishParam, getSerializeConfig());
//        //加签名
//        //String jsonString = getSignAndParam(bizParams, appId, appSecret);
//        log.info("规则发布-pre,url:{},参数:{}", loadRuleSetUrl, bizParams);
//        String post = HttpUtil.post(loadRuleSetUrl, bizParams);
//        log.info("规则发布-after,返回:{}", post);
//        JSONObject resultJson = JSON.parseObject(post);
//        boolean boo = resultJson.containsKey("code") && Objects.equals(resultJson.getString("code"), "000000")
//                && resultJson.containsKey("data") && resultJson.getBoolean("data");
//        if (!boo) {
//            throw new ValidationException("发布失败," + resultJson.getString("message"));
//        }
//    }
//
//    /**
//     * 重新发布规则，可能存在多个
//     * bug修复，基础组件变更升级小版本，需要存储发布记录
//     *
//     * @param ruleEngineParams ruleEngineParams
//     * @return true
//     */
//    @Override
//    @Transactional(rollbackFor = Exception.class)
//    public boolean rePublish(List<RuleEngineParam> ruleEngineParams) {
//        if (CollUtil.isEmpty(ruleEngineParams)) {
//            return true;
//        }
//        //查询到已发布的版本环境
//        Map<String, List<RuleEngineParam>> envRuleSet = new HashMap<>();
//        for (RuleEngineParam ruleEngineParam : ruleEngineParams) {
//            //已发布版本在这个表中是一定存在的
//            List<RuleEngineEnviromentPublishHistory> list = ruleEngineEnviromentPublishHistoryManager.lambdaQuery()
//                    .eq(RuleEngineEnviromentPublishHistory::getRuleSetId, ruleEngineParam.getRuleSetId())
//                    //根据升级前的版本查询
//                    .eq(RuleEngineEnviromentPublishHistory::getRuleSetVersion, ruleEngineParam.getRuleOldVersion())
//                    .eq(RuleEngineEnviromentPublishHistory::getDeleted, DeletedEnum.ENABLE.getStatus()).list();
//            //规则id=1 版本为1.0 同时发布到test/pre/prd 则都需要更新
//            for (RuleEngineEnviromentPublishHistory ruleEngineEnviromentPublishHistory : list) {
//                List<RuleEngineParam> params = envRuleSet.getOrDefault(ruleEngineEnviromentPublishHistory.getEnviromentName(), new ArrayList<>());
//                params.add(ruleEngineParam);
//                envRuleSet.put(ruleEngineEnviromentPublishHistory.getEnviromentName(), params);
//            }
//        }
//        if (CollUtil.isEmpty(envRuleSet)) {
//            throw new ValidationException("没有找到发布记录");
//        }
//        HashSet<Long> removeRuleIdSet = new HashSet<>();
//        //所有用到的环境
//        Set<String> env = envRuleSet.keySet();
//        Map<String, RuleEngineEnviroment> environmentsMap = ruleEngineEnviromentManager.lambdaQuery()
//                .in(RuleEngineEnviroment::getName, env).list()
//                .stream().collect(Collectors.toMap(RuleEngineEnviroment::getName, e -> e));
//        for (Map.Entry<String, List<RuleEngineParam>> entry : envRuleSet.entrySet()) {
//            RuleEnginePublishParam ruleEnginePublishParam = new RuleEnginePublishParam();
//            ruleEnginePublishParam.setRuleEngineParamList(entry.getValue());
//            ruleEnginePublishParam.setEnvironment(entry.getKey());
//            ruleEnginePublishParam.setIsCurrentEnv(isCurrentEnvProcess(runEnvironment, entry.getKey()));
//            ruleEnginePublishParam.setPriority(environmentsMap.get(entry.getKey()).getPriority());
//            //自己环境往自己环境发，不保留升级前的版本
//            for (RuleEngineParam ruleEngineParam : entry.getValue()) {
//                if (runEnvironment.equals(entry.getKey())) {
//                    removeRuleIdSet.add(ruleEngineParam.getId());
//                }
//            }
//            String loadRuleSetUrl = environmentsMap.get(entry.getKey()).getLoadRuleSetUrl();
//            String bizParams = JSONObject.toJSONString(ruleEnginePublishParam, getSerializeConfig());
//            //加签名
//            //String jsonString = getSignAndParam(bizParams, appId, appSecret);
//            log.info("重新发布-pre,url:{},参数:{}", loadRuleSetUrl, bizParams);
//            String post = HttpUtil.post(loadRuleSetUrl, bizParams);
//            log.info("重新发布-after,返回:{}", post);
//            JSONObject resultJson = JSON.parseObject(post);
//            if (resultJson.containsKey("code") && Objects.equals(resultJson.getString("code"), "000000")
//                    && resultJson.containsKey("data") && !resultJson.getBoolean("data")) {
//                throw new ValidationException("执行失败," + resultJson.getString("message"));
//            }
//            //发布记录
//            List<RuleEngineParam> entryValue = entry.getValue();
//            List<RuleEngineEnviromentPublishHistory> collect = entryValue.stream().map(m -> {
//                RuleEngineEnviromentPublishHistory history = new RuleEngineEnviromentPublishHistory();
//                history.setRuleSetId(m.getRuleSetId());
//                history.setPriority(environmentsMap.get(entry.getKey()).getPriority());
//                history.setRuleSetCode(m.getRuleSetCode());
//                history.setRuleSetVersion(m.getRuleVersion());
//                history.setEnviromentName(entry.getKey());
//                history.setDeleted(ENABLE.getStatus());
//                return history;
//            }).collect(Collectors.toList());
//            //存发布记录
//            ruleEngineEnviromentPublishHistoryManager.saveBatch(collect);
//        }
//        if (CollUtil.isNotEmpty(removeRuleIdSet)) {
//            //如果当前环境是prd
//            if ("prd".equals(runEnvironment)) {
//                ruleSetJsonManager.lambdaUpdate()
//                        .in(RuleEngineRuleSetJson::getId, removeRuleIdSet)
//                        .set(RuleEngineRuleSetJson::getPublished, PublishEnum.PUBLISH.getType())
//                        .update();
//            } else {
//                //pre /test 依然删除
//                ruleSetJsonManager.removeByIds(removeRuleIdSet);
//            }
//        }
//        return true;
//    }
//
//    /**
//     * 删除的规则id
//     *
//     * @param removeRuleInfo removeRuleInfo
//     * @return true 成功
//     */
//    @Override
//    @Transactional(rollbackFor = Exception.class)
//    public boolean removeRule(RemoveRuleInfo removeRuleInfo) {
//        log.info("开始删除已发环境对应的规则");
//        //删除已发环境对应的规则
//        List<RuleEngineEnviromentPublishHistory> ruleEngineEnviromentPublishHistories = ruleEngineEnviromentPublishHistoryManager.lambdaQuery()
//                .eq(RuleEngineEnviromentPublishHistory::getRuleSetId, removeRuleInfo.getRuleId())
//                .eq(RuleEngineEnviromentPublishHistory::getDeleted, ENABLE.getStatus()).list();
//        if (CollUtil.isEmpty(ruleEngineEnviromentPublishHistories)) {
//            return true;
//        }
//        //规则已经发布的环境
//        Set<String> environmentName = ruleEngineEnviromentPublishHistories.stream().map(RuleEngineEnviromentPublishHistory::getEnviromentName).collect(Collectors.toSet());
//        List<RuleEngineEnviroment> list = ruleEngineEnviromentManager.lambdaQuery().in(RuleEngineEnviroment::getName, environmentName).list();
//        //调用删除接口
//        for (RuleEngineEnviroment ruleEngineEnviroment : list) {
//            //删除规则
//            String bizParams = JSONObject.toJSONString(removeRuleInfo, getSerializeConfig());
//            //加签名
//            //String jsonString = getSignAndParam(bizParams, appId, appSecret);
//            log.info("删除规则-pre,url:{},参数:{}", ruleEngineEnviroment.getRemoveRuleSetUrl(), bizParams);
//            String post = HttpUtil.post(ruleEngineEnviroment.getRemoveRuleSetUrl(), bizParams);
//            log.info("删除规则-after,返回:{}", post);
//            JSONObject resultJson = JSON.parseObject(post);
//            if (resultJson.containsKey("code") && Objects.equals(resultJson.getString("code"), "000000")
//                    && resultJson.containsKey("data") && !resultJson.getBoolean("data")) {
//                throw new ValidationException("删除规则失败," + resultJson.getString("message"));
//            }
//            //如果getRemoveMockRuleSetUrl不为空 删除mock
//            if (Validator.isNotEmpty(ruleEngineEnviroment.getRemoveMockRuleSetUrl())) {
//                log.info("删除Mock规则-pre,url:{},参数:{}", ruleEngineEnviroment.getRemoveMockRuleSetUrl(), bizParams);
//                String removeRuleSetMockResultJson = HttpUtil.post(ruleEngineEnviroment.getRemoveMockRuleSetUrl(), bizParams);
//                log.info("删除Mock规则-after,返回:{}", removeRuleSetMockResultJson);
//                JSONObject removeRuleSetMockResult = JSON.parseObject(removeRuleSetMockResultJson);
//                if (removeRuleSetMockResult.containsKey("code") && Objects.equals(removeRuleSetMockResult.getString("code"), "000000")
//                        && removeRuleSetMockResult.containsKey("data") && !removeRuleSetMockResult.getBoolean("data")) {
//                    throw new ValidationException("删除Mock规则失败," + removeRuleSetMockResult.getString("message"));
//                }
//            }
//        }
//        //删除此规则的所有测试用例
//        ruleEngineRuleSetTestCaseManager.lambdaUpdate().eq(RuleEngineRuleSetTestCase::getRuleSetId, removeRuleInfo.getRuleId()).remove();
//        //删除此规则的发布记录
//        return ruleEngineEnviromentPublishHistoryManager.lambdaUpdate().eq(RuleEngineEnviromentPublishHistory::getRuleSetId, removeRuleInfo.getRuleId()).remove();
//    }
//
//    /**
//     * 模拟运行
//     *
//     * @return RuleEngineResultStatus
//     */
//    @Override
//    public RuleEngineResultStatus runRule(RuleEngineCaseAddRequest ruleEngineCaseAddRequest) {
//        String env = ruleEngineCaseAddRequest.getEnv();
//        RuleEngineEnviroment ruleEngineEnviroment = ruleEngineEnviromentManager.lambdaQuery()
//                .eq(RuleEngineEnviroment::getName, env)
//                .eq(RuleEngineEnviroment::getDeleted, DeletedEnum.ENABLE.getStatus()).one();
//        if (ruleEngineEnviroment == null) {
//            throw new ValidException("找不到运行环境:{}", env);
//        }
//        Integer bizId = ruleEngineCaseAddRequest.getBizId();
//        //存在bug  都改为默认的签名
//        RuleEngineBiz engineBiz = new RuleEngineBiz();//bizManager.getById(bizId);
//        String bizParams = JSONObject.toJSONString(ruleEngineCaseAddRequest, getSerializeConfig());
//        //r
//        Map<String, Object> map = new HashMap<>();
//        String time = DateUtil.format(new Date(), "yyyyMMddHHmmss");
//        map.put("app_id", getOrElse(engineBiz.getAppId(), appId));
//        map.put("timestamp", time);
//        map.put("biz_params", bizParams);
//        String sign = SecureUtil.md5(getOrElse(engineBiz.getAppSecret(), appSecret) + time + bizParams);
//        map.put("sign", sign);
//        //
//        String jsonString = JSON.toJSONString(map);
//        log.info("模拟运行-pre,url:{},参数:{}", ruleEngineEnviroment.getRunRuleSetUrl(), jsonString);
//        String post = HttpUtil.post(ruleEngineEnviroment.getRunRuleSetUrl(), jsonString);
//        log.info("模拟运行-after,返回:{}", post);
//        JSONObject resultJson = JSON.parseObject(post);
//        if (resultJson.containsKey("code") && Objects.equals(resultJson.getString("code"), "000000")
//                && resultJson.containsKey("data")) {
//            return resultJson.getJSONObject("data").toJavaObject(RuleEngineResultStatus.class);
//        }
//        throw new ValidationException("模拟运行失败," + resultJson.getString("message"));
//    }
//
//}