package tech.finovy.gateway.config.nacos.listener;

public abstract class AbstractSchedulerRefreshListener implements SchedulerRefreshListener {
    protected String index;

    @Override
    public void startup() {

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
        return "AbstractConfigurationRefreshListener";
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
