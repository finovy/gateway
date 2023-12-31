package tech.finovy.gateway.globalfilter.listener;

import tech.finovy.framework.common.core.chain.ChainListener;
import tech.finovy.gateway.globalfilter.GlobalChainContext;

public interface GlobalAuthListener extends ChainListener {
    boolean refreshToken(GlobalChainContext context);

    void refresh(GlobalChainContext context);

    void encode(GlobalChainContext context);

    void decode(GlobalChainContext context);
}
