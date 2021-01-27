package com.zjb.ruleplatform.service;


import com.zjb.ruleplatform.entity.common.PageRequest;
import com.zjb.ruleplatform.entity.common.PageResult;
import com.zjb.ruleplatform.entity.dto.AddHttpFunction;
import com.zjb.ruleplatform.entity.vo.FunctionDetailVo;

/**
 * @author yuzhiji
 */
public interface RuleEngineFunctionService {
    /**
     * 查找 function
     *
     * @param name
     * @param valueDataType
     * @return
     */
    PageResult<FunctionDetailVo> functionLookUp(String name, String valueDataType);

    /**
     * 注册 httpFunction
     * @return
     */
    Boolean registerHttpFunction(AddHttpFunction function);

    Boolean updateHttpFunction(AddHttpFunction function);

    Boolean deleteHttpFunction(Long id);

    /**
     * 分页查询http function
     * @return
     * @param pageResult
     */
    PageResult<AddHttpFunction> pageHttpFunction(PageRequest<String> pageResult);


}
