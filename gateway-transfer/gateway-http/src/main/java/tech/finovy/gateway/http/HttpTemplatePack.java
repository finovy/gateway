package tech.finovy.gateway.http;

import lombok.Data;
import org.springframework.web.client.RestTemplate;

@Data
public class HttpTemplatePack {

    private RestTemplate restTemplate;
    private String host;

    public HttpTemplatePack(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }
}
