package tech.finovy.gateway.common.chain;


import lombok.extern.slf4j.Slf4j;
import tech.finovy.gateway.common.exception.DistributedListenerException;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Slf4j
public class ChainSortUtil {
    public static boolean debug=false;
    private ChainSortUtil() {
        throw new IllegalStateException("Utility class");
    }
    public static <T extends ChainListener> Map<String, Map<String, T>> multiChainListenerSort(List<T> filters) {
        Map<String, T> mfilters = new HashMap<>(filters.size());
        filters.forEach(f -> mfilters.put(f.getKey(), f));
        return multiChainListenerSort(mfilters);
    }

    public static <T extends ChainListener> Map<String, Map<String, T>> multiChainListenerSort(Map<String, T> filters) {
        Map<String, Map<String, T>> distributedListeners = new LinkedHashMap<>();
        filters.entrySet()
                .stream()
                .sorted(Map.Entry.comparingByValue(new ChainComparator()))
                .forEachOrdered(x -> initListenterMap(distributedListeners, x.getValue()));
        for (Map.Entry<String, Map<String, T>> type : distributedListeners.entrySet()) {
            Map<String, T> m = type.getValue();
            int index = 0;
            int size = m.size();
            for (Map.Entry<String, T> item : m.entrySet()) {
                T listener = item.getValue();
                listener.setIndex((++index) + "/" + size);
                listener.init();
                if(debug) {
                    log.info("Init ChainListener type:{},Key:{},Order:{},Index:{}", type.getKey(), listener.getKey(), listener.getOrder(), listener.getIndex());
                }
            }
        }
        return distributedListeners;
    }

    private static <T extends ChainListener> void initListenterMap(Map<String, Map<String, T>> flowHandlerListeners, T listener) {
        if (listener.getType() == null) {
            log.error("ChainListener Type IS NULL,Class:{}", listener.getClass().getSimpleName());
            throw new DistributedListenerException("DistributedListener Type IS NULL");
        }
        String listenerType = listener.getType().replaceAll(",", ";");
        String[] types = listenerType.split(";");
        for (String type : types) {
            Map<String, T> listenerMap = flowHandlerListeners.computeIfAbsent(type, k -> new LinkedHashMap<>());
            listenerMap.put(listener.getKey(), listener);
        }
    }

    public static <T extends ChainListener> Map<String, T> singleChainListenerSort(List<T> filters) {
        Map<String, T> mfilters = new HashMap<>(filters.size());
        filters.forEach(f -> mfilters.put(f.getKey(), f));
        return singleChainListenerSort(mfilters);
    }

    public static <T extends ChainListener> Map<String, T> singleChainListenerSort(Map<String, T> flowHandlerListeners) {
        Map<String, T> distributed = new LinkedHashMap<>();
        flowHandlerListeners.entrySet()
                .stream()
                .sorted(Map.Entry.comparingByValue(new ChainComparator()))
                .forEachOrdered(x -> distributed.put(x.getKey(), x.getValue()));
        int index = 0;
        int size = distributed.size();
        for (Map.Entry<String, T> item : distributed.entrySet()) {
            T listener = item.getValue();
            listener.setIndex((++index) + "/" + size);
            listener.init();
            if(debug) {
                log.info("Init ChainListener:{},index:{}", listener.getKey(), listener.getIndex());
            }
        }
        return distributed;
    }
}
