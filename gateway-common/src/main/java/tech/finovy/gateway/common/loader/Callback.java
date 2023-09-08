package tech.finovy.gateway.common.loader;

public interface Callback<T> {

    /**
     * Execute t.
     *
     * @return the t
     * @throws Throwable the throwable
     */
    T execute() throws Throwable;
}

