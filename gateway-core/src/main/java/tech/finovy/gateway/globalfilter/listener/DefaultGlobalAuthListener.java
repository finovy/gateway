package tech.finovy.gateway.globalfilter.listener;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import tech.finovy.gateway.common.constant.GlobalAuthConstant;
import tech.finovy.gateway.globalfilter.GlobalChainContext;

@Slf4j
@Component
public class DefaultGlobalAuthListener extends AbstractGlobalAuthListener {

    @Override
    public String getType() {
        return GlobalAuthConstant.DEFAULT_TOKEN_LISTENER_TYPE;
    }

    @Override
    public boolean refreshToken(GlobalChainContext context) {

        return !context.isForbidden();
    }

    @Override
    public void refresh(GlobalChainContext context) {

    }

    @Override
    public void encode(GlobalChainContext context) {

    }

    @Override
    public void decode(GlobalChainContext context) {

    }
}
