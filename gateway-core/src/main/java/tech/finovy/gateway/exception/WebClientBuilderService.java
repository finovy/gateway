package tech.finovy.gateway.exception;

import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.client.loadbalancer.reactive.ReactorLoadBalancerExchangeFilterFunction;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpHeaders;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.server.ServerResponse;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;
import tech.finovy.gateway.common.constant.Constant;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
public class WebClientBuilderService {
    @Autowired
    private ReactorLoadBalancerExchangeFilterFunction reactorLoadBalancerExchangeFilterFunction;

    public WebClient.RequestBodyUriSpec choice(ServerWebExchange exchange, String url) {
        ServerHttpRequest request = exchange.getRequest();
        String host = url.toLowerCase();
        while (host.endsWith("/")) {
            host = host.substring(0, host.length() - 1);
        }
        if (host.startsWith(Constant.LB) || host.startsWith(Constant.LBS)) {
            return WebClient.builder().filter(reactorLoadBalancerExchangeFilterFunction).baseUrl(host).build().method(request.getMethod());
        }
        return WebClient.create(url).method(request.getMethod());
    }

    public Mono<ServerResponse> choiceAction(ServerWebExchange exchange, String url, String traceId, String routeId) {
        ServerHttpRequest request = exchange.getRequest();
        HttpHeaders hs = degradeHttpHeaders(request, traceId, routeId);
        return choice(exchange, url)
                .uri(f -> f.path(request.getPath().value()).queryParams(request.getQueryParams()).build())
                .headers(newHeaders -> newHeaders.putAll(hs))
                .body(request.getBody(), DataBuffer.class)
                .exchange()
                .flatMap(this::monoServerResponse);
    }

    private Mono<ServerResponse> monoServerResponse(ClientResponse mapper) {
        MDC.clear();
        return ServerResponse.status(mapper.statusCode())
                .headers(c -> c.putAll(mapper.headers().asHttpHeaders()))
                .body(mapper.bodyToFlux(DataBuffer.class), DataBuffer.class);
    }

    private HttpHeaders degradeHttpHeaders(ServerHttpRequest request, String traceId, String routeId) {
        HttpHeaders header = request.getHeaders();
        HttpHeaders hs = new HttpHeaders();
        for (Map.Entry<String, List<String>> eache : header.entrySet()) {
            List<String> headerList = eache.getValue();
            if (headerList == null) {
                continue;
            }
            hs.put(eache.getKey(), headerList);
        }
        List<String> headerList = new ArrayList<>();
        headerList.add(Constant.DEGRADE_AGENT);
        hs.put(HttpHeaders.USER_AGENT, headerList);
        headerList = new ArrayList<>();
        headerList.add(traceId);
        hs.put(Constant.TRACE_ID, headerList);
        headerList = new ArrayList<>();
        headerList.add(routeId);
        hs.put(Constant.ROUTE_ID, headerList);
        return hs;
    }
}
