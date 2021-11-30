package com.taibai.admin.cache;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.support.SFunction;
import com.taibai.admin.api.entity.Function;
import com.taibai.admin.mapper.FunctionMapper;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class FunctionCache {
    Map<String, Map<Integer, Function>> functionMap = new HashMap<>();

    @Autowired
    private FunctionMapper functionMapper;

    public Map<String, Map<Integer, Function>> getFunctionCache(){
        if(functionMap == null) {
            functionMap = new HashMap<>();
            List<Function> functions = functionMapper.selectList(new QueryWrapper<Function>().lambda().eq(Function::getDelFlag, "0"));
            for (Function function : functions) {
                if (StringUtils.isEmpty(function.getApiUrl())
                        || StringUtils.isEmpty(function.getHttpMethod())) {
                    continue;
                }
                String[] arr = function.getApiUrl().split("/");
                if(arr.length >= 2) {
                    Map<Integer, Function> functions1 = functionMap.get(arr[1]);
                    if (functions1 == null) {
                        functions1 = new HashMap<>();
                        functionMap.put(arr[1], functions1);
                    }
                    functions1.put(function.getId(), function);
                }
            }
        }
        return functionMap;
    }

    public void updateCache(){
//        if(functionMap == null) {
            functionMap = new HashMap<>();
//        }
        List<Function> functions = functionMapper.selectList(new QueryWrapper<Function>().lambda().eq(Function::getDelFlag, "0"));
        for (Function function : functions) {
            if (StringUtils.isEmpty(function.getApiUrl())
                    || StringUtils.isEmpty(function.getHttpMethod())) {
                continue;
            }
            String[] arr = function.getApiUrl().split("/");
            if(arr.length >= 2) {
                Map<Integer, Function> functions1 = functionMap.get(arr[1]);
                if (functions1 == null) {
                    functions1 = new HashMap<>();
                    functionMap.put(arr[1], functions1);
                }
                functions1.put(function.getId(), function);
            }
        }
    }
}
