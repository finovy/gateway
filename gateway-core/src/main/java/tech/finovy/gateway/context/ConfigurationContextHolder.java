package tech.finovy.gateway.context;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ConfigurationContextHolder {

    private static final Logger LOGGER = LoggerFactory.getLogger(ConfigurationContext.class);

    private ConfigurationContextHolder() {

    }

    public static ConfigurationContext get() {
        return SingletonHolder.INSTANCE;
    }

    /**
     * Set a TM instance.
     *
     * @param mock commonly used for test mocking
     */
    public static void set(ConfigurationContext mock) {
        SingletonHolder.INSTANCE = mock;
    }

    private static class SingletonHolder {

        private static ConfigurationContext INSTANCE = null;

        static {
            try {
                INSTANCE = new ConfigurationContext();
                LOGGER.info("ConfigurationContext Singleton {}", INSTANCE);
            } catch (Throwable anyEx) {
                LOGGER.error("Failed to load ConfigurationContext Singleton! ", anyEx);
            }
        }
    }
}
