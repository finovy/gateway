package tech.finovy.gateway.config;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.annotation.Configuration;

@Getter
@RefreshScope
@Configuration
public class ExceptionConfiguration {
    @Value("${spring.application.name:gateway}")
    private String applicationName;
    @Value("${gateway.exception.page-not-found.message:Page_Not_Found}")
    private String pageNotFoundMessage;
    @Value("${gateway.exception.page-not-found.status-code:404}")
    private int statusCodeNotFound;

    @Value("${gateway.exception.internal-server-error.message:Internal_Server_Error}")
    private String internalServerError;
    @Value("${gateway.exception.internal-server-error.status-code:500}")
    private int statusCodeInternalServerError;
    @Value("${gateway.exception.throw-error-to-page:false}")
    private boolean throwErrorToPage;
    @Value("${gateway.event.enable:true}")
    private boolean eventEnable;

    @Value("${gateway.degrade.message:Too_Many_Requests}")
    private String degradeMessage;
    @Value("${gateway.degrade.status-code:429}")
    private int degradeCode;
    @Value("${gateway.degrade.data-id:gateway-degrade.json}")
    private String degradeDataId;
    @Value("${gateway.degrade.data-group:DEFAULT_GROUP}")
    private String degradeDataGroup;
}
