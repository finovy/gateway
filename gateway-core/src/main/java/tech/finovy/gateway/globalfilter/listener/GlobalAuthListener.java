package tech.finovy.gateway.globalfilter.listener;

import tech.finovy.gateway.common.chain.ChainListener;
import tech.finovy.gateway.globalfilter.GlobalChainContext;

public interface GlobalAuthListener extends ChainListener {
    boolean refreshToken(GlobalChainContext context);

    void refresh(GlobalChainContext context);

    void encode(GlobalChainContext context);

    void decode(GlobalChainContext context);
}
