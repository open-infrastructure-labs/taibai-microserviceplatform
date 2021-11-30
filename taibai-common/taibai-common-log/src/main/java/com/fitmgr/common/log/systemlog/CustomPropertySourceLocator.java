package com.taibai.common.log.systemlog;

import com.taibai.common.core.constant.CommonConstants;
import com.taibai.common.core.util.EnvIsoUtil;
import com.taibai.common.core.util.OSInfo;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.X509Certificate;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.apache.http.config.Registry;
import org.apache.http.config.RegistryBuilder;
import org.apache.http.conn.socket.ConnectionSocketFactory;
import org.apache.http.conn.socket.PlainConnectionSocketFactory;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.ssl.SSLContextBuilder;
import org.springframework.cloud.bootstrap.config.PropertySourceLocator;
import org.springframework.core.env.Environment;
import org.springframework.core.env.MapPropertySource;
import org.springframework.core.env.PropertySource;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

import com.taibai.common.core.constant.CommonConstants;
import com.taibai.common.core.util.EnvIsoUtil;

public class CustomPropertySourceLocator implements PropertySourceLocator {

    @Override
    public PropertySource<?> locate(Environment environment) {
        Map<String, Object> source = new HashMap<>();

        source.put("configserver.discovered.uri", "http://taibai-config:8888");

        String hostIP = queryHostIp();
        String fenghuoEnv = EnvIsoUtil.getSysEnv();

        if(OSInfo.isWindows() || OSInfo.isMacOS() || OSInfo.isMacOSX()) {
            if(StringUtils.isEmpty(hostIP) || StringUtils.isEmpty(fenghuoEnv)) {
                throw new RuntimeException("本地启动需要配置register_ip_address和fenghuo_env");
            }
        }
        if (StringUtils.isNotEmpty(hostIP)) {
            System.out.println("hostIP=" + hostIP);
            source.put("eureka.instance.ip-address", hostIP);
        }

        if (StringUtils.isNotEmpty(fenghuoEnv)) {
            System.out.println("fenghuo-env=" + fenghuoEnv);
            Map<String, String> metaMap = new HashMap<>();
            metaMap.put(CommonConstants.ENV_HEADER_ENV_KEY, fenghuoEnv);
            source.put("eureka.instance.metadata-map", metaMap);
        }
        return new MapPropertySource("customProperty", source);
    }

    private String queryHostIp() {
        return System.getenv("register_ip_address");
    }

    public RestTemplate restTemplate() throws KeyStoreException, NoSuchAlgorithmException {

        final HttpComponentsClientHttpRequestFactory factory = new HttpComponentsClientHttpRequestFactory();
        // 设置过期时间
        factory.setConnectionRequestTimeout(100000);
        factory.setReadTimeout(100000);
        final SSLContextBuilder builder = new SSLContextBuilder();
        try {
            // 全部信任 不做身份鉴定
            builder.loadTrustMaterial(null, (X509Certificate[] x509Certificate, String s) -> true);
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            throw e;
        } catch (KeyStoreException e) {
            throw e;
        }
        SSLConnectionSocketFactory socketFactory = null;
        try {
            // 客户端支持SSLv2Hello，SSLv3,TLSv1，TLSv1
            socketFactory = new SSLConnectionSocketFactory(builder.build(),
                    new String[] { "SSLv2Hello", "SSLv3", "TLSv1", "TLSv1.2" }, null, NoopHostnameVerifier.INSTANCE);
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (KeyManagementException e) {
            e.printStackTrace();
        }
        // 为自定义连接器注册http与https
        Registry<ConnectionSocketFactory> registry = RegistryBuilder.<ConnectionSocketFactory>create()
                .register("http", new PlainConnectionSocketFactory()).register("https", socketFactory).build();
        PoolingHttpClientConnectionManager poolingHttpClientConnectionManager = new PoolingHttpClientConnectionManager(
                registry);
        poolingHttpClientConnectionManager.setMaxTotal(500);
        final CloseableHttpClient httpClient = HttpClients.custom().setSSLSocketFactory(socketFactory)
                .setConnectionManager(poolingHttpClientConnectionManager).setConnectionManagerShared(true).build();
        factory.setHttpClient(httpClient);
        final RestTemplate restTemplate = new RestTemplate(factory);
        return restTemplate;
    }
}
