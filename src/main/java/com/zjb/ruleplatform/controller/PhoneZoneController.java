package com.zjb.ruleplatform.controller;

import cn.hutool.http.HttpUtil;
import com.alibaba.fastjson.JSON;
import com.zjb.ruleplatform.entity.dto.Phone;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author 赵静波
 * Created on 2021-02-23
 */
@RestController
@RequestMapping("/test")
public class PhoneZoneController {

    @PostMapping("/getPhoneZone")
    public Object getPhoneZone(Phone phone) {
        final String s = HttpUtil.get("https://tcc.taobao.com/cc/json/mobile_tel_segment.htm?tel=" + phone.getPhone());
        return JSON.parseObject(s.replace("__GetZoneResult_ =",""));
    }

}
