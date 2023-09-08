package tech.finovy.gateway.globalfilter;


import org.apache.commons.lang3.StringUtils;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import tech.finovy.gateway.common.configuration.GatewayConfiguration;
import tech.finovy.gateway.common.entity.HostItemEntity;
import tech.finovy.gateway.common.entity.SkipUrlItemEntity;
import tech.finovy.gateway.globalfilter.listener.GlobalAuthListener;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;

public class GlobalChainContext {
    public static final String CONTEXT_KEY = "GW_GlobalContext";

    private final String traceId;
    private final AtomicReference<HttpHeaders> headers = new AtomicReference<>();
    private final GatewayConfiguration authConfiguration;
    private final SkipUrlItemEntity urlItem;
    private final HostItemEntity hostItem;
    private final Set<String> skipHeaders;
    private final Map<String, GlobalAuthListener> authListeners;
    private final MediaType requestContentType;
    private final long startTime = System.currentTimeMillis();
    private byte[] responseBody;
    private byte[] requestBody;
    private boolean forbidden = false;
    private String authMessage;
    private int authCode;
    private MediaType responseContentType;
    private String token;
    private volatile String filterType;
    private String responseCompresssType;
    private String acceptCompresssType;
    private long spends = 0;
    private boolean skipScheme;
    private boolean skipExtend;
    private boolean skipAuth;
    private boolean skipRefreshListener;
    private boolean authEnable;
    private boolean forbiddenHost;
    private boolean validatorToken;
    private final long requestCount;

    public GlobalChainContext(long requestCount, String traceId, HttpHeaders httpHeaders, GatewayConfiguration authConfiguration, MediaType requestContentType, HostItemEntity hostItem, SkipUrlItemEntity urlItem, Set<String> skipHeaders, Map<String, GlobalAuthListener> authListeners) {
        this.traceId = traceId;
        this.authMessage = authConfiguration.getTokenNotExistsMessage();
        this.authCode = authConfiguration.getTokenNotExistsCode();
        this.authConfiguration = authConfiguration;
        this.hostItem = hostItem;
        HttpHeaders hs = new HttpHeaders();
        this.headers.set(hs);
        hs.addAll(httpHeaders);
        if(skipHeaders!=null){
            for(String key:skipHeaders){
                hs.remove(key);
            }
        }
        this.skipHeaders=skipHeaders;
        this.urlItem = urlItem;
        this.requestContentType = requestContentType;
        this.authListeners = authListeners;
        this.requestCount = requestCount;
    }

    public long getRequestCount() {
        return requestCount;
    }

    public void putHeader(String key, String value) {
        headers.get().add(key, value);
    }

    public void replaceHeader(String key, String value) {
        if (StringUtils.isBlank(value) || StringUtils.isBlank(key)) {
            return;
        }
        HttpHeaders h = headers.get();
        if(h.containsKey(key)){
            h.replace(key, List.of(value));
        }else {
            h.add(key,value);
        }
    }

    public void removeHeader(String key) {
        headers.get().remove(key);
    }

    public long getSpendsTime() {
        if (spends == 0) {
            spends = System.currentTimeMillis() - startTime;
        }
        return spends;
    }

    public String getTraceId() {
        return traceId;
    }

    public HttpHeaders getHeaders() {
        HttpHeaders httpHeaders = headers.get();
        if (authConfiguration.isEnableTrace() || traceId != null) {
            httpHeaders.remove(authConfiguration.getTraceHeaderKey());
            httpHeaders.add(authConfiguration.getTraceHeaderKey(), traceId);
        }
        return httpHeaders;
    }

    public GatewayConfiguration getAuthConfiguration() {
        return authConfiguration;
    }

    public SkipUrlItemEntity getUrlItem() {
        return urlItem;
    }

    public Map<String, GlobalAuthListener> getAuthListeners() {
        return authListeners;
    }

    public boolean isForbidden() {
        return forbidden;
    }

    public void setForbidden(boolean forbidden) {
        this.forbidden = forbidden;
    }

    public String getAuthMessage() {
        return authMessage;
    }

    public void setAuthMessage(String authMessage) {
        this.authMessage = authMessage;
    }

    public int getAuthCode() {
        return authCode;
    }

    public void setAuthCode(int authCode) {
        this.authCode = authCode;
    }

    public MediaType getRequestContentType() {
        return requestContentType;
    }

    public MediaType getResponseContentType() {
        return responseContentType;
    }

    public void setResponseContentType(MediaType responseContentType) {
        this.responseContentType = responseContentType;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getFilterType() {
        return filterType;
    }

    public void setFilterType(String filterType) {
        this.filterType = filterType;
    }

    public byte[] getResponseBody() {
        return responseBody;
    }

    public void setResponseBody(byte[] responseBody) {
        this.responseBody = responseBody;
    }

    public String getResponseCompresssorType() {
        return responseCompresssType;
    }

    public void setResponseCompresssorType(String responseCompresssType) {
        this.responseCompresssType = responseCompresssType;
    }

    public String getAcceptCompresssorType() {
        return acceptCompresssType;
    }

    public void setAcceptCompresssorType(String requestCompresssType) {
        this.acceptCompresssType = requestCompresssType;
    }

    public HostItemEntity getHostItem() {
        return hostItem;
    }


    public byte[] getRequestBody() {
        return requestBody;
    }

    public void setRequestBody(byte[] requestBody) {
        this.requestBody = requestBody;
    }

    public boolean isSkipScheme() {
        return skipScheme;
    }

    public void setSkipScheme(boolean skipScheme) {
        this.skipScheme = skipScheme;
    }

    public void setSkipExtend(boolean skipExtend) {
        this.skipExtend = skipExtend;
    }

    public void setSkipAuth(boolean skipAuth) {
        this.skipAuth = skipAuth;
    }

    public void setAuthEnable(boolean authEnable) {
        this.authEnable = authEnable;
    }

    public boolean isForbiddenHost() {
        return forbiddenHost;
    }

    public void setForbiddenHost(boolean forbiddenHost) {
        this.forbiddenHost = forbiddenHost;
    }

    public boolean isValidatorToken() {
        return validatorToken;
    }

    public void setValidatorToken(boolean validatorToken) {
        this.validatorToken = validatorToken;
    }

    public boolean isSkipRefreshListener() {
        return skipRefreshListener;
    }

    public void setSkipRefreshListener(boolean skipRefreshListener) {
        this.skipRefreshListener = skipRefreshListener;
    }

    public boolean isSkip() {
        return !authEnable || skipScheme || skipExtend || skipAuth;
    }
}
