package com.zjb.ruleplatform.util;

import com.google.common.collect.Maps;
import com.zjb.ruleengine.core.enums.DataTypeEnum;

import java.util.Map;

public class DataTypeUtils {

    private static final Map<String, String> map = Maps.newHashMap();
    static {
        map.put(DataTypeEnum.BOOLEAN.name(), "布尔");
        map.put(DataTypeEnum.COLLECTION.name(), "集合");
        map.put(DataTypeEnum.STRING.name(), "字符串");
        map.put(DataTypeEnum.NUMBER.name(), "数字");
        map.put(DataTypeEnum.POJO.name(), "JAVA对象");
        map.put(DataTypeEnum.JSONOBJECT.name(), "JSON对象");

    }

    public static String getName(String dataType) {
        return map.getOrDefault(dataType, "");
    }
}
