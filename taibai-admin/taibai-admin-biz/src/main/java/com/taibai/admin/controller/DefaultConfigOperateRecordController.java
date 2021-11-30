package com.taibai.admin.controller;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import org.apache.commons.collections4.CollectionUtils;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.taibai.admin.api.constants.DefaultConfigRecordStatus;
import com.taibai.admin.api.entity.DefaultConfigOperateRecord;
import com.taibai.admin.api.entity.SelfGrowId;
import com.taibai.admin.mapper.DefaultConfigOperateRecordMapper;
import com.taibai.admin.mapper.SelfGrowIdMapper;
import com.taibai.common.core.util.R;
import com.taibai.common.core.util.SpringContextHolder;
import com.taibai.common.encrypt.util.AesUtil;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@AllArgsConstructor
@RequestMapping("/default-config-operate-record")
public class DefaultConfigOperateRecordController {

    private final DefaultConfigOperateRecordMapper defaultConfigOperateRecordMapper;
    private final SelfGrowIdMapper selfGrowIdMapper;

    private static final Map<String, String> SERVICE_URL_MAP = new HashMap<>();

    static {
        SERVICE_URL_MAP.put("admin", "taibai-admin-biz");
        SERVICE_URL_MAP.put("recycle", "taibai-recycle-biz");
        SERVICE_URL_MAP.put("template", "taibai-template-biz");
        SERVICE_URL_MAP.put("resource", "taibai-resource-biz");
        SERVICE_URL_MAP.put("quota", "taibai-quota-biz");
        SERVICE_URL_MAP.put("meterage", "taibai-meterage-biz");
        SERVICE_URL_MAP.put("davinci", "taibai-dashboard-biz");
        SERVICE_URL_MAP.put("webpush", "taibai-webpush-biz");
    }

    @PostMapping
    public R addRecord(@RequestBody DefaultConfigOperateRecord defaultConfigOperateRecord) {
        defaultConfigOperateRecord.setCreateTime(LocalDateTime.now());
        defaultConfigOperateRecord.setUuid(UUID.randomUUID().toString());
        defaultConfigOperateRecordMapper.insert(defaultConfigOperateRecord);
        return R.ok();
    }

    @GetMapping
    public R importConfigOperate() {

        log.info("start importConfigOperate");
        RestTemplate restTemplate = SpringContextHolder.getBean("SecurityInternalRestTemplate");
        int page = 1;
        int rows = 100;

        IPage<DefaultConfigOperateRecord> iPage = new Page<>(page, rows);
        IPage<DefaultConfigOperateRecord> recordiPage = defaultConfigOperateRecordMapper.selectPage(iPage,
                new QueryWrapper<DefaultConfigOperateRecord>().lambda()
                        .eq(DefaultConfigOperateRecord::getStatus, DefaultConfigRecordStatus.NEW.name())
                        .orderByAsc(DefaultConfigOperateRecord::getId));
        List<DefaultConfigOperateRecord> defaultConfigOperateRecords = recordiPage.getRecords();
        while (CollectionUtils.isNotEmpty(defaultConfigOperateRecords)) {
            log.info("defaultConfigOperateRecords.size()={}", defaultConfigOperateRecords.size());
            for (DefaultConfigOperateRecord defaultConfigOperateRecord : defaultConfigOperateRecords) {
                log.info("start execute {}", defaultConfigOperateRecord.getUuid());
                String[] urlArr = StringUtils.tokenizeToStringArray(defaultConfigOperateRecord.getHttpUrl(), "/");
                String newPath = "/" + Arrays.stream(urlArr).skip(1L).collect(Collectors.joining("/"));
                newPath = "http://" + SERVICE_URL_MAP.get(urlArr[0]) + newPath;
                try {
                    if ("POST".equalsIgnoreCase(defaultConfigOperateRecord.getHttpMethod())) {
                        HttpHeaders headers = getHttpHeaders();
                        HttpEntity<String> httpEntity = new HttpEntity<>(defaultConfigOperateRecord.getRequestBody(),
                                headers);
                        ResponseEntity<Map> responseEntity = restTemplate.exchange(newPath, HttpMethod.POST, httpEntity,
                                Map.class);
                        processResult(defaultConfigOperateRecord, responseEntity);
                    } else if ("PUT".equalsIgnoreCase(defaultConfigOperateRecord.getHttpMethod())) {
                        HttpHeaders headers = getHttpHeaders();
                        HttpEntity<String> httpEntity = new HttpEntity<>(defaultConfigOperateRecord.getRequestBody(),
                                headers);
                        ResponseEntity<Map> responseEntity = restTemplate.exchange(newPath, HttpMethod.PUT, httpEntity,
                                Map.class);
                        processResult(defaultConfigOperateRecord, responseEntity);
                    } else if ("DELETE".equalsIgnoreCase(defaultConfigOperateRecord.getHttpMethod())) {
                        HttpHeaders headers = getHttpHeaders();
                        HttpEntity httpEntity = new HttpEntity<>(headers);
                        ResponseEntity<Map> responseEntity = restTemplate.exchange(newPath, HttpMethod.DELETE,
                                httpEntity, Map.class);
                        processResult(defaultConfigOperateRecord, responseEntity);
                    }
                } catch (Throwable th) {
                    log.error("fail record uuid={}", defaultConfigOperateRecord.getUuid(), th);
                    return R.failed("执行" + defaultConfigOperateRecord.getUuid() + "失败");
                }
                log.info("end execute {}", defaultConfigOperateRecord.getUuid());
            }

            iPage.setCurrent(++page);
            recordiPage = defaultConfigOperateRecordMapper.selectPage(iPage,
                    new QueryWrapper<DefaultConfigOperateRecord>().lambda()
                            .orderByAsc(DefaultConfigOperateRecord::getCreateTime));
            defaultConfigOperateRecords = recordiPage.getRecords();
        }
        log.info("end importConfigOperate");
        return R.ok();
    }

    private void processResult(DefaultConfigOperateRecord defaultConfigOperateRecord,
            ResponseEntity<Map> responseEntity) {
        Map<String, Object> map = responseEntity.getBody();
        Integer code = (Integer) map.get("code");
        if (!(code != null && code.equals(0))) {
            log.error("exec config fail. record uuid={}", defaultConfigOperateRecord.getUuid());
            throw new RuntimeException("exec config fail");
        } else {
            defaultConfigOperateRecord.setStatus(DefaultConfigRecordStatus.SUCCESS.name());
            defaultConfigOperateRecordMapper.update(defaultConfigOperateRecord,
                    new QueryWrapper<DefaultConfigOperateRecord>().lambda().eq(DefaultConfigOperateRecord::getUuid,
                            defaultConfigOperateRecord.getUuid()));
        }
    }

    private HttpHeaders getHttpHeaders() {
        HttpHeaders headers = new HttpHeaders();
        String token = AesUtil.createInternalAdminToken();
        headers.add("Authorization", "Bearer " + token);
        headers.add("Content-Type", "application/json");
        return headers;
    }

    @GetMapping("/extend/primaryId")
    public R genPrimaryId() {
        SelfGrowId selfGrowId = new SelfGrowId();
        selfGrowId.setExtend1("self");
        selfGrowIdMapper.insert(selfGrowId);
        return R.ok(selfGrowId.getId());
    }
}
