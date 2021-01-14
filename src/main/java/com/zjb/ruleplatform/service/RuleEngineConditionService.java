package com.zjb.ruleplatform.service;

import com.founder.ego.common.request.PageRequest;
import com.founder.ego.common.response.PageResult;
import com.founder.ego.common.response.PlainResult;
import com.founder.ego.store.bpm.entity.RuleEngineCondition;
import com.founder.ego.vo.ruleengine.*;

import java.util.List;

/**
 * @author v-dingqianwen.ea
 */
public interface RuleEngineConditionService {
    /**
     * 分页查询条件
     *
     * @return lis
     */
    PageResult<RuleEngineConditionResponse> list(PageRequest<RuleEngineConditionRequest> pageRequest);

    /**
     * 删除条件
     *
     * @param id 条件id
     * @return true表示删除成功
     */
    Boolean delete(Integer id);

    /**
     * 添加条件
     *
     * @param add 条件参数
     * @return true表示添加成功
     */
    Boolean add(AddRuleEngineConditionParam add);

    /**
     * 根据id查询
     *
     * @param id 条件id
     * @return data
     */
    GetRuleEngineConditionResponse get(Long id);

    /**
     * 更新条件
     *
     * @param update 条件信息
     * @return 返回更新后的数据
     */
    GetRuleEngineConditionResponse update(UpdateRuleEngineBizRequest update);

    /**
     * 条件验重接口
     *
     * @param name 条件名称
     * @return true时条件已经存在
     */
    PlainResult<Boolean> validateUniqName(String name);

    /**
     * 生成条件值
     *
     * @param condition 条件信息
     * @param add       AddRuleEngineConditionParam
     */
    void generateConditionValue(RuleEngineCondition condition, AddRuleEngineConditionParam add);


    /**
     * 条件被多少规则引用
     *
     * @return 返回被规则引用的数量
     */
    Integer useCount(Integer id);

    /**
     * 根据Ids批量查询条件
     *
     * @param ids 条件id
     * @return data
     */
    List<IdAndName> getByIds(List<Integer> ids);
}
