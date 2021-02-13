//package com.zjb.ruleplatform.service.impl;
//
//import cn.hutool.core.text.StrFormatter;
//import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
//import com.baomidou.mybatisplus.core.toolkit.StringPool;
//import com.baomidou.mybatisplus.extension.service.additional.query.impl.LambdaQueryChainWrapper;
//import com.zjb.ruleplatform.entity.RuleEngineRuleSet;
//import com.zjb.ruleplatform.entity.RuleEngineRuleSetJson;
//import com.zjb.ruleplatform.manager.RuleEngineRuleSetJsonManager;
//import com.zjb.ruleplatform.manager.RuleEngineRuleSetManager;
//import com.zjb.ruleplatform.service.RuleEnginePublishService;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.beans.factory.annotation.Value;
//import org.springframework.context.annotation.Primary;
//import org.springframework.stereotype.Service;
//import org.springframework.transaction.annotation.Transactional;
//
//import javax.annotation.Resource;
//import javax.validation.ValidationException;
//import java.util.Arrays;
//import java.util.Collections;
//import java.util.List;
//
///**
// * 规则引擎加载规则集
// *
// * @author v-lixing.ea
// */
//@Primary
//@Service
//@Slf4j
//public class RuleEnginePublishServiceImpl implements RuleEnginePublishService {
//
//    //@Resource
//    //private RuleEngineRuleSetManager ruleSetManager;
//    //@Resource
//    //private RuleEngineRuleSetJsonManager ruleSetJsonManager;
//    //@Resource
//    //private RuleEngineEnviromentPublishHistoryManager ruleEngineEnviromentPublishHistoryManager;
//    //@Resource
//    //private RuleEngineRuleMultipleEnvExecutor ruleEngineRuleMultipleEnvExecutor;
//    //@Value("${wfc-rule.run.environment}")
//    //protected String runEnvironment;
//    //@Resource
//    //private RuleLockService ruleLockService;
//    //
//    ///**
//    // * 规则发布
//    // *
//    // * @param rulePublishRequest 规则id/版本/环境信息
//    // * @return true
//    // */
//    //@Override
//    //@Transactional(rollbackFor = Exception.class)
//    //public Boolean publish(RulePublishRequest rulePublishRequest) {
//    //    Integer ruleSetId = rulePublishRequest.getId();
//    //    //发布前校验
//    //    ruleLockService.ruleSetValid(ruleSetId);
//    //    String environment = rulePublishRequest.getEnvironment();
//    //    //获取到最新版本的数据
//    //    LambdaQueryWrapper<RuleEngineRuleSetJson> queryWrapper = new LambdaQueryWrapper<RuleEngineRuleSetJson>()
//    //            .eq(RuleEngineRuleSetJson::getRuleSetId, ruleSetId)
//    //            .eq(RuleEngineRuleSetJson::getIsCurEnv, RuleEnvironmentEnum.CURRENT_ENV.getValue())
//    //            .eq(RuleEngineRuleSetJson::getDeleted, DeletedEnum.ENABLE.getStatus())
//    //            .in(RuleEngineRuleSetJson::getPublished, Arrays.asList(PublishEnum.WAITING_PUBLISH.getType(), PublishEnum.PUBLISH.getType(), PublishEnum.PUBLISH_OTHER_ENV.getType()));
//    //    List<RuleEngineRuleSetJson> ruleEngineRuleSetJsons = ruleSetJsonManager.list(queryWrapper);
//    //    RuleEngineRuleSetJson json = RuleEngineLoadServiceImpl.getRuleEngineRuleSetJsonNew(ruleEngineRuleSetJsons);
//    //    if (json == null) {
//    //        throw new ValidationException(StrFormatter.format("该规则{}没有可发布JSON数据", ruleSetId));
//    //    }
//    //    //先判断此环境是否已经发布过
//    //    boolean envPublish = ruleEngineRuleMultipleEnvExecutor.isEnvPublish(ruleSetId, json.getRuleVersion(), environment);
//    //    if (envPublish) {
//    //        throw new ValidationException(String.format("该规则版本在%s环境已经发布过", environment));
//    //    }
//    //    RuleEnginePublishParam ruleEnginePublishParam = new RuleEnginePublishParam();
//    //    ruleEnginePublishParam.setEnvironment(environment);
//    //    RuleEngineParam ruleEngineParam = new RuleEngineParam();
//    //    ruleEngineParam.setRuleSetJson(json.getRuleSetJson());
//    //    ruleEngineParam.setRuleSetId(json.getRuleSetId());
//    //    ruleEngineParam.setBizId(json.getBizId());
//    //    ruleEngineParam.setBizCode(json.getBizCode());
//    //    ruleEngineParam.setBizName(json.getBizName());
//    //    ruleEngineParam.setRuleSetCode(json.getRuleSetCode());
//    //    ruleEngineParam.setRuleSetName(json.getRuleSetName());
//    //    ruleEngineParam.setRuleVersion(json.getRuleVersion());
//    //    ruleEngineParam.setCountInfo(json.getCountInfo());
//    //    ruleEnginePublishParam.setRuleEngineParamList(Collections.singletonList(ruleEngineParam));
//    //    ruleEngineRuleMultipleEnvExecutor.publish(ruleEnginePublishParam);
//    //    //更新rule set 状态
//    //    RuleEngineRuleSet ruleEngineRuleSet = new RuleEngineRuleSet()
//    //            .setId(Long.valueOf(ruleSetId))
//    //            .setCreateStatus(PublishEnum.PUBLISH.getType())
//    //            .setPublishVersion(json.getRuleVersion())
//    //            .setPreparedVersion(StringPool.EMPTY);
//    //    //如果发布到自己环境，需要删除json
//    //    if (runEnvironment.equals(environment)) {
//    //        //发布到自己环境 老的当前环境已发布变成历史
//    //        ruleSetJsonManager.lambdaUpdate()
//    //                .eq(RuleEngineRuleSetJson::getRuleSetId, ruleEngineParam.getRuleSetId())
//    //                .eq(RuleEngineRuleSetJson::getPublished, PublishEnum.PUBLISH.getType())
//    //                .eq(RuleEngineRuleSetJson::getIsCurEnv, RuleEnvironmentEnum.CURRENT_ENV.getValue())
//    //                .ne(RuleEngineRuleSetJson::getRuleVersion, json.getRuleVersion())
//    //                .ne(RuleEngineRuleSetJson::getId, json.getId())
//    //                .set(RuleEngineRuleSetJson::getPublished, PublishEnum.HISTORY.getType())
//    //                .update();
//    //        //prd bug,不需要删除
//    //        if ("prd".equals(runEnvironment)) {
//    //            RuleEngineRuleSetJson ruleEngineRuleSetJson = new RuleEngineRuleSetJson();
//    //            ruleEngineRuleSetJson.setId(json.getId());
//    //            ruleEngineRuleSetJson.setEnvironment(runEnvironment);
//    //            ruleEngineRuleSetJson.setPublished(PublishEnum.PUBLISH.getType());
//    //            ruleSetJsonManager.updateById(ruleEngineRuleSetJson);
//    //        } else {
//    //            ruleSetJsonManager.removeById(json.getId());
//    //        }
//    //    } else {
//    //        //如果本环境已经发布过，不需要执行下面
//    //        LambdaQueryChainWrapper<RuleEngineRuleSetJson> ruleSetJsonLambdaQueryChainWrapper = ruleSetJsonManager.lambdaQuery()
//    //                .eq(RuleEngineRuleSetJson::getRuleSetId, json.getRuleSetId())
//    //                .eq(RuleEngineRuleSetJson::getIsCurEnv, RuleEnvironmentEnum.CURRENT_ENV.getValue())
//    //                .eq(RuleEngineRuleSetJson::getPublished, PublishEnum.PUBLISH.getType())
//    //                .eq(RuleEngineRuleSetJson::getRuleVersion, json.getRuleVersion());
//    //        //prd 不需要此条件
//    //        if (!"prd".equals(runEnvironment)) {
//    //            ruleSetJsonLambdaQueryChainWrapper.eq(RuleEngineRuleSetJson::getEnvironment, runEnvironment);
//    //        }
//    //        Integer count = ruleSetJsonLambdaQueryChainWrapper.eq(RuleEngineRuleSetJson::getDeleted, DeletedEnum.ENABLE.getStatus()).count();
//    //        //如果没有查询到数据，自己环境没有发布过，那么此次往别的环境发状态为PUBLISH_OTHER_ENV
//    //        if (count == 0) {
//    //            //发布到其他环境
//    //            RuleEngineRuleSetJson ruleEngineRuleSetJson = new RuleEngineRuleSetJson();
//    //            ruleEngineRuleSetJson.setId(json.getId());
//    //            ruleEngineRuleSetJson.setPublished(PublishEnum.PUBLISH_OTHER_ENV.getType());
//    //            ruleSetJsonManager.updateById(ruleEngineRuleSetJson);
//    //            ruleEngineRuleSet.setCreateStatus(PublishEnum.PUBLISH_OTHER_ENV.getType());
//    //        }
//    //        //老的发布到其他环境的5变成历史
//    //        ruleSetJsonManager.lambdaUpdate()
//    //                .eq(RuleEngineRuleSetJson::getRuleSetId, json.getRuleSetId())
//    //                .eq(RuleEngineRuleSetJson::getIsCurEnv, RuleEnvironmentEnum.CURRENT_ENV.getValue())
//    //                .eq(RuleEngineRuleSetJson::getPublished, PublishEnum.PUBLISH_OTHER_ENV.getType())
//    //                .ne(RuleEngineRuleSetJson::getRuleVersion, json.getRuleVersion())
//    //                .ne(RuleEngineRuleSetJson::getId, json.getId())
//    //                .set(RuleEngineRuleSetJson::getPublished, PublishEnum.HISTORY.getType())
//    //                .update();
//    //    }
//    //    ruleSetManager.updateById(ruleEngineRuleSet);
//    //    //存储发布记录
//    //    RuleEngineEnviromentPublishHistory ruleEngineEnviromentPublishHistory = new RuleEngineEnviromentPublishHistory();
//    //    ruleEngineEnviromentPublishHistory.setRuleSetId(json.getRuleSetId());
//    //    ruleEngineEnviromentPublishHistory.setPriority(ruleEnginePublishParam.getPriority());
//    //    ruleEngineEnviromentPublishHistory.setRuleSetCode(json.getRuleSetCode());
//    //    ruleEngineEnviromentPublishHistory.setRuleSetVersion(json.getRuleVersion());
//    //    ruleEngineEnviromentPublishHistory.setEnviromentName(environment);
//    //    ruleEngineEnviromentPublishHistory.setDeleted(DeletedEnum.ENABLE.getStatus());
//    //    ruleEngineEnviromentPublishHistoryManager.save(ruleEngineEnviromentPublishHistory);
//    //    return true;
//    //}
//
//}
