package tech.finovy.gateway;


import com.alibaba.fastjson.JSON;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.autoconfigure.RefreshAutoConfiguration;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.util.AntPathMatcher;
import org.springframework.util.PathMatcher;
import tech.finovy.gateway.common.context.ConfigurationContext;
import tech.finovy.gateway.common.context.ConfigurationContextHolder;
import tech.finovy.gateway.common.entity.SkipContentTypeItemEntity;
import tech.finovy.gateway.common.entity.SkipUrlItemEntity;

@Slf4j
@ComponentScan(basePackages = {"tech.finovy.*"})
@ContextConfiguration
@EnableDiscoveryClient
@EnableAutoConfiguration
@ExtendWith(SpringExtension.class)
@ImportAutoConfiguration(RefreshAutoConfiguration.class)
@SpringBootTest(classes = TestRoute.class)
class TestRoute {

    private ConfigurationContext configurationContext = ConfigurationContextHolder.get();

    @Test
    @DisplayName("TestRoute")
    void routeTest() {
        SkipUrlItemEntity url = configurationContext.pathContain("/v1/user/auth/signIn");
        log.info("signIn url :{}", JSON.toJSONString(url));
        url = configurationContext.pathContain("/trading/99");
        log.info("trading url :{}", JSON.toJSONString(url));
        url = configurationContext.pathContain("/trading/99.js");
        log.info("js url :{}", JSON.toJSONString(url));

        SkipContentTypeItemEntity contentType = configurationContext.contentTypeContain("text/html:chart=dd");
        log.info("url :{}", JSON.toJSONString(contentType));
        contentType = configurationContext.contentTypeContain("image");
        log.info("url :{}", JSON.toJSONString(contentType));
    }

    @Test
    @DisplayName("TestMatch")
    void matchTest() {
        PathMatcher matcher = new AntPathMatcher();
        log.info("------------->{}", matcher.match("/**/*.js", "/99/99/99.js"));
    }
}
