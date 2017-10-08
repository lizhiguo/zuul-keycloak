package de.ctrlaltdel.sample.gateway;

import org.apache.catalina.Context;
import org.apache.tomcat.util.descriptor.web.SecurityCollection;
import org.apache.tomcat.util.descriptor.web.SecurityConstraint;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.embedded.EmbeddedServletContainerCustomizer;
import org.springframework.boot.context.embedded.tomcat.TomcatContextCustomizer;
import org.springframework.boot.context.embedded.tomcat.TomcatEmbeddedServletContainerFactory;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.netflix.zuul.filters.ZuulProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * KeycloakCustomizing
 */
@Configuration
@EnableConfigurationProperties(KeycloakCustomizing.ExtendedZuulProperties.class)
public class KeycloakCustomizing {

    private static final Logger LOG = LoggerFactory.getLogger(KeycloakCustomizing.class);

    @Autowired
    private ExtendedZuulProperties zuulProperties;

    @Bean
    public EmbeddedServletContainerCustomizer createContainerCustomizer() {
        return container -> {
            if (container instanceof TomcatEmbeddedServletContainerFactory) {
                ((TomcatEmbeddedServletContainerFactory) container).addContextCustomizers(new ZuulTomcatContextCustomizer(zuulProperties.getRoutes()));
            } else {
                LOG.error("no customizing support for jetty/undertow ...");
            }
        };
    }

    static class ZuulTomcatContextCustomizer implements TomcatContextCustomizer {

        private final Map<String, ExtendedZuulProperties.ExtendedZuulRoute> routes;

        public ZuulTomcatContextCustomizer(Map<String, ExtendedZuulProperties.ExtendedZuulRoute> routes) {
            this.routes = routes;
        }

        @Override
        public void customize(Context context) {

            routes.forEach((name, route) -> {

                if (route.getRoles() == null || route.getRoles().isEmpty()) {
                    return;
                }

                SecurityConstraint securityConstraint = new SecurityConstraint();
                securityConstraint.setDisplayName(route.getId());

                route.getRoles().forEach(securityConstraint::addAuthRole);

                SecurityCollection securityCollection = new SecurityCollection();
                securityCollection.setName(route.getId());

                securityConstraint.addCollection(securityCollection);

                String pattern = route.getPath();
                if (pattern.endsWith("**")) {
                    pattern = pattern.substring(0, pattern.length() - 1);
                }
                securityCollection.addPattern(pattern);

                context.addConstraint(securityConstraint);

                LOG.info("add tomact contraint {} {}", securityConstraint.getDisplayName(), pattern);

            });
        }

    }

    @ConfigurationProperties("zuul")
    public static class ExtendedZuulProperties {

        private Map<String, ExtendedZuulRoute> routes = new LinkedHashMap<>();

        public void setRoutes(Map<String, ExtendedZuulRoute> routes) {
            this.routes = routes;
        }

        public Map<String, ExtendedZuulRoute> getRoutes() {
            return routes;
        }


        public static class ExtendedZuulRoute extends ZuulProperties.ZuulRoute {

            private List<String> roles = new ArrayList<>();

            public void setRoles(List<String> roles) {
                this.roles = roles;
            }

            public List<String> getRoles() {
                return roles;
            }
        }

    }
}
