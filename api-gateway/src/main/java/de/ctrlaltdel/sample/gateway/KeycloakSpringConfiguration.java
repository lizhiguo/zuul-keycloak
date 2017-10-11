package de.ctrlaltdel.sample.gateway;

import org.keycloak.adapters.KeycloakDeployment;
import org.keycloak.adapters.KeycloakDeploymentBuilder;
import org.keycloak.adapters.springboot.KeycloakSpringBootProperties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * KeycloakSpringConfiguration
 */
@Configuration
@ConditionalOnWebApplication
@EnableConfigurationProperties(KeycloakSpringBootProperties.class)
public class KeycloakSpringConfiguration {

    @Autowired
    private KeycloakSpringBootProperties keycloakProperties;

    @Bean
    public KeycloakDeployment createKeycloakDeployment() {
        return KeycloakDeploymentBuilder.build(keycloakProperties);
    }

}
