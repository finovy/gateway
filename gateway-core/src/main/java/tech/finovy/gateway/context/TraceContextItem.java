package tech.finovy.gateway.context;

import lombok.Data;
import org.apache.commons.lang3.StringUtils;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.UUID;

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
    private static String base64Encode2String(String str) {
        return new String(Base64.getEncoder().encode(str.getBytes(StandardCharsets.UTF_8)));
    }


    public static String generateSW8ID() {
        return encodeBase64(UUID.randomUUID().toString());
    }

    public static int generateSW8ParentSpanID() {
        return 0;
    }

    public static String generateSW8ParentInfo(String info) {
        return encodeBase64(info);
    }

    public static String generateSW8TargetAddress(String targetAddress) {
        return encodeBase64(targetAddress);
    }

    private static String encodeBase64(String input) {
        return Base64.getEncoder().encodeToString(input.getBytes());
    }

    public static String getSw8(String traceId,String uri,String applicationName,String endpoint) {
        String sw8SegmentID = generateSW8ID();
        String sw8ParentTraceSegmentID = generateSW8ID();
        int sw8ParentSpanID = generateSW8ParentSpanID();
        String sw8ParentService = generateSW8ParentInfo(applicationName);
        String sw8ParentInstance = generateSW8ParentInfo(applicationName);
        String sw8ParentEndpoint = generateSW8ParentInfo(endpoint);
        String sw8TargetAddress = generateSW8TargetAddress(uri);
        return "1" + traceId + "-" + sw8SegmentID + "-" + sw8ParentTraceSegmentID + "-" +
                sw8ParentSpanID + "-" + sw8ParentService + "-" + sw8ParentInstance + "-" +
                sw8ParentEndpoint + "-" + sw8TargetAddress;
    }

}
