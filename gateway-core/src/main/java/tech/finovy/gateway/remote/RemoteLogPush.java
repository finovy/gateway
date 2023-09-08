package tech.finovy.gateway.remote;

import com.alibaba.fastjson.JSON;
import lombok.extern.slf4j.Slf4j;
import tech.finovy.gateway.common.entity.DegradeEvent;
import tech.finovy.gateway.common.entity.ExceptionEvent;
import tech.finovy.gateway.common.entity.RequestEvent;
import tech.finovy.gateway.common.entity.ResponseEvent;


@Slf4j
public class RemoteLogPush {


    public static void push(ExceptionEvent exceptionEvent) {
        log.error(JSON.toJSONString(exceptionEvent));
    }


    public static void push(DegradeEvent degradeEvent) {
        log.error(JSON.toJSONString(degradeEvent));
    }


    public static void push(ResponseEvent responseEvent) {
        log.info(JSON.toJSONString(responseEvent));
    }


    public static void push(RequestEvent requestEvent) {
        log.info(JSON.toJSONString(requestEvent));
    }
}
