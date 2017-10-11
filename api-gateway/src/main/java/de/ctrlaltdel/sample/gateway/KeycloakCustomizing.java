package de.ctrlaltdel.sample.gateway;

import org.keycloak.adapters.springboot.KeycloakAutoConfiguration;
import org.keycloak.adapters.springboot.KeycloakSpringBootProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.embedded.EmbeddedServletContainerCustomizer;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.netflix.zuul.filters.ZuulProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * KeycloakCustomizing
 */
@Configuration
@EnableConfigurationProperties(KeycloakCustomizing.ExtendedZuulProperties.class)
@AutoConfigureBefore(KeycloakAutoConfiguration.class)
@ConditionalOnProperty(value = "keycloak.enabled", matchIfMissing = true)
public class KeycloakCustomizing {

    private static final Logger LOG = LoggerFactory.getLogger(KeycloakCustomizing.class);

    @Autowired
    private ExtendedZuulProperties zuulProperties;

    @Autowired
    private KeycloakSpringBootProperties keycloakProperties;


    @Bean
    public EmbeddedServletContainerCustomizer getContainerCustomizer() {
        // komplex : mit AutoConfigureBefore erreicht man das diese Configuration (normalerweise) vor der KeycloakAutoConfiguration geladen wird
        // das reicht allerdings nicht aus, es muss auch ein EmbeddedServletContainerCustomizer erzeugt werden da der Container vor den restlichen durch Configure-Beans erzeugt wird
        // d.h. damit waere der KeycloakAutoConfiguration.EmbeddedServletContainerCustomizer wieder vor dieser Configuration
        // deshalb erzeugen wir einen EmbeddedServletContainerCustomizer der nichts am Container customized, aber die KeycloakSpringBootProperties mit den Zuulrouten erweitert
        return container -> {

            List<KeycloakSpringBootProperties.SecurityConstraint> securityConstraints = extractSecurityConstraints();
            zuulProperties.getRoutes().forEach((name, route) -> {

                if (route.getRoles() == null || route.getRoles().isEmpty()) {
                    return;
                }

                String pattern = route.getPath();
                if (pattern.endsWith("**")) {
                    pattern = pattern.substring(0, pattern.length() - 1);
                }

                KeycloakSpringBootProperties.SecurityConstraint securityConstraint = new KeycloakSpringBootProperties.SecurityConstraint();
                securityConstraint.setAuthRoles(route.getRoles());

                KeycloakSpringBootProperties.SecurityCollection securityCollection = new KeycloakSpringBootProperties.SecurityCollection();
                securityCollection.setName(name);
                securityCollection.setPatterns(Collections.singletonList(pattern));

                securityConstraint.setSecurityCollections(Collections.singletonList(securityCollection));
                securityConstraints.add(securityConstraint);

                LOG.info("add zuul route {} to security constraints, role={}", name, route.getRoles());
            });

        };
    }

    private List<KeycloakSpringBootProperties.SecurityConstraint> extractSecurityConstraints() {

        List<KeycloakSpringBootProperties.SecurityConstraint> securityConstraints = keycloakProperties.getSecurityConstraints();
        if (securityConstraints == null) {
            securityConstraints = new ArrayList<>();
            keycloakProperties.setSecurityConstraints(securityConstraints);
        }
        return securityConstraints;
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
