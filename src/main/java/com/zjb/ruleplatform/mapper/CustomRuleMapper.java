package com.zjb.ruleplatform.mapper;

import com.zjb.ruleplatform.entity.vo.RuleDetail;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * @author 赵静波
 * @date 2021-02-07 16:00:20
 */
public interface CustomRuleMapper {

    RuleDetail getRule(@Param("id") Long id);

    List<RuleDetail> listRules();
}
