package tech.finovy.gateway.config;

import org.apache.commons.lang3.StringUtils;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;


@Component
@ConditionalOnProperty(name = "gateway.api-doc.enable", havingValue = "true")
public class SwaggerHeaderFilter extends AbstractGatewayFilterFactory {
    private static final String HEADER_NAME = "X-Forwarded-Prefix";

    private static final String URI = "/v2/api-docs";

    @Override
    public GatewayFilter apply(Object config) {
        return (exchange, chain) -> {
            ServerHttpRequest request = exchange.getRequest();
            String path = request.getURI().getPath();
            if (!StringUtils.endsWithIgnoreCase(path, URI)) {
                return chain.filter(exchange);
            }
            // dev环境使用独立路由维护,需要去掉对应的docs前缀,才能拿到对应的真实路径
            if (StringUtils.isNotBlank(path) && path.length() > 5 && path.startsWith("/docs")) {
                path = path.replaceFirst("/docs", "");
            }
            String basePath = path.substring(0, path.lastIndexOf(URI));
            if (StringUtils.isNotBlank(basePath)) {
                ServerHttpRequest newRequest = request.mutate().header(HEADER_NAME, basePath).build();
                ServerWebExchange newExchange = exchange.mutate().request(newRequest).build();
                return chain.filter(newExchange);
            }
            return chain.filter(exchange);
        };
    }
}
