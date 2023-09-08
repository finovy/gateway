package tech.finovy.gateway.config.nacos.listener;

public abstract class AbstractStartupRefreshListener implements SchedulerRefreshListener {
    protected String index;

    @Override
    public void trigger(long trigger) {

    }

    @Override
    public boolean isAsync() {
        return false;
    }

    @Override
    public int getOrder() {
        return 0;
    }

    @Override
    public String getType() {
        return "AbstractStartupRefreshListener";
    }

    @Override
    public String getKey() {
        return getClass().getSimpleName();
    }

    @Override
    public String getIndex() {
        return index;
    }

    @Override
    public void setIndex(String index) {
        this.index = index;
    }

    @Override
    public boolean isEnable() {
        return true;
    }

    @Override
    public void init() {

    }
}
