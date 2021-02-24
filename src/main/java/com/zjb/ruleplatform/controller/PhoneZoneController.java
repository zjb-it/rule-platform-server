package com.zjb.ruleplatform.controller;

import cn.hutool.core.util.StrUtil;
import cn.hutool.http.HttpUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Maps;
import com.zjb.ruleplatform.entity.common.PlainResult;
import com.zjb.ruleplatform.entity.dto.Phone;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;

/**
 * @author 赵静波
 * Created on 2021-02-23
 */
@RestController
@RequestMapping("/ruleEngine/test")
public class PhoneZoneController {

    @PostMapping("/getPhoneZone")
    public Object getPhoneZone(@RequestBody Phone phone) {
        final String s = HttpUtil.get("https://tcc.taobao.com/cc/json/mobile_tel_segment.htm?tel=" + phone.getPhone());
        return new PlainResult<>(JSON.parseObject(s.replace("__GetZoneResult_ =","")));
    }
    public static void main(String[] args) throws Exception{
        final String s = HttpUtil.get("https://tcc.taobao.com/cc/json/mobile_tel_segment.htm?tel=" + "17611592687");
        final String replace = s.replace("__GetZoneResult_ =", "");
        final JSONObject jsonObject = JSON.parseObject(replace);
        final HashMap<Object, Object> objectObjectHashMap = Maps.newHashMap();
        objectObjectHashMap.put("data", jsonObject);
        final JsonNode jsonNode1 = new ObjectMapper().readTree(JSON.toJSONString(objectObjectHashMap));
        String fieldName = "data";
        final String[] split = fieldName.split("\\.");
        JsonNode jsonNode = jsonNode1;
        for (String name : split) {
            jsonNode = jsonNode.get(name);
        }
        System.out.println(jsonNode);
    }
}
