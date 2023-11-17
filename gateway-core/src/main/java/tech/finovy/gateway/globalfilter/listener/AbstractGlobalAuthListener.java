package tech.finovy.gateway.globalfilter.listener;


import tech.finovy.framework.common.core.chain.AbstractChainListener;
import tech.finovy.gateway.common.constant.GlobalAuthConstant;
import tech.finovy.gateway.globalfilter.GlobalChainContext;

public abstract class AbstractGlobalAuthListener extends AbstractChainListener implements GlobalAuthListener {
    @Override
    public String getType() {
        return GlobalAuthConstant.AUTH_LISTENER_TYPE;
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
