package tech.finovy.gateway.globalfilter.listener;


import lombok.extern.slf4j.Slf4j;
import org.apache.commons.compress.compressors.CompressorException;
import org.apache.commons.compress.compressors.CompressorInputStream;
import org.apache.commons.compress.compressors.CompressorStreamFactory;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import tech.finovy.gateway.common.configuration.GatewayConfiguration;
import tech.finovy.gateway.common.constant.GlobalAuthConstant;
import tech.finovy.gateway.disruptor.core.listener.AbstractDisruptorListener;
import tech.finovy.gateway.listener.DisrptorConfigurationService;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;

@Slf4j
public abstract class AbstractDisruptorChainEventListener extends AbstractDisruptorListener implements DisrptorConfigurationService {


    private GatewayConfiguration configuration;

    public void setConfiguration(GatewayConfiguration configuration) {
        this.configuration = configuration;
    }


    protected String compType(List<String> context) {
        if (checkValueExists(context, GlobalAuthConstant.GZIP)) {
            return GlobalAuthConstant.GZIP;
        }
        if (checkValueExists(context, CompressorStreamFactory.DEFLATE)) {
            return CompressorStreamFactory.DEFLATE;
        }
        if (checkValueExists(context, CompressorStreamFactory.BROTLI)) {
            return CompressorStreamFactory.BROTLI;
        }
        return null;
    }

    protected boolean checkValueExists(List<String> context, String value) {
        if (context == null) {
            return false;
        }
        for (String each : context) {
            if (StringUtils.containsIgnoreCase(each, value)) {
                return true;
            }
        }
        return false;
    }

    protected byte[] uncompresss(String responseType, byte[] response) {
        if (StringUtils.isBlank(responseType) || response == null) {
            return response;
        }
        byte[] resp = response;
        if (ArrayUtils.isEmpty(response)) {
            return response;
        }
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        ByteArrayInputStream in = new ByteArrayInputStream(resp);
        String ctype = responseType;
        if (GlobalAuthConstant.GZIP.equals(ctype)) {
            ctype = CompressorStreamFactory.GZIP;
        }
        try (CompressorInputStream gzip = new CompressorStreamFactory().createCompressorInputStream(ctype, in)) {
            byte[] buffer = new byte[1024];
            int n;
            while ((n = gzip.read(buffer)) >= 0) {
                out.write(buffer, 0, n);
            }
            return out.toByteArray();
        } catch (IOException | CompressorException e) {
            log.error("uncompress error:{}", e);
        } finally {
            try {
                out.close();
                in.close();
            } catch (IOException e) {
                log.error("Close Stream error:{}", e.toString());
            }
        }
        return response;
    }
//
//    private void compress(GlobalAuthContext authContext) {
//        if (StringUtils.isBlank(authContext.getAcceptCompresssorType())) {
//            return;
//        }
//        byte[] resp = authContext.getBody();
//        if (ArrayUtils.isEmpty(resp)) {
//            return;
//        }
//        String ctype=authContext.getAcceptCompresssorType();
//        if(GlobalAuthConstant.GZIP.equals(ctype)){
//            ctype=CompressorStreamFactory.GZIP;
//        }
//        ByteArrayOutputStream out = new ByteArrayOutputStream();
//        try (CompressorOutputStream gzip = new CompressorStreamFactory().createCompressorOutputStream(ctype, out)) {
//            gzip.write(resp);
//        } catch (IOException | CompressorException e) {
//            log.error("compress error:{}", e);
//        } finally {
//            try {
//                out.close();
//            } catch (IOException e) {
//                log.error("Close Stream error:{}", e.toString());
//            }
//        }
//        authContext.setBody(out.toByteArray());
//    }
}
