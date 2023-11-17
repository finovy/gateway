package tech.finovy.gateway.context;

import lombok.Data;
import org.apache.commons.lang3.StringUtils;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

@Data
public class TraceContextItem {

    private String traceId;
    /**
     * The segment id of the parent.
     */
    private String traceSegmentId;
    /**
     * The span id in the parent segment.
     */
    private int spanId = -1;
    private String parentService = "";
    private String parentServiceInstance = "";
    /**
     * The endpoint(entrance URI/method signature) of the parent service.
     */
    private String parentEndpoint;
    /**
     * The network address(ip:port, hostname:port) used in the parent service to access the current service.
     */
    private String addressUsedAtClient;

    /**
     * 是否为有效的上下文
     */
    private boolean valid = false;

    public boolean isValid() {
        return StringUtils.isNotBlank(traceId)
                && StringUtils.isNotEmpty(traceSegmentId)
                && getSpanId() > -1
                && StringUtils.isNotEmpty(parentService)
                && StringUtils.isNotEmpty(parentServiceInstance)
                && StringUtils.isNotEmpty(parentEndpoint)
                && StringUtils.isNotEmpty(addressUsedAtClient);
    }

    public static TraceContextItem deserialize(String text) {
        if (StringUtils.isBlank(text)) {
            return invalid();
        }
        TraceContextItem item = new TraceContextItem();
        int fileNums = 8;
        try {
            String[] parts = text.split("-", fileNums);
            if (parts.length == fileNums) {
                // parts[0] is sample flag, always trace if header exists.
                item.setTraceId(base64Decode2String(parts[1]));
                item.setTraceSegmentId(base64Decode2String(parts[2]));
                item.setSpanId(Integer.parseInt(parts[3]));
                item.setParentService(base64Decode2String(parts[4]));
                item.setParentServiceInstance(base64Decode2String(parts[5]));
                item.setParentEndpoint(base64Decode2String(parts[6]));
                item.setAddressUsedAtClient(base64Decode2String(parts[7]));
                return item;
            }
        } catch (Exception ignored) {
        }
        return invalid();
    }

    private static TraceContextItem invalid() {
        return new TraceContextItem();
    }

    private static String base64Decode2String(String str) {
        return new String(Base64.getDecoder().decode(str), StandardCharsets.UTF_8);
    }
}
