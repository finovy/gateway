package tech.finovy.gateway.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.annotation.Configuration;
import tech.finovy.gateway.common.util.ConversionUtil;
import tech.finovy.gateway.common.constant.GlobalAuthConstant;

@Getter
@RefreshScope
@Configuration
public class GatewayConfiguration {
    @Value("${spring.application.name:gateway}")
    private String applicationName;

    @Value("${gateway.chain.skip:false}")
    private boolean chainSkip;
    @Value("${gateway.chain.ignore-delete-and-get-body:false}")
    private boolean ignoreDeleteAndGetMethodBoby = false;
    @Value("${gateway.auth.enable:false}")
    private boolean authEnable;
    @Value("${gateway.auth.skip-refresh:true}")
    private boolean skipRefresh;
    @Value("${gateway.auth.host.validator-host:false}")
    private boolean validatorHost;
    @Value("${gateway.auth.default-appid:}")
    private String defaultApppId;
    @Value("${gateway.auth.skip-scheme:ws,wss}")
    private String[] skipAuthScheme;
    @Value("${gateway.auth.skip-extend:.js,.ico,.map,.css,.svg,.png,.ttf}")
    private String[] skipAuthExtend;
    @Value("${gateway.auth.token.validator-token:false}")
    private boolean validatorToken;
    @Value("${gateway.auth.token.message.not-exists:token not exists!}")
    private String tokenNotExistsMessage;
    @Value("${gateway.auth.token.message.invalidate:token invalidate}")
    private String tokenInvalidateMessage;
    @Value("${gateway.auth.token.message.not-exists-code:2000}")
    private int tokenNotExistsCode;
    @Value("${gateway.auth.token.message.invalidate-code:2001}")
    private int tokenInvalidateCode;
    @Value("${gateway.auth.token.message.forbidden-code:403}")
    private int forbiddenCode;
    @Value("${gateway.auth.token.header-key:X-Auth-Token}")
    private String headerTokenKey;
    @Value("${gateway.auth.token.query-key:token}")
    private String queryTokenKey;
    @Value("${gateway.auth.token.out-put-key:X-Auth-Token}")
    private String outPutTokenKey;


    @Value("${gateway.auth.app.header-key:X-Auth-AppId}")
    private String headerAppKey;
    @Value("${gateway.auth.app.query-key:appid}")
    private String queryAppKey;
    @Value("${gateway.auth.app.out-put-key:X-Auth-AppId}")
    private String outPutAppKey;

    @Value("${gateway.auth.order:-1}")
    private int authFilterOrder;

    @Value("${gateway.trace.enable:true}")
    private boolean enableTrace;
    @Value("${gateway.trace.trace-id-prefix:TX}")
    private String traceIdPrefix;
    @Value("${gateway.trace.trace-id-append:-}")
    private String traceIdAppend;
    @Value("${gateway.trace.trace-id-header-key:x-trace-id}")
    private String traceHeaderKey;
    @Value("${gateway.trace.sw8-trace-id-header-key:sw8}")
    private String sw8TraceHeaderKey;
    @Value("${gateway.trace.trace-id-query-key:x-trace-id}")
    private String traceQueryKey;
    @Value("${gateway.trace.sw8-trace-id-query-key:sw8}")
    private String sw8TraceQueryKey;
    @Value("${gateway.trace.trace-if-context-is-null:true}")
    private boolean traceIfContextIsNull;
    @Value("${gateway.trace.debug:false}")
    private boolean traceDebug;

    @Setter
    private String startupTimeMillis;

    public String getStartupTimeMillis() {
        if (startupTimeMillis == null) {
            long i = System.currentTimeMillis() - GlobalAuthConstant.STARTUP_TIME_MILLIS;
            startupTimeMillis = ConversionUtil.encode(i, 36);
        }
        return startupTimeMillis;
    }
}
