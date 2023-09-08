package tech.finovy.gateway;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;

@Slf4j
class TestBootstrapController {

    @Test
    @DisplayName("TestGlobalAuthService")
    void traceIdTest() {
        String sk = StringUtils.joinWith(
                "-",
                "1",
                Base64.encodeBase64String("x-trace-09672".getBytes(StandardCharsets.UTF_8)),
                Base64.encodeBase64String("TXTraceId09666".getBytes(StandardCharsets.UTF_8)),
                "0",
                Base64.encodeBase64String("gateway".getBytes(StandardCharsets.UTF_8)),
                Base64.encodeBase64String("gateway@0001".getBytes(StandardCharsets.UTF_8)),
                Base64.encodeBase64String("ParentEndpoint".getBytes(StandardCharsets.UTF_8)),
                Base64.encodeBase64String("127.0.0.7:8750".getBytes(StandardCharsets.UTF_8))
        );
        log.info("-------------------->{}", sk);
    }
}
