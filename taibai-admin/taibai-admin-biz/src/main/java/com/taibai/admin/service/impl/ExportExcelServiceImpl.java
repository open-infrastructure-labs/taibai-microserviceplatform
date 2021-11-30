package com.taibai.admin.service.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import javax.servlet.http.HttpServletResponse;

import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.taibai.admin.api.entity.ExcelData;
import com.taibai.admin.api.entity.ExcelLevelMenu;
import com.taibai.admin.api.entity.Function;
import com.taibai.admin.api.entity.Menu;
import com.taibai.admin.api.entity.Role;
import com.taibai.admin.api.entity.RoleFunction;
import com.taibai.admin.api.vo.MenuVO;
import com.taibai.admin.mapper.ExportExcelMapper;
import com.taibai.admin.service.ExportExcelService;
import com.taibai.admin.utils.ExcelUtil;
import com.taibai.common.core.util.R;
import com.taibai.template.api.feign.RemoteServiceModelService;
import com.taibai.template.api.vo.ServiceMenuVO;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * @author Taibai
 * @date ：Created in 2021/1/11 14:18
 * @modified By：
 */
@Service
@Slf4j
@AllArgsConstructor
public class ExportExcelServiceImpl implements ExportExcelService {

    private final ExportExcelMapper exportExcelMapper;

    private final MenuServiceImpl menuService;

    private final RemoteServiceModelService remoteServiceModelService;

    public void exportRoleMenuExcel(HttpServletResponse response) throws Exception {

        List<Menu> menuList = exportExcelMapper.findRoleMenu();
        List<String> titles = exportExcelMapper.findRoleNameList();
        titles.add(0, "");
        titles.add(0, "");

        List<String> menuName = exportExcelMapper.findMenuTitle();
        log.info("menuList:" + menuList);
        log.info("titles:" + titles);
        log.info("menuName:" + menuName);

        JSONObject jsonObject = new JSONObject();
        for (String key : menuName) {
            List<Object> valueList = new ArrayList<>();
            for (String roleName : titles) {
                int i = 0;

                if ("".equals(roleName)) {
                    continue;
                }
                for (Menu menu : menuList) {
                    if (roleName.equals(menu.getName()) && key.equals(menu.getTitle())) {
                        i = 1;
                        break;
                    }
                }
                if (1 == i) {
                    valueList.add("是");
                } else {
                    valueList.add("否");
                }
                jsonObject.put(key, valueList);
            }
        }
        List<List<Object>> rows = new ArrayList();
        for (String key : jsonObject.keySet()) {
            List<Object> valueList = jsonObject.getJSONArray(key);
            valueList.add(0, key);
            rows.add(valueList);
        }

        log.info("rows:" + JSONArray.toJSONString(rows));
        List<ExcelLevelMenu> levelMenuList = exportExcelMapper.findLevelMenuList();
        log.info("levelMenuList" + JSONObject.toJSONString(levelMenuList));
        JSONObject levelMenuJsonObject = new JSONObject();
        for (ExcelLevelMenu excelLevelMenu : levelMenuList) {
            if (null == excelLevelMenu.getParentTitle()) {
                continue;
            }
            if (!levelMenuJsonObject.containsKey(excelLevelMenu.getParentTitle())) {
                List<String> lowLevelMenuList = new ArrayList<>();
                lowLevelMenuList.add(excelLevelMenu.getTitle());
                levelMenuJsonObject.put(excelLevelMenu.getParentTitle(), lowLevelMenuList);
            }
            JSONArray lowLevelMenu = levelMenuJsonObject.getJSONArray(excelLevelMenu.getParentTitle());
            if (lowLevelMenu.contains(excelLevelMenu.getTitle())) {
                continue;
            }
            lowLevelMenu.add(excelLevelMenu.getTitle());
            levelMenuJsonObject.put(excelLevelMenu.getParentTitle(), lowLevelMenu);
        }
        List<MenuVO> list = menuService.getList(88);

        R tree = remoteServiceModelService.getTree();
        List<ServiceMenuVO> serviceMenuVos = (List<ServiceMenuVO>) tree.getData();

        // 单独处理云服务
        String menuId = "512b0c2ac5b24d43ab860a2d21c8e6d2";

        log.info("levelMenuJsonObject" + levelMenuJsonObject);
    }

    @Override
    public void exportRoleFunctionExcel(HttpServletResponse response) throws Exception {

        List<RoleFunction> menuList = exportExcelMapper.findRoleFunction();
        List<String> titles = exportExcelMapper.findRoleNameList();
        titles.add(0, "");
        titles.add(0, "");

        List<Function> functionList = exportExcelMapper.findFunction();
        log.info("menuList:" + menuList);
        log.info("titles:" + titles);
        log.info("menuName:" + functionList);

        JSONObject jsonObject = new JSONObject();
        JSONObject functionCodeJson = new JSONObject();
        for (Function function : functionList) {
            String key = function.getName();
            String functionCode = function.getFunctionCode();
            List<Object> valueList = new ArrayList<>();
            for (String roleName : titles) {
                int i = 0;

                if ("".equals(roleName)) {
                    continue;
                }
                for (RoleFunction roleFunction : menuList) {
                    if (roleName.equals(roleFunction.getRoleName()) && key.equals(roleFunction.getFunctionName())) {
                        i = 1;
                        break;
                    }
                }
                if (1 == i) {
                    valueList.add("是");
                } else {
                    valueList.add("否");
                }
                jsonObject.put(key, valueList);
                functionCodeJson.put(key, functionCode);
            }
        }
        ExcelData data = new ExcelData();
        List<List<Object>> rows = new ArrayList();
        for (String key : jsonObject.keySet()) {
            List<Object> valueList = jsonObject.getJSONArray(key);
            valueList.add(0, key);
            valueList.add(1, functionCodeJson.get(key));
            rows.add(valueList);
        }

        data.setTitles(titles);
        data.setName("角色操作关系表");
        data.setRows(rows);

        ExcelUtil.exportExcel(response, "角色操作关系表.xls", data);

    }

    @Override
    public void exportRoleMenuExcelTest(HttpServletResponse response) throws Exception {

        List<String> titles = exportExcelMapper.findRoleNameList();
        log.info("titles:" + JSONObject.toJSONString(titles));
        List<Role> roleList = exportExcelMapper.findRoleIdList();

        ExcelData data = new ExcelData();
        List<List<Object>> rows = new ArrayList();

        JSONObject menuIdNameJson = new JSONObject();
        JSONObject parentIdJson = new JSONObject();

        JSONObject jsonObject = new JSONObject();
        for (Role role : roleList) {
            List<MenuVO> menuVoList = menuService.getList(role.getId());
            for (MenuVO menuVO : menuVoList) {
                if (!"0".equals(menuVO.getParentId())) {
                    parentIdJson.put(menuVO.getMenuId(), menuVO.getParentId());
                }

                if (null != menuVO.getMeta()) {
                    menuIdNameJson.put(menuVO.getMenuId(), menuVO.getMeta().getTitle());
                } else {
                    menuIdNameJson.put(menuVO.getMenuId(), menuVO.getTitle());
                }
                if (jsonObject.containsKey(menuVO.getMenuId())) {
                    JSONArray jsonArray = jsonObject.getJSONArray(menuVO.getMenuId());
                    jsonArray.add(role.getRoleName());
                    jsonObject.put(menuVO.getMenuId(), jsonArray);
                    continue;
                }

                JSONArray jsonArray = new JSONArray();
                jsonArray.add(role.getRoleName());
                jsonObject.put(menuVO.getMenuId(), jsonArray);

            }
        }

        log.info("jsonObject:" + jsonObject);
        log.info("size:" + jsonObject.keySet().size());

        List<Menu> parentIdList = exportExcelMapper.findParentId();
        for (Menu menu : parentIdList) {
            if (!"0".equals(menu.getParentId())) {
                if (!parentIdJson.containsKey(menu.getMenuId())) {
                    parentIdJson.put(menu.getMenuId(), menu.getParentId());
                }
            }
            if (!menuIdNameJson.containsKey(menu.getMenuId())) {
                menuIdNameJson.put(menu.getMenuId(), menu.getTitle());
            }
        }

        log.info("parentIdList:" + parentIdJson);

        log.info("menuIdNameJson:" + menuIdNameJson);

        // 将数据转换为是否
        for (String s : jsonObject.keySet()) {
            List<String> list = new ArrayList<>();
            JSONArray jsonArray = jsonObject.getJSONArray(s);
            for (String title : titles) {
                if (jsonArray.contains(title)) {
                    list.add("是");
                } else {
                    list.add("否");
                }
            }
            jsonObject.put(s, list);
        }

        for (String s : jsonObject.keySet()) {
            JSONArray jsonArray = jsonObject.getJSONArray(s);
            jsonArray.add(0, s);
        }

        // 获取父id加入到数组中
        for (String s : jsonObject.keySet()) {
            if (parentIdJson.containsKey(s)) {
                JSONArray jsonArray = jsonObject.getJSONArray(s);
                jsonArray.add(0, parentIdJson.get(s));
                jsonObject.put(s, jsonArray);

                if (parentIdJson.containsKey(parentIdJson.get(s))) {
                    JSONArray jsonArray1 = jsonObject.getJSONArray(s);
                    jsonArray1.add(0, parentIdJson.get(parentIdJson.get(s)));
                    jsonObject.put(s, jsonArray1);
                } else {
                    JSONArray jsonArray1 = jsonObject.getJSONArray(s);
                    jsonArray1.add(0, parentIdJson.get(s));
                    jsonObject.put(s, jsonArray1);
                }
            } else {
                JSONArray jsonArray = jsonObject.getJSONArray(s);
                jsonArray.add(0, s);
                jsonObject.put(s, jsonArray);
            }

            JSONArray jsonArray = jsonObject.getJSONArray(s);
            for (int i = 0; i < jsonArray.size(); i++) {
                Object o = jsonArray.get(i);
                if (menuIdNameJson.containsKey(o.toString())) {
                    jsonArray.set(i, menuIdNameJson.get(o.toString()));
                }
            }
            jsonObject.put(s, jsonArray);

            JSONArray jsonArray1 = jsonObject.getJSONArray(s);
            if ("是".equals(jsonArray1.get(2)) || "否".equals(jsonArray1.get(2))) {
                jsonArray1.add(2, "");
            } else {
                if (jsonArray1.get(0).equals(jsonArray1.get(1))) {
                    jsonArray1.set(1, jsonArray1.get(2));
                    jsonArray1.set(2, "");
                }
            }
            jsonObject.put(s, jsonArray1);
        }

        List<List<String>> lists = new ArrayList<>();
        for (String s : jsonObject.keySet()) {
            lists.add(JSONObject.parseArray(jsonObject.getJSONArray(s).toJSONString(), String.class));
        }
        log.info("list:" + lists);
        log.info("new Json:" + jsonObject);

        lists = lists.stream().sorted((o1, o2) -> {
            for (int i = 0; i < Math.min(o1.size(), o2.size()); i++) {
                int c = o1.get(0).compareTo(o2.get(0));
                if (c != 0) {
                    return c;
                }
            }
            return Integer.compare(o1.size(), o2.size());
        }).collect(Collectors.toList());

        log.info("resultList:" + JSONObject.toJSONString(lists));

        Map<String, List<List<String>>> resultMap = new HashMap<>();
        for (List<String> list : lists) {
            if (resultMap.containsKey(list.get(0))) {
                List<List<String>> listList = resultMap.get(list.get(0));
                listList.add(list);
                resultMap.put(list.get(0), listList);
            } else {
                List<List<String>> listList = new ArrayList<>();
                listList.add(list);
                resultMap.put(list.get(0), listList);
            }
        }

        List<List<String>> resultList = new ArrayList<>();
        for (Entry<String, List<List<String>>> entry : resultMap.entrySet()) {
            List<List<String>> listList = entry.getValue();
            listList = listList.stream().sorted((o1, o2) -> {
                for (int i = 0; i < Math.min(o1.size(), o2.size()); i++) {
                    int c = o1.get(0).compareTo(o2.get(0));
                    if (c != 0) {
                        return c;
                    }
                }
                return Integer.compare(o1.size(), o2.size());
            }).collect(Collectors.toList());
            for (List<String> list : listList) {
                resultList.add(list);
            }
        }

        log.info("resultMap:" + JSONObject.toJSONString(resultMap));
        log.info("resultList:" + JSONObject.toJSONString(resultList));

        for (List<String> list : resultList) {
            List<Object> list1 = (List<Object>) (List) list;
            rows.add(list1);
        }

        titles.add(0, "");
        titles.add(0, "");
        titles.add(0, "");

        data.setTitles(titles);
        data.setName("角色菜单关系表");
        data.setRows(rows);

        ExcelUtil.exportExcel(response, "角色菜单关系表.xls", data);
    }

}
