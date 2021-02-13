package com.zjb.ruleplatform.service;


import com.zjb.ruleplatform.entity.RuleEngineCondition;
import com.zjb.ruleplatform.entity.common.PageRequest;
import com.zjb.ruleplatform.entity.common.PageResult;
import com.zjb.ruleplatform.entity.dto.ConditionParam;
import com.zjb.ruleplatform.entity.dto.IdAndName;
import com.zjb.ruleplatform.entity.dto.RuleEngineConditionResponse;

import java.util.List;

/**
 * @author v-dingqianwen.ea
 */
public interface ConditionService {
    /**
     * 分页查询条件
     *
     * @return lis
     */
    PageResult<RuleEngineConditionResponse> list(PageRequest<String> pageRequest);



    /**
     * 添加条件
     *
     * @param add 条件参数
     * @return true表示添加成功
     */
    Boolean add(ConditionParam add);

    /**
     * 根据id查询
     *
     * @param id 条件id
     * @return data
     */
    ConditionParam get(Long id);

    /**
     * 更新条件
     *
     * @param update 条件信息
     * @return 返回更新后的数据
     */
    ConditionParam update(ConditionParam update);



    /**
     * 生成条件值
     *
     * @param condition 条件信息
     * @param add       AddRuleEngineConditionParam
     */
    void generateConditionValue(RuleEngineCondition condition, ConditionParam add);



    /**
     * 根据Ids批量查询条件
     *
     * @param ids 条件id
     * @return data
     */
    List<IdAndName> getByIds(List<Integer> ids);
}
