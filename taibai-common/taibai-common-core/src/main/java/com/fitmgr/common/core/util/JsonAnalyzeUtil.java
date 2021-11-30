package com.fitmgr.common.core.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang.StringUtils;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.parser.Feature;

/**
 * @描述 json动态解析工具类（解析执行模板定制工具类）  
 * @author Fitmgr    
 */
public class JsonAnalyzeUtil {

    public static Map<String, String> getJsonMap(String json, List<String> list) {
        Map<String, String> map = new HashMap<>();
        String variable = getSection(json, "variable");
        for (String key : list) {
            String valueJson = getSection(variable, key);
            if (StringUtils.isNotBlank(valueJson)) {
                JSONObject jsonObject = JSONObject.parseObject(valueJson);
                map.put(key, String.valueOf(jsonObject.get("default")));
            }
        }
        return map;
    }

    /**
     * 通过组件属性的list获取Variable下面的key
     *
     * @param resourceJson 执行模板
     * @param key          list
     * @return
     */
    public static List<String> getVariableKey(String resourceJson, List<String> key) {
        String variable = getSection(resourceJson, "resource");
        List<String> list = new ArrayList<>();
        for (String s : key) {
            String josnValue = findJosnKey(variable, s);
            if (josnValue != null) {
                list.add(josnValue);
            }
        }
        return list;
    }

    /**
     * 获取指定部分的json字符串
     *
     * @param variable terrafrom_json字符串
     * @param section  指定部分名称(provider,resource,variable)
     * @return 返回指定部分字符串
     */
    public static String getSection(String variable, String section) {
        LinkedHashMap<String, Object> linkObj = JSON.parseObject(variable, LinkedHashMap.class, Feature.OrderedField);
        Object obj = linkObj.get(section);
        if (obj != null && StringUtils.isNotBlank(obj.toString())) {
            return obj.toString();
        }
        return null;
    }

    /**
     * 通过指定key 获取value
     *
     * @param jsonStr json
     * @param key     key
     * @return
     */
    public static String findJosnKey(String jsonStr, String key) {
        String fieldValues = null;
        String regex = "(?<=(\"" + key + "\":\")).*?(?=(\"))";
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(jsonStr);
        while (matcher.find()) {
            if (StringUtils.isNotEmpty(matcher.group().trim())) {
                String trim = matcher.group().trim();
                fieldValues = findJosnValue2(trim);
            }
        }
        return fieldValues;
    }

    /**
     * 匹配以 .开头，以 } 结尾的数据
     *
     * @param josn
     * @return
     */
    public static String findJosnValue2(String josn) {
        String regex = "\\.(\\w+?)}";
        Matcher matcher = Pattern.compile(regex).matcher(josn);
        String value = null;
        if (matcher.find()) {
            value = matcher.group(1).trim();
        }
        return value;
    }

    /**
     * // * 匹配以 key：开头，以 " 结尾的数据 // * // * @param key 指定的key // * @param josn json模板
     * // * @return //
     */
//    public static String findJosnValue(String josn, String key) {
//        String regex = "\"" + key + "\": (\"(.*?)\"|(\\d*))";
//        Matcher matcher = Pattern.compile(regex).matcher(josn);
//        String value = null;
//        while (matcher.find()) {
//            if (StringUtils.isNotEmpty(matcher.group().trim())) {
//                value = matcher.group().split("\\:")[1].replace("\"", "").trim();
//                return findJosnValue2(value);
//            }
//        }
//        return null;
//    }
//    public static void main(String[] args) {
//        String ss = "{\n" +
//                "\t\"provider\": {\n" +
//                "\t\t\"resourcecenter\": {\n" +
//                "\t\t\t\"password\": \"Abc12345\",\n" +
//                "\t\t\t\"tenant_name\": \"admin\",\n" +
//                "\t\t\t\"logpath\": \"log\",\n" +
//                "\t\t\t\"software_center_addr\": \"http://10.127.9.55:7770\",\n" +
//                "\t\t\t\"user_name\": \"admin\",\n" +
//                "\t\t\t\"resource_center_addr\": \"http://192.168.1.105:8087\",\n" +
//                "\t\t\t\"auth_url\": \"https://10.127.7.150:35357/v3\",\n" +
//                "\t\t\t\"cacert_file\": \"/etc/ssl/ca.crt\",\n" +
//                "\t\t\t\"region\": \"RegionOne\"\n" +
//                "\t\t}\n" +
//                "\t},\n" +
//                "\t\"resource\": {\n" +
//                "\t\t\"resourcecenter_compute_instance_v1\": {\n" +
//                "\t\t\t\"compute_instance_wx_test\": {\n" +
//                "\t\t\t\t\"flavor_id\": \"${var.flavor_id}\",\n" +
//                "\t\t\t\t\"cluster_id\": \"${var.cluster_id}\",\n" +
//                "\t\t\t\t\"resource_pool_id\": \"${var.resource_pool_id}\",\n" +
//                "\t\t\t\t\"name\": \"${var.cim_name}\",\n" +
//                "\t\t\t\t\"alias\": \"${var.alias}\",\n" +
//                "\t\t\t\t\"description\": \"${var.description}\",\n" +
//                "\t\t\t\t\"subnet_id\": \"${var.subnet_id}\",\n" +
//                "\t\t\t\t\"image_id\": \"${var.image_id}\"\n" +
//                "\t\t\t}\n" +
//                "\t\t}\n" +
//                "\t},\n" +
//                "\t\"variable\": {\n" +
//                "\t\t\"resource_pool_id\": {\n" +
//                "\t\t\t\"default\": \"48e48d3b-3221-4cfb-9711-ea455c17ec16\"\n" +
//                "\t\t},\n" +
//                "\t\t\"vpc_id\": {\n" +
//                "\t\t\t\"default\": \"6c3faae9-e6cc-478f-b2df-6fc3b41bcc0a\"\n" +
//                "\t\t},\n" +
//                "\t\t\"service_count\": {\n" +
//                "\t\t\t\"default\": 1\n" +
//                "\t\t},\n" +
//                "\t\t\"resource_zone_id\": {\n" +
//                "\t\t\t\"default\": \"cc5dcd1c-0db4-46a0-bca3-4fd7118abeb4\"\n" +
//                "\t\t},\n" +
//                "\t\t\"cluster_pool_id\": {\n" +
//                "\t\t\t\"default\": \"17dfcf88-688c-4ab4-9640-a257e38a117b\"\n" +
//                "\t\t},\n" +
//                "\t\t\"description\": {\n" +
//                "\t\t\t\"default\": \"阿三打撒\"\n" +
//                "\t\t},\n" +
//                "\t\t\"resource_use\": {\n" +
//                "\t\t\t\"default\": \"0\"\n" +
//                "\t\t},\n" +
//                "\t\t\"image_version\": {\n" +
//                "\t\t\t\"default\": \"7.1\"\n" +
//                "\t\t},\n" +
//                "\t\t\"flavor_id\": {\n" +
//                "\t\t\t\"default\": \"b5381ea0-d34d-40e7-b86c-80d9bc9ecbda\"\n" +
//                "\t\t},\n" +
//                "\t\t\"cluster_id\": {\n" +
//                "\t\t\t\"default\": \"69066488-631a-4008-9657-cd91a470fe6a\"\n" +
//                "\t\t},\n" +
//                "\t\t\"cim_name\": {\n" +
//                "\t\t\t\"default\": \"202002231719\"\n" +
//                "\t\t},\n" +
//                "\t\t\"storage_level\": {\n" +
//                "\t\t\t\"default\": \"低\"\n" +
//                "\t\t},\n" +
//                "\t\t\"opt_app\": {\n" +
//                "\t\t\t\"default\": 1\n" +
//                "\t\t},\n" +
//                "\t\t\"opt_app_log\": {\n" +
//                "\t\t\t\"default\": 1\n" +
//                "\t\t},\n" +
//                "\t\t\"service\": {\n" +
//                "\t\t\t\"default\": []\n" +
//                "\t\t},\n" +
//                "\t\t\"subnet_id\": {\n" +
//                "\t\t\t\"default\": \"e5f29c79-edc7-4d55-a824-10d00c7120b3\"\n" +
//                "\t\t},\n" +
//                "\t\t\"alias\": {\n" +
//                "\t\t\t\"default\": \"war-s2\"\n" +
//                "\t\t},\n" +
//                "\t\t\"region\": {\n" +
//                "\t\t\t\"default\": \"fd7ff24b-dc71-4fb9-968d-44b5d11d713d\"\n" +
//                "\t\t},\n" +
//                "\t\t\"image_id\": {\n" +
//                "\t\t\t\"default\": \"7c96d01f-c67b-4761-94fb-07e21b52b7bf\"\n" +
//                "\t\t}\n" +
//                "\t}\n" +
//                "}";
//        List<String> list = new ArrayList<>();
//        list.add("flavor_id");
//        list.add("cloud_disk");
//        list.add("public_network_ip");
//        list.add("lb");
//        String aa = "{\"resourcecenter_compute_instance_v1\":{\"compute_instance_wx_test\":{\"flavor_id\":\"${var.flavor_id}\",\"cluster_id\":\"${var.cluster_id}\",\"resource_pool_id\":\"${var.resource_pool_id}\",\"name\":\"${var.cim_name}\",\"alias\":\"${var.alias}\",\"description\":\"${var.description}\",\"subnet_id\":\"${var.subnet_id}\",\"image_id\":\"${var.image_id}\"}}}";
//        System.out.println(getVariableKey(ss, list));
////        System.out.println(getVariableKey(ss, list));
//
//    }

}
