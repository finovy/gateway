package tech.finovy.gateway.common.chain;

import org.apache.commons.lang3.StringUtils;

public abstract class AbstractChainListener implements ChainListener {
    protected int order;
    protected String key;
    protected String index;

    @Override
    public void init() {

    }

    @Override
    public boolean isAsync() {
        return false;
    }

    @Override
    public boolean isEnable() {
        return true;
    }

    @Override
    public int getOrder() {
        return order;
    }

    @Override
    public String getKey() {
        if (StringUtils.isNotBlank(key)) {
            return key;
        }
        return StringUtils.uncapitalize(this.getClass().getSimpleName());
    }

    @Override
    public String getIndex() {
        return index;
    }

    @Override
    public void setIndex(String index) {
        this.index = index;
    }
}
