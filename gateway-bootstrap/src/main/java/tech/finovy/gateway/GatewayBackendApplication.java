package tech.finovy.gateway;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.annotation.Bean;
import org.springframework.http.codec.ServerCodecConfigurer;
import reactor.netty.ReactorNetty;

@EnableDiscoveryClient
@SpringBootApplication(scanBasePackages = "tech.finovy.gateway")
public class GatewayBackendApplication {

    public static void main(String[] args) {
        System.setProperty(ReactorNetty.IO_SELECT_COUNT, "1");
        SpringApplication.run(GatewayBackendApplication.class, args);
    }
}
