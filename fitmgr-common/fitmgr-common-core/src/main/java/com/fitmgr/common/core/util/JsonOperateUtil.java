package com.fitmgr.common.core.util;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.commons.lang.StringUtils;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONException;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.TypeReference;
import com.alibaba.fastjson.parser.Feature;
import com.fitmgr.common.core.constant.enums.BusinessEnum;
import com.fitmgr.common.core.exception.JsonException;

/**
 * @Author: yf
 * @Description: JSON操作工具类
 * @Date: 2019/11/27 10:04
 */
public class JsonOperateUtil {

    private static final int LEVER_TWO = 2;

    /**
     * 固定结构数据模板临时接收变量
     */
    private static String str = "";

    /**
     * 最底层key容器(无序,去重集合)
     */
    private static Set<String> setList = new HashSet<>();

    /**
     * 递归动态层级替换key值
     *
     * @param jsonObj 原json对象
     * @param keyMap  新key的map
     * @return 替换key后的json字符串
     */
    public static JSONObject changeJsonKey(JSONObject jsonObj, Map<String, String> keyMap) {
        JSONObject resJson = new JSONObject(true);
        Set<String> keySet = jsonObj.keySet();
        for (String key : keySet) {
            String resKey = keyMap.get(key) == null ? key : keyMap.get(key);
            try {
                JSONObject jsonobj1 = jsonObj.getJSONObject(key);
                resJson.put(resKey, changeJsonKey(jsonobj1, keyMap));
            } catch (Exception e) {
                try {
                    JSONArray jsonArr = jsonObj.getJSONArray(key);
                    resJson.put(resKey, changeJsonArr(jsonArr, keyMap));
                } catch (Exception x) {
                    resJson.put(resKey, jsonObj.get(key));
                }
            }
        }
        return resJson;
    }

    /**
     * json数组递归
     * 
     * @param jsonArr jsonArr
     * @param keyMap  keyMap
     * @return JSONArray
     */
    public static JSONArray changeJsonArr(JSONArray jsonArr, Map<String, String> keyMap) {
        JSONArray resJson = new JSONArray();
        for (int i = 0; i < jsonArr.size(); i++) {
            JSONObject jsonObj = jsonArr.getJSONObject(i);
            resJson.add(changeJsonKey(jsonObj, keyMap));
        }
        return resJson;
    }

    /**
     * 递归遍历更改指定value值
     *
     * @param jsonObj  JSON数据
     * @param mapKey   原指定value值
     * @param mapValue 需要替换的新的指定value值
     * @return 提后换的完整JSON数据
     */
    public static JSONObject changeJsonValue(JSONObject jsonObj, String mapKey, String mapValue) {
        JSONObject resJson = new JSONObject(true);
        Set<String> keySet = jsonObj.keySet();
        for (String key : keySet) {
            try {
                JSONObject obj = jsonObj.getJSONObject(key);
                resJson.put(key, changeJsonValue(obj, mapKey, mapValue));
            } catch (Exception e) {
                try {
                    JSONArray jsonArr = jsonObj.getJSONArray(key);
                    resJson.put(key, changeJsonArr(jsonArr, mapKey, mapValue));
                } catch (Exception x) {
                    String value = jsonObj.getString(key);
                    try {
                        String val = value.substring(value.indexOf(".") + 1, value.lastIndexOf("."));
                        String newValue;
                        if (val.contains(mapKey)) {
                            /**
                             * zxk 修改 begin >>>>>>>> 处理volume后面带[*]的问题
                             */
                            if (val.contains("[*]")) {
                                newValue = value.replace("." + mapKey + "[*].", "." + mapValue + "[*].");
                            } else {
                                newValue = value.replace("." + mapKey + ".", "." + mapValue + ".");
                            }
                            /**
                             * zxk 修改 end >>>>>>>>
                             */
                            getResultByTry(resJson, key, newValue);
                        } else {
                            getResultByTry(resJson, key, value);
                        }
                    } catch (Exception y) {
                        getResultByTry(resJson, key, value);
                    }
                }
            }
        }
        return resJson;
    }

    /**
     * JSON数组结构递归
     * 
     * @param jsonArr  jsonArr
     * @param mapKey   mapKey
     * @param mapValue mapValue
     * @return JSONArray
     */
    public static JSONArray changeJsonArr(JSONArray jsonArr, String mapKey, String mapValue) {
        JSONArray resJson = new JSONArray();
        for (int i = 0; i < jsonArr.size(); i++) {
            JSONObject jsonObj = jsonArr.getJSONObject(i);
            resJson.add(changeJsonValue(jsonObj, mapKey, mapValue));
        }
        return resJson;
    }

    /**
     * 利用异常做兼容处理
     * 
     * @param resJson resJson
     * @param key     key
     * @param value   value
     */
    private static void getResultByTry(JSONObject resJson, String key, String value) {
        try {
            resJson.put(key, JSONObject.parseObject(value));
        } catch (Exception e) {
            resJson.put(key, JSONObject.parseArray(value));
        }
    }

    /**
     * 递归遍历获取所有key值
     *
     * @param obj json对象
     * @return
     */
    public static StringBuffer getAllKey(JSONObject obj) {
        StringBuffer stringBuffer = new StringBuffer();
        Iterator<String> keys = obj.keySet().iterator();
        while (keys.hasNext()) {
            String key = keys.next();
            stringBuffer.append(key.toString()).append(",");
            if (obj.get(key) instanceof JSONObject) {
                JSONObject innerObject = (JSONObject) obj.get(key);
                stringBuffer.append(getAllKey(innerObject));
            } else if (obj.get(key) instanceof JSONArray) {
                JSONArray innerObject = (JSONArray) obj.get(key);
                stringBuffer.append(getAllKey(innerObject));
            }
        }
        return stringBuffer;
    }

    /**
     * 递归遍历获取所有key值
     *
     * @param obj json数组
     * @return
     */
    public static StringBuffer getAllKey(JSONArray obj) {
        StringBuffer stringBuffer = new StringBuffer();
        if (obj != null) {
            Iterator i1 = obj.iterator();
            while (i1.hasNext()) {
                Object key = i1.next();
                if (key instanceof JSONObject) {
                    JSONObject innerObject = (JSONObject) key;
                    stringBuffer.append(getAllKey(innerObject));
                } else if (key instanceof JSONArray) {
                    JSONArray innerObject = (JSONArray) key;
                    stringBuffer.append(getAllKey(innerObject));
                } else {
                }
            }
        }
        return stringBuffer;
    }

    /**
     * 解析json字符串第一级所有key的底层key值
     *
     * @param json json字符串
     * @return
     */
    public static Set<String> parseJsonKeyByAll(String json) {
        LinkedHashMap<String, Object> jsonMap = JSON.parseObject(json,
                new TypeReference<LinkedHashMap<String, Object>>() {
                });
        for (Map.Entry<String, Object> entry : jsonMap.entrySet()) {
            System.out.println(entry.getKey());
            parseJsonMap(entry);
        }
        return setList;
    }

    /**
     * 解析json字符串resource部分底层key值
     *
     * @param json json字符串
     * @return
     */
    public static Set<String> parseJsonKeyByResource(String json) {
        try {
            LinkedHashMap<String, Object> jsonMap = JSON.parseObject(json,
                    new TypeReference<LinkedHashMap<String, Object>>() {
                    });
            for (Map.Entry<String, Object> entry : jsonMap.entrySet()) {
                if (("resource").equals(entry.getKey())) {
                    parseJsonMap(entry);
                }
            }
            return setList;
        } catch (JSONException e) {
            throw new JsonException(BusinessEnum.JSON_WRONG_FORMAT);
        }
    }

    /**
     * 解析
     * 
     * @param entry entry
     */
    public static void parseJsonMap(Map.Entry<String, Object> entry) {
        // 如果是单个map继续递归遍历
        if (entry.getValue() instanceof Map) {
            LinkedHashMap<String, Object> jsonMap = JSON.parseObject(entry.getValue().toString(),
                    new TypeReference<LinkedHashMap<String, Object>>() {
                    });
            for (Map.Entry<String, Object> entry2 : jsonMap.entrySet()) {
                parseJsonMap(entry2);
            }
        }
        // 如果是String或者list就获取key
        if (entry.getValue() instanceof String || entry.getValue() instanceof List) {
            setList.add(entry.getKey() + ":" + entry.getValue());
        }
    }

    /**
     * TF文件的json数据合并 ---- 合并resource部分(多份合并,只增不减)
     *
     * @param jsonList json数据的集合
     * @return 合并完成后的数据
     */
    public static JSONObject mergeJson(List<String> jsonList) {
        // 定义返回JSON
        JSONObject result = new JSONObject(true);
        try {
            JSONArray array = new JSONArray();
            for (String item : jsonList) {
                LinkedHashMap<String, Object> arrayObj = JSON.parseObject(item, LinkedHashMap.class,
                        Feature.OrderedField);
                if (arrayObj.containsKey("provider") && arrayObj.containsKey("output")
                        && arrayObj.containsKey("variable")) {
                    str = item;
                }
                // 存入json数组
                array.add(arrayObj);
            }
            // 当缺失头部和尾部基础数据时抛出格式错误异常
            if (StringUtils.isBlank(str)) {
                throw new JsonException(BusinessEnum.JSON_WRONG_FORMAT.getDescription());
            }
            // 符合,保存到容器中
            LinkedHashMap<String, Object> commomObj = JSON.parseObject(str, LinkedHashMap.class, Feature.OrderedField);

            // 遍历获取key = resource下的内容,并合并后存储到新的json对象中
            JSONObject resourceObj = new JSONObject(true);
            for (int i = 0; i < array.size(); i++) {
                Object object = array.getJSONObject(i).getString("resource");
                LinkedHashMap<String, Object> linkObj = JSON.parseObject(object.toString(), LinkedHashMap.class,
                        Feature.OrderedField);
                resourceObj.putAll(linkObj);
            }

            /**
             * 从容器中取出各部分数据,组成完整json
             */
            result.put("provider", commomObj.get("provider"));
            result.put("resource", resourceObj);
            result.put("output", commomObj.get("output"));
            result.put("variable", commomObj.get("variable"));
        } catch (Exception e) {
            throw new JsonException(BusinessEnum.JSON_MERGE.getDescription());
        }
        return result;
    }

    /**
     * 合并json所有主key部分(provider,resource,variable)
     *
     * @param jsonList terraform_json数据
     * @return 合并后的数据
     */
    public static JSONObject mergeJsonAll(List<String> jsonList) {
        // 定义返回JSON
        JSONObject result = new JSONObject(true);

        try {
            JSONArray array = new JSONArray();
            for (String item : jsonList) {
                LinkedHashMap<String, Object> arrayObj = JSON.parseObject(item, LinkedHashMap.class,
                        Feature.OrderedField);
                array.add(arrayObj);
            }

            // 遍历获取各key下的内容,并合并后存储到新的json对象中
            JSONObject providerObj = new JSONObject(true);
            JSONObject resourceObj = new JSONObject(true);
            JSONObject outputObj = new JSONObject(true);
            JSONObject variableObj = new JSONObject(true);
            for (int i = 0; i < array.size(); i++) {
                // 合并provider部分
                Object providerObject = array.getJSONObject(i).getString("provider");
                if (providerObject != null) {
                    LinkedHashMap<String, Object> linkObj1 = JSON.parseObject(providerObject.toString(),
                            LinkedHashMap.class, Feature.OrderedField);
                    providerObj.putAll(linkObj1);
                }

                // 合并resource部分
                Object resourceObject = array.getJSONObject(i).getString("resource");
                if (resourceObject != null) {
                    LinkedHashMap<String, Object> linkObj2 = JSON.parseObject(resourceObject.toString(),
                            LinkedHashMap.class, Feature.OrderedField);
                    resourceObj.putAll(linkObj2);
                }

                // 合并output部分
                Object outputObject = array.getJSONObject(i).getString("output");
                if (outputObject != null) {
                    LinkedHashMap<String, Object> linkObj3 = JSON.parseObject(outputObject.toString(),
                            LinkedHashMap.class, Feature.OrderedField);
                    outputObj.putAll(linkObj3);
                }

                // 合并variable部分
                Object variableObject = array.getJSONObject(i).getString("variable");
                if (variableObject != null) {
                    LinkedHashMap<String, Object> linkObj4 = JSON.parseObject(variableObject.toString(),
                            LinkedHashMap.class, Feature.OrderedField);
                    variableObj.putAll(linkObj4);
                }
            }

            // 当缺失头部和尾部基础数据时抛出格式错误异常
            if (providerObj == null || resourceObj == null || variableObj == null) {
                throw new JsonException(BusinessEnum.JSON_WRONG_FORMAT.getDescription());
            }

            /**
             * 从容器中取出各部分数据,组成完整json
             */
            result.put("provider", providerObj);
            result.put("resource", resourceObj);
            result.put("output", outputObj);
            result.put("variable", variableObj);
        } catch (JsonException e) {
            throw e;
        } catch (Exception e) {
            throw new JsonException(BusinessEnum.JSON_MERGE.getDescription());
        }

        return result;
    }

    /**
     * 获取指定部分的json字符串
     *
     * @param json    terrafrom_json字符串
     * @param section 指定部分名称(provider,resource,variable)
     * @return 返回指定部分字符串
     */
    public static String getSection(String json, String section) {
        Object obj;
        try {
            LinkedHashMap<String, Object> linkObj = JSON.parseObject(json, LinkedHashMap.class, Feature.OrderedField);
            obj = linkObj.get(section);
            if (obj == null) {
                throw new JsonException(BusinessEnum.JSON_WRONG_FORMAT);
            }
            if (StringUtils.isBlank(obj.toString())) {
                throw new JsonException(BusinessEnum.JSON_EMPTY);
            }
        } catch (JSONException e) {
            throw new JsonException(BusinessEnum.JSON_WRONG_STRUCTURE);
        }
        return obj.toString();
    }

    /**
     * 批量根据key替换value
     *
     * @param json 原terraform_json字符串
     * @param map  key和value的map集合
     * @return 返回新的json字符串
     */
    public static JSONObject changeJsonValue(String json, Map<String, String> map) {
        LinkedHashMap<String, Object> linkedJson = (LinkedHashMap) JSON.parseObject(json, LinkedHashMap.class,
                new Feature[] { Feature.OrderedField });
        JSONObject jsonObject = new JSONObject(true);
        jsonObject.putAll(linkedJson);
        Iterator entrys = map.entrySet().iterator();

        while (entrys.hasNext()) {
            Entry<String, String> entry = (Entry<String, String>) entrys.next();
            // 通过key查找对应的json片段,然后替换值
            String obj = changeJsonValue(getSection(json, entry.getKey()), "default", entry.getValue());
            jsonObject.put(entry.getKey().trim(), JSON.parseObject(obj));
        }

        return jsonObject;
    }

    /**
     * 根据key替换value ---- 单个替换
     *
     * @param json  原terraform_json字符串
     * @param key   需要替换值的key
     * @param value 需要替换成的value(JSON对象)
     * @return 返回新的json字符串
     */
    public static String changeJsonValue(String json, String key, JSONObject value) {
        LinkedHashMap<String, Object> linkedJson = (LinkedHashMap) JSON.parseObject(json, LinkedHashMap.class,
                new Feature[] { Feature.OrderedField });
        JSONObject jsonObject = new JSONObject(true);
        jsonObject.putAll(linkedJson);
        jsonObject.put(key, value);
        return jsonObject.toJSONString();
    }

    /**
     * 根据key替换value ---- 单个替换
     *
     * @param json  原terraform_json字符串
     * @param key   需要替换值的key
     * @param value 需要替换成的value(字符串)
     * @return 返回新的json字符串
     */
    public static String changeJsonValue(String json, String key, String value) {
        LinkedHashMap<String, Object> linkedJson = (LinkedHashMap) JSON.parseObject(json, LinkedHashMap.class,
                new Feature[] { Feature.OrderedField });
        JSONObject jsonObject = new JSONObject(true);
        jsonObject.putAll(linkedJson);
        jsonObject.put(key, value);
        return jsonObject.toJSONString();
    }

    /**
     * 根据层级查找指定层的key
     *
     * @param obj   json对象
     * @param level 层级(例如1.2)
     * @return list
     */
    public static List<String> getKeysByLevel(JSONObject obj, int level) {
        List<String> list = new ArrayList<>();
        Iterator<String> keys = obj.keySet().iterator();

        if (level == 1) {
            while (keys.hasNext()) {
                String key = keys.next();
                if (obj.get(key) instanceof JSONObject) {
                    list.add(key);
                }
            }
        } else if (level == LEVER_TWO) {
            while (keys.hasNext()) {
                String key = keys.next();
                if (obj.get(key) instanceof JSONObject) {
                    JSONObject object = (JSONObject) obj.get(key);
                    Iterator<String> itemKeys = object.keySet().iterator();
                    while (itemKeys.hasNext()) {
                        list.add(itemKeys.next());
                    }
                }
            }
        }
        return list;
    }

    /**
     * 查找子节点所有key(针对子节点为数组结构)
     *
     * @param json json字符串
     * @return list
     */
    public static List<String> getKeysByLevel(String json) {
        List<String> list = new ArrayList<>();
        JSONArray jsonArray = JSONObject.parseArray(json);

        for (int i = 0; i < jsonArray.size(); i++) {
            JSONObject jsonObject = jsonArray.getJSONObject(i);
            Iterator<String> keys = jsonObject.keySet().iterator();
            while (keys.hasNext()) {
                String key = keys.next();
                list.add(key);
            }
        }
        return list;
    }

    /**
     * 查找子节点第二层key(针对查找资源自定义名称)
     *
     * @param json json字符串(子节点为数组结构)
     * @return list
     */
    public static List<String> getKeysArrayByTwoFloor(String json) {
        List<String> list = new ArrayList<>();
        JSONArray jsonArray = JSONObject.parseArray(json);

        for (int i = 0; i < jsonArray.size(); i++) {
            JSONObject jsonObject = jsonArray.getJSONObject(i);
            Iterator keys = jsonObject.keySet().iterator();

            while (keys.hasNext()) {
                String key = (String) keys.next();
                JSONArray itemArr = (JSONArray) jsonObject.get(key);
                for (int j = 0; j < itemArr.size(); j++) {
                    JSONObject itemObj = itemArr.getJSONObject(j);
                    Iterator itemKeys = itemObj.keySet().iterator();
                    while (itemKeys.hasNext()) {
                        String itemKey = (String) itemKeys.next();
                        list.add(itemKey);
                    }
                }
            }
        }
        return list;
    }

    /**
     * 查找子节点第二层key(针对查找资源自定义名称)
     *
     * @param json json字符串(子节点为对象结构)
     * @return list
     */
    public static List<String> getKeysObjectByTwoFloor(String json) {
        List<String> list = new ArrayList<>();
        JSONObject jsonObject = JSONObject.parseObject(json);
        Iterator keys = jsonObject.keySet().iterator();

        while (keys.hasNext()) {
            String key = (String) keys.next();
            JSONObject itemObj = (JSONObject) jsonObject.get(key);
            Iterator itemKeys = itemObj.keySet().iterator();
            while (itemKeys.hasNext()) {
                String itemKey = (String) itemKeys.next();
                list.add(itemKey);
            }
        }
        return list;
    }

}
