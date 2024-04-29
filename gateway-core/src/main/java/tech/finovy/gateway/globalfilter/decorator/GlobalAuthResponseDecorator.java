package tech.finovy.gateway.globalfilter.decorator;


import lombok.extern.slf4j.Slf4j;
import org.apache.commons.compress.compressors.CompressorException;
import org.apache.commons.compress.compressors.CompressorInputStream;
import org.apache.commons.compress.compressors.CompressorOutputStream;
import org.apache.commons.compress.compressors.CompressorStreamFactory;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.reactivestreams.Publisher;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DataBufferFactory;
import org.springframework.core.io.buffer.DataBufferUtils;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseCookie;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.http.server.reactive.ServerHttpResponseDecorator;
import org.springframework.util.MultiValueMap;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import tech.finovy.gateway.common.constant.GlobalAuthConstant;
import tech.finovy.gateway.config.GatewayConfiguration;
import tech.finovy.gateway.globalfilter.GlobalChainContext;
import tech.finovy.gateway.globalfilter.GlobalChainEventPushHandler;
import tech.finovy.gateway.globalfilter.listener.GlobalAuthListener;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Map;

@Slf4j
public class GlobalAuthResponseDecorator extends ServerHttpResponseDecorator {

    private final GlobalChainContext authContext;
    private final ServerWebExchange exchange;
    private final GatewayConfiguration configuration;
    private final GlobalChainEventPushHandler tokenEventPushHandler;

    public GlobalAuthResponseDecorator(ServerWebExchange exchange, GatewayConfiguration configuration, GlobalChainEventPushHandler tokenEventPushHandler) {
        super(exchange.getResponse());
        this.authContext = exchange.getAttribute(GlobalChainContext.CONTEXT_KEY);
        this.exchange = exchange;
        this.tokenEventPushHandler = tokenEventPushHandler;
        this.configuration = configuration;
    }

    @Override
    public MultiValueMap<String, ResponseCookie> getCookies() {
        return super.getCookies();
    }

    @Override
    public Mono<Void> writeWith(Publisher<? extends DataBuffer> body) {
        ServerHttpResponse response = getDelegate();
        HttpHeaders responseHttpHeaders = response.getHeaders();
        MediaType responseMediaType = responseHttpHeaders.getContentType();
        authContext.setResponseContentType(responseMediaType);
        GatewayConfiguration configuration = authContext.getAuthConfiguration();
        if (!(configuration.isTraceIfContextIsNull() || responseMediaType != null)) {
            tokenEventPushHandler.responsePush(exchange, exchange.getAttribute(GlobalChainContext.CONTEXT_KEY));
            return super.writeWith(body);
        }
        if (MediaType.IMAGE_GIF.isCompatibleWith(responseMediaType)
                || MediaType.IMAGE_PNG.isCompatibleWith(responseMediaType)
                || MediaType.IMAGE_JPEG.isCompatibleWith(responseMediaType)
                || MediaType.APPLICATION_PDF.isCompatibleWith(responseMediaType)) {
            tokenEventPushHandler.responsePush(exchange, exchange.getAttribute(GlobalChainContext.CONTEXT_KEY));
            return super.writeWith(body);
        }
        Flux<? extends DataBuffer> fluxBody = Flux.from(body);
        DataBufferFactory bufferFactory = response.bufferFactory();
        Flux<DataBuffer> flux = fluxBody.buffer().map(dataBuffers -> {
            try {
                DataBuffer join = bufferFactory.join(dataBuffers);
                byte[] content = new byte[join.readableByteCount()];
                join.read(content);
                DataBufferUtils.release(join);
                authContext.setResponseBody(content);
                Map<String, GlobalAuthListener> tokenListeners = authContext.getAuthListeners();
                for (GlobalAuthListener listener : tokenListeners.values()) {
                    listener.encode(authContext);
                }
                responseHttpHeaders.setContentLength(authContext.getResponseBody().length);
            } catch (Exception e) {
                log.warn("DataBuffer error:{}", e);
            }
            return bufferFactory.wrap(authContext.getResponseBody());
        });
        return super.writeWith(flux).then(Mono.fromRunnable(() -> tokenEventPushHandler.responsePush(exchange, exchange.getAttribute(GlobalChainContext.CONTEXT_KEY))));
    }


    protected byte[] unCompresss(String responseType, byte[] response) {
        if (StringUtils.isBlank(responseType) || response == null) {
            return response;
        }
        byte[] resp = response;
        if (ArrayUtils.isEmpty(response)) {
            return response;
        }
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        ByteArrayInputStream in = new ByteArrayInputStream(resp);
        String ctype = responseType;
        if (GlobalAuthConstant.GZIP.equals(ctype)) {
            ctype = CompressorStreamFactory.GZIP;
        }
        try (CompressorInputStream gzip = new CompressorStreamFactory().createCompressorInputStream(ctype, in)) {
            byte[] buffer = new byte[1024];
            int n;
            while ((n = gzip.read(buffer)) >= 0) {
                out.write(buffer, 0, n);
            }
            return out.toByteArray();
        } catch (IOException | CompressorException e) {
            log.error("uncompress error:{}", e);
        } finally {
            try {
                out.close();
                in.close();
            } catch (IOException e) {
                log.error("Close Stream error:{}", e.toString());
            }
        }
        return response;
    }


    private byte[] compress(String ctype, byte[] resp) {
        if (StringUtils.isBlank(ctype)||ArrayUtils.isEmpty(resp)) {
            return null;
        }
        if(GlobalAuthConstant.GZIP.equals(ctype)){
            ctype=CompressorStreamFactory.GZIP;
        }
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        try (CompressorOutputStream gzip = new CompressorStreamFactory().createCompressorOutputStream(ctype, out)) {
            gzip.write(resp);
        } catch (IOException | CompressorException e) {
            log.error("compress error:{}", e.getMessage(), e);
        } finally {
            try {
                out.close();
            } catch (IOException e) {
                log.error("Close Stream error:{}", e.toString());
            }
        }
        return out.toByteArray();
    }
}

