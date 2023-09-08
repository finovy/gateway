package tech.finovy.gateway.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.cloud.gateway.route.RouteDefinitionLocator;
import org.springframework.cloud.gateway.support.NameUtils;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;
import springfox.documentation.swagger.web.SwaggerResource;
import springfox.documentation.swagger.web.SwaggerResourcesProvider;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;


@Slf4j
@Component
@Primary
@ConditionalOnProperty(name = "gateway.api-doc.enable", havingValue = "true")
public class SwaggerResourceConfig implements SwaggerResourcesProvider {


    @Autowired
    private RouteDefinitionLocator routeLocator;

    @Override
    public List<SwaggerResource> get() {
        List<SwaggerResource> resources = new ArrayList<>();
        routeLocator.getRouteDefinitions().subscribe(route -> {
            route.getPredicates().stream()
                    .filter(predicateDefinition -> Optional.ofNullable(predicateDefinition.getArgs().get(NameUtils.GENERATED_NAME_PREFIX + "0")).orElse("").endsWith("/**"))
                    .forEach(predicateDefinition -> {
                        if (route.getId().contains("docs-")) {
                            resources.add(swaggerResource(route.getId(), predicateDefinition.getArgs().get(NameUtils.GENERATED_NAME_PREFIX + "0").replace("**", "v2/api-docs")));
                        }
                    });
        });
        return resources;
    }

    private SwaggerResource swaggerResource(String name, String location) {
        log.info("name:{},location:{}", name, location);
        SwaggerResource swaggerResource = new SwaggerResource();
        swaggerResource.setName(name);
        swaggerResource.setLocation(location);
        swaggerResource.setSwaggerVersion("3.0");
        swaggerResource.setUrl(location);
        return swaggerResource;
    }

}
