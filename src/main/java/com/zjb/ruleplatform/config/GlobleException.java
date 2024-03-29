package com.zjb.ruleplatform.config;

import com.zjb.ruleplatform.entity.common.BaseResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * @author 赵静波
 * @date 2021-02-10 16:39:47
 */
@ControllerAdvice
@Slf4j
public class GlobleException {

    @ResponseBody
    @ExceptionHandler
    public BaseResult exception(Exception exception) {
        log.error("{}",exception);

        return new BaseResult().setErrorMessage("9851", exception.getClass().getName()+":"+exception.getMessage());
    }
}
