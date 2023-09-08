package tech.finovy.gateway.common.chain;


public interface ChainListener {
    int getOrder();

    String getType();

    String getKey();

    String getIndex();

    void setIndex(String index);

    boolean isEnable();
    boolean isAsync();

    void init();
}
