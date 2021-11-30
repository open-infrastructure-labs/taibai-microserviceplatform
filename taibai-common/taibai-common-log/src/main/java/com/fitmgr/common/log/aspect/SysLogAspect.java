
package com.fitmgr.common.log.aspect;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang3.StringUtils;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.client.serviceregistry.Registration;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import com.alibaba.fastjson.JSONObject;
import com.fitmgr.common.core.constant.CommonConstants;
import com.fitmgr.common.core.constant.SecurityConstants;
import com.fitmgr.common.core.constant.enums.BusinessEnum;
import com.fitmgr.common.core.constant.enums.ResponseCodeEnum;
import com.fitmgr.common.core.util.R;
import com.fitmgr.common.log.annotation.SysLog;
import com.fitmgr.common.log.event.SysLogEvent;
import com.fitmgr.common.log.format.IFormat;
import com.fitmgr.common.log.util.SysLogUtils;
import com.fitmgr.common.security.service.FitmgrUser;
import com.fitmgr.common.security.util.SecurityUtils;
import com.fitmgr.log.api.dto.OperateLogDTO;
import com.fitmgr.log.api.entity.OperateLog;
import com.fitmgr.resource.api.entity.ServiceOperat;
import com.google.common.collect.Lists;
import com.google.gson.Gson;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

/**
 * 操作日志使用spring event异步入库
 *
 * @author Fitmgr
 */
@Slf4j
@Aspect
@Component
public class SysLogAspect {

    private static String HTTP_GET = "GET";
    private static String START_STR = "arg.";

    private final ApplicationEventPublisher publisher;

    /**
     * 因为要使用Gson 外加属性 故去掉AllArgsConstructor
     * 
     * @param publisher
     */
    public SysLogAspect(ApplicationEventPublisher publisher) {
        this.publisher = publisher;
    }

    @Autowired
    private Gson gson;

    /**
     * 服务注册信息
     */
    @Resource
    private Registration registration;

    @SneakyThrows
    @Around("@annotation(sysLog)")
    public Object around(ProceedingJoinPoint point, SysLog sysLog) {
        HttpServletRequest httpServletRequest = currentRequest();
        if (httpServletRequest == null) {
            return point.proceed();
        }

        if (SecurityConstants.INNER_CALL.equals(httpServletRequest.getHeader(SecurityConstants.HEADER_CALL_MODE))) {
            return point.proceed();
        }

        if (HTTP_GET.equalsIgnoreCase(httpServletRequest.getMethod())) {
            return point.proceed();
        }

        String strClassName = point.getTarget().getClass().getName();
        String strMethodName = point.getSignature().getName();
        log.debug("[类名]:{},[方法]:{}", strClassName, strMethodName);

        OperateLog logVo = SysLogUtils.getSysLog();
        logVo.setTitle(sysLog.value());
        logVo.setOperateObjType(sysLog.cloudResType());
        if (StringUtils.isNotEmpty(sysLog.resIdLocation())) {
            Object[] args = point.getArgs();
            if (args != null && args.length > 0) {
                if (sysLog.resIdLocation().startsWith(START_STR)) {
                    String jsonStr = gson.toJson(args[sysLog.resIdArgIndex()]);
                    Map<String, Object> map = gson.fromJson(jsonStr, Map.class);
                    String[] idArr = sysLog.resIdLocation().split("\\.");
                    if (map.get(idArr[1]) != null) {
                        if (!sysLog.isBatch()) {
                            if (map.get(idArr[1]) instanceof Double) {
                                logVo.setOperateObjId(String.valueOf(((Double) map.get(idArr[1])).longValue()));
                            } else {
                                logVo.setOperateObjId(String.valueOf(map.get(idArr[1])));
                            }
                        } else {
                            if (map.get(idArr[1]) instanceof List) {
                                List<String> ids = Lists.newArrayList();
                                List argIdList = (List) map.get(idArr[1]);
                                for (Object idObj : argIdList) {
                                    if (idObj instanceof Double) {
                                        ids.add(String.valueOf(((Double) idObj).longValue()));
                                    } else {
                                        ids.add(String.valueOf(idObj));
                                    }
                                }
                                logVo.setOperateObjIds(ids);
                            }
                        }
                    }
                } else {
                    if (args[sysLog.resIdArgIndex()] != null) {
                        if (args[sysLog.resIdArgIndex()] instanceof Double) {
                            logVo.setOperateObjId(String.valueOf(((Double) args[sysLog.resIdArgIndex()]).longValue()));
                        } else {
                            logVo.setOperateObjId(String.valueOf(args[sysLog.resIdArgIndex()]));
                        }
                    }
                }
            }
        }
        if (StringUtils.isNotEmpty(sysLog.resNameLocation())) {
            Object[] args = point.getArgs();
            if (args != null && args.length > 0) {
                if (sysLog.resNameLocation().startsWith(START_STR)) {
                    String jsonStr = gson.toJson(args[sysLog.resNameArgIndex()]);
                    Map<String, Object> map = gson.fromJson(jsonStr, Map.class);
                    String[] nameArr = sysLog.resNameLocation().split("\\.");
                    if (!sysLog.isBatch()) {
                        logVo.setOperateObjName(String.valueOf(map.get(nameArr[1])));
                    } else {
                        if (map.get(nameArr[1]) instanceof List) {
                            List<String> names = Lists.newArrayList();
                            List argNameList = (List) map.get(nameArr[1]);
                            for (Object nameObj : argNameList) {
                                names.add(String.valueOf(nameObj));
                            }
                            logVo.setOperateObjNames(names);
                        }
                    }
                } else {
                    logVo.setOperateObjName(String.valueOf(args[sysLog.resIdArgIndex()]));
                }
            }
        }
        logVo.setOperateType(sysLog.operateType().getName());
        logVo.setOperateLocation(sysLog.operateLocation());

        try {
            // 获取前端数据 转换成Json对象
            logVo.setParams(gson.toJson(point.getArgs()));
        } catch (UnsupportedOperationException e) {
            log.error("该接口入参无法捕捉");
        }

        // 发送异步日志事件
        Long startTime = System.currentTimeMillis();
        Object obj = point.proceed();
        Long endTime = System.currentTimeMillis();

        // 设置方法调用时间
        logVo.setTime(endTime - startTime);

        // 获取eureka客户端服务名
        logVo.setServiceId(registration.getServiceId());

        FitmgrUser user = SecurityUtils.getUser();

        if (user != null) {
            // 获取用户id
            logVo.setUserId(user.getId());

            // 获取当前租户id
            logVo.setTenantId(user.getDefaultTenantId());
        }

        // 获取方法返回值

        if (obj instanceof R) {
            R result = (R) obj;
            // 判断方法是否成功
            if (result.getCode() == CommonConstants.SUCCESS) {
                logVo.setResultCode(ResponseCodeEnum.SUCCESS.getDesc());
            } else {
                logVo.setResultCode(ResponseCodeEnum.ERROR.getDesc());
            }
        } else {
            String jsonStr = gson.toJson(obj);
            if (jsonStr != null) {
                Map<String, Object> map = gson.fromJson(jsonStr, Map.class);
                if (map != null) {

                    Object codeObj = map.get("code");
                    if (codeObj != null) {
                        if (codeObj instanceof Double) {
                            int code = ((Double) codeObj).intValue();
                            if (code == CommonConstants.SUCCESS) {
                                logVo.setResultCode(ResponseCodeEnum.SUCCESS.getDesc());
                            } else {
                                logVo.setResultCode(ResponseCodeEnum.ERROR.getDesc());
                            }
                        }
                    }
                }
            }
        }

        publisher.publishEvent(new SysLogEvent(logVo));
        return obj;
    }

    private HttpServletRequest currentRequest() {
        ServletRequestAttributes servletRequestAttributes = (ServletRequestAttributes) RequestContextHolder
                .getRequestAttributes();
        return Optional.ofNullable(servletRequestAttributes).map(ServletRequestAttributes::getRequest).orElse(null);
    }

    /**
     * 单独针对资源操作记录
     * 
     * @param logVo logVo对象
     * @param obj   返回值
     */
    private void forResource(OperateLog logVo, Object obj)
            throws ClassNotFoundException, IllegalAccessException, InstantiationException {
        if (obj instanceof R) {
            R<JSONObject> result = (R) obj;

            // 资源那边返回的是JSONObject
            JSONObject jsonObject = result.getData();

            // 从json中取出OperateLogDTO
            OperateLogDTO operateLogDTO = (OperateLogDTO) jsonObject.get("operateLogDTO");

            // 资源的操作对象
            ServiceOperat serviceOperat = (ServiceOperat) jsonObject.get("serviceOperat");

            // 是否为同步
            Integer operateType = (Integer) jsonObject.get("operateType");

            // 异步
            if (operateType == 1) {
                // 为什么要硬编码 因为对需求无力吐槽
                logVo.setResultCode("进行中");
            }

            // 操作
            String operatName = "";
            if (serviceOperat != null) {
                operatName = serviceOperat.getOperatName();
            }

            if (operateLogDTO != null) {
                logVo.setTitle(operatName);

                String formatClazz = operateLogDTO.getFormatClazz();
                if (StringUtils.isBlank(formatClazz)) {
                    logVo.setResourceFormat(BusinessEnum.RESOURCE_OPERATE_NOT_EXIST.getDescription());
                } else {
                    // 获取format 转换成class
                    Class c = Class.forName(formatClazz);

                    // 实例化
                    IFormat iFormat = (IFormat) c.newInstance();

                    // 处理format
                    String format = iFormat.format(logVo.getParams());

                    format = new StringBuilder(format).insert(0, operatName).toString();

                    log.info("format:" + format);

                    logVo.setResourceFormat(format);

                }
            }

        }

    }

}
