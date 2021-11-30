package com.taibai.admin.service.impl;

import com.alibaba.fastjson.JSON;
import com.taibai.admin.api.dto.MenuTree;
import com.taibai.common.core.util.SysTreeNode;
import lombok.extern.slf4j.Slf4j;

import java.util.Comparator;

@Slf4j
public class MenuComparator implements Comparator<SysTreeNode> {
    @Override
    public int compare(SysTreeNode o1, SysTreeNode o2) {
        if(o1 == null && o2 == null) {
            return 0;
        }

        if(o1 == null && o2 != null) {
            return -1;
        }

        if(o1 != null && o2 == null) {
            return 1;
        }

        if(o1 instanceof MenuTree) {
            MenuTree m1 = (MenuTree)o1;
            if(o2 instanceof MenuTree) {
                MenuTree m2 = (MenuTree)o2;
                return m1.getMenuOrder() - m2.getMenuOrder();
            }else {
                return 1;
            }
        }else {
            return -1;
        }
    }
}
