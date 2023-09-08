package tech.finovy.gateway.http;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.client.RestTemplate;

import java.util.Arrays;


@Slf4j
@Configuration
public class RestTemplateConfig {
    @Value("${rest-template.connect-timeout:600000}")
    private int executorConnectTimeout;
    @Value("${rest-template.read-timeout:20000}")
    private int executorReadTimeout;

    @LoadBalanced
    @Bean("loadbalanceRestTemplate")
    public RestTemplate loadbalanceRestTemplate() {
        return createRestTemplate();
    }


    @Bean("httpRestTemplate")
    public RestTemplate httpRestTemplate() {
        return createRestTemplate();
    }

    private RestTemplate createRestTemplate() {
//        PoolingHttpClientConnectionManager connectionManager = new PoolingHttpClientConnectionManager();
//        connectionManager.setMaxTotal(200);
//        connectionManager.setValidateAfterInactivity(TimeValue.ofMicroseconds(5000));
//        connectionManager.setDefaultMaxPerRoute(100);
//        connectionManager.closeIdle(TimeValue.ofSeconds(30));
//        HttpClient httpClient = HttpClientBuilder.create()
//                .setConnectionManager(connectionManager)
//                .setConnectionReuseStrategy(DefaultConnectionReuseStrategy.INSTANCE)
//                .setDefaultRequestConfig(RequestConfig.custom()
//                        .setConnectTimeout(Timeout.ofMicroseconds(executorConnectTimeout))
//                        .build())
//                .build();
//        HttpComponentsClientHttpRequestFactory factory = new HttpComponentsClientHttpRequestFactory();
//        factory.setHttpClient(httpClient);
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(executorConnectTimeout);
        factory.setReadTimeout(executorReadTimeout);
        RestTemplate restTemplate = new RestTemplate();
        restTemplate.setRequestFactory(factory);
        MappingJackson2HttpMessageConverter mappingJackson2HttpMessageConverter = new MappingJackson2HttpMessageConverter();
        mappingJackson2HttpMessageConverter.setSupportedMediaTypes(Arrays.asList(
                MediaType.APPLICATION_JSON,
                MediaType.APPLICATION_FORM_URLENCODED,
                MediaType.APPLICATION_XML,
                MediaType.TEXT_HTML,
                MediaType.TEXT_PLAIN));
        restTemplate.getMessageConverters().add(mappingJackson2HttpMessageConverter);
        return restTemplate;
    }
}
