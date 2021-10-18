package com.fitmgr.common.encrypt.config;

import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.X509Certificate;

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
import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

/**
 * @author Fitmgr
 * @since 2020-04-07
 */

@Configuration
public class InternalRestTemplateConfig {

    @Bean(name = "kmsInternalRestTemplate")
    @LoadBalanced
    public RestTemplate restTemplate() throws KeyStoreException, NoSuchAlgorithmException {

        final HttpComponentsClientHttpRequestFactory factory = new HttpComponentsClientHttpRequestFactory();
        // 设置过期时间
        factory.setConnectionRequestTimeout(5000);
        factory.setReadTimeout(5000);
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
                    new String[] { "SSLv2Hello", "SSLv3", "TLSv1", "TLSv1.2", "TLSv1.3" }, null,
                    NoopHostnameVerifier.INSTANCE);
        } catch (NoSuchAlgorithmException | KeyManagementException e) {
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
        return new RestTemplate(factory);
    }
}
