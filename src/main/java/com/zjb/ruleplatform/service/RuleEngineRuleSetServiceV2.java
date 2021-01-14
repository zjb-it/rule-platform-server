package com.zjb.ruleplatform.service; /**
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

import com.alibaba.fastjson.JSONObject;
import com.founder.ego.vo.EnvVersion;
import com.founder.ego.vo.IdRequest;
import com.founder.ego.vo.ruleengine.RuleSetCancelRequest;
import com.founder.ego.vo.ruleengine.RuleSetViewRequest;

import java.util.List;

/**
 * 〈一句话功能简述〉<br>
 * 〈〉
 *
 * @author v-dingqianwen.ea
 * @create 2019/9/29
 * @since 1.0.0
 */
public interface RuleEngineRuleSetServiceV2 {
    /**
     * 规则预览页面-规则撤销
     *
     * @param ruleSetCancelRequest ruleSetCancelRequest
     * @return true时撤销成功
     */
    Boolean cancel(RuleSetCancelRequest ruleSetCancelRequest);

    /**
     * 规则预览页面-规则展示
     *
     * @param ruleSetViewRequest ruleSetViewRequest
     * @return 规则
     */
    JSONObject view(RuleSetViewRequest ruleSetViewRequest);

    /**
     *规则预览页面获取版本接口
     */
    List<EnvVersion> envVersions(IdRequest id);

}