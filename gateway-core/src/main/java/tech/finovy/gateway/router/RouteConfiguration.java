package tech.finovy.gateway.router;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.annotation.Configuration;

@Getter
@RefreshScope
@Configuration
public class RouteConfiguration {
    @Value("${spring.application.name:framework-gateway}")
    private String applicationName;
    @Value("${gateway.route.data-id.route:gateway-routes.json}")
    private String routeDataId;
    @Value("${gateway.route.data-id.host:gateway-host.json}")
    private String hostDataId;
    @Value("${gateway.route.data-id.skip-url:gateway-skip-url.json}")
    private String skipUrlDataId;
    @Value("${gateway.route.data-group:DEFAULT_GROUP}")
    private String routeDataGroup;
    @Value("${gateway.route.debug:false}")
    private boolean debug;
}
