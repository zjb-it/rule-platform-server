package com.zjb.ruleplatform.mapper;

import com.zjb.ruleplatform.entity.vo.RuleDetail;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;


@SpringBootTest
public class CustomRuleMapperTest {

    @Autowired
    private CustomRuleMapper ruleMapper;

    @Test
    void getRule() {
        final RuleDetail rule = ruleMapper.getRule(29744L);
        System.out.println();
    }
}