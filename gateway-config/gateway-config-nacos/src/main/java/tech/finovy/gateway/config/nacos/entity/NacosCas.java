package tech.finovy.gateway.config.nacos.entity;

public class NacosCas {
    private String context;
    private String casMd5;

    public NacosCas() {
    }

    public NacosCas(String context, String casMd5) {
        this.context = context;
        this.casMd5 = casMd5;
    }

    public String getContext() {
        return context;
    }

    public void setContext(String context) {
        this.context = context;
    }

    public String getCasMd5() {
        return casMd5;
    }

    public void setCasMd5(String casMd5) {
        this.casMd5 = casMd5;
    }
}
