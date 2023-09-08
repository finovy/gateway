package tech.finovy.gateway.http;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class HttpTemplateService {

    @Qualifier("loadbalanceRestTemplate")
    @Autowired
    private RestTemplate loadbalanceRestTemplate;
    @Qualifier("httpRestTemplate")
    @Autowired
    private RestTemplate restTemplate;
    @Value("${rest-template.enable-http:true}")
    private boolean enableHttp;

    public HttpTemplatePack choice(String url) {
        HttpTemplatePack pack = new HttpTemplatePack(loadbalanceRestTemplate);
        if (enableHttp) {
            pack.setRestTemplate(restTemplate);
        }
        String host = url.toLowerCase();
        pack.setHost(host);
        while (host.endsWith("/")) {
            host = host.substring(0, host.length() - 1);
        }
        if (host.startsWith(Constant.LB) || host.startsWith(Constant.LBS)) {
            host = host.replaceFirst(Constant.LB, Constant.HTTP);
            host = host.replaceFirst(Constant.LBS, Constant.HTTPS);
            pack.setHost(host);
            pack.setRestTemplate(loadbalanceRestTemplate);
            return pack;
        }
        return pack;
    }

}
