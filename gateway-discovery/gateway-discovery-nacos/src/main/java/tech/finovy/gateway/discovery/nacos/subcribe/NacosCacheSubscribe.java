package tech.finovy.gateway.discovery.nacos.subcribe;

import com.alibaba.fastjson.JSON;
import com.alibaba.nacos.client.naming.event.InstancesChangeEvent;
import com.alibaba.nacos.common.notify.Event;
import com.alibaba.nacos.common.notify.listener.Subscriber;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.cloud.loadbalancer.core.CachingServiceInstanceListSupplier;


@Slf4j
public class NacosCacheSubscribe extends Subscriber<InstancesChangeEvent> {
    private CacheManager defaultLoadBalancerCacheManager;

    public NacosCacheSubscribe(CacheManager defaultLoadBalancerCacheManager) {
        this.defaultLoadBalancerCacheManager = defaultLoadBalancerCacheManager;
    }

    @Override
    public void onEvent(InstancesChangeEvent instancesChangeEvent) {
        Cache cache = defaultLoadBalancerCacheManager.getCache(CachingServiceInstanceListSupplier.SERVICE_INSTANCE_CACHE_NAME);
        if (cache == null) {
            for (String name : defaultLoadBalancerCacheManager.getCacheNames()) {
                log.warn("------>{}", name);
            }
            return;
        }
        cache.evict(instancesChangeEvent.getServiceName());
        log.warn("cache event:{}", JSON.toJSONString(instancesChangeEvent));
    }

    @Override
    public Class<? extends Event> subscribeType() {
        return InstancesChangeEvent.class;
    }
}
