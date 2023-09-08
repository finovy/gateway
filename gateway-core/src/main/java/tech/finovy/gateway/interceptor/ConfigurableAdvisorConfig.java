package tech.finovy.gateway.interceptor;

import lombok.extern.slf4j.Slf4j;
import org.springframework.aop.aspectj.AspectJExpressionPointcutAdvisor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Slf4j
@Import({DispatcherHandlerMethodInterceptor.class})
@Configuration
public class ConfigurableAdvisorConfig {
    private static final String DISPATCHER_HANDLER_POINTCUT = "execution(public * org.springframework.web.reactive.DispatcherHandler.handle(..))";

    @Bean
    public AspectJExpressionPointcutAdvisor build(DispatcherHandlerMethodInterceptor dispatcherHandlerMethodInterceptor) {
        AspectJExpressionPointcutAdvisor advisor = new AspectJExpressionPointcutAdvisor();
        advisor.setExpression(DISPATCHER_HANDLER_POINTCUT);
        advisor.setAdvice(dispatcherHandlerMethodInterceptor);
        return advisor;
    }
}
