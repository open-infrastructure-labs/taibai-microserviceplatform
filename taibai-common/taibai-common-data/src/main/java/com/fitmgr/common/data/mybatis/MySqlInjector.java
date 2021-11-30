package com.fitmgr.common.data.mybatis;

import com.baomidou.mybatisplus.core.injector.AbstractMethod;
import com.baomidou.mybatisplus.core.injector.DefaultSqlInjector;

import java.util.List;

public class MySqlInjector extends DefaultSqlInjector {
    @Override
    public List<AbstractMethod> getMethodList(Class<?> mapperClass) {
        List<AbstractMethod> methodList = super.getMethodList(mapperClass);
        methodList.removeIf(e -> e instanceof com.baomidou.mybatisplus.core.injector.methods.Insert);
        methodList.add(new Insert());
        return methodList;
    }
}
