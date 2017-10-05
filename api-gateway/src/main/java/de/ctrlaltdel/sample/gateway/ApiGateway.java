package de.ctrlaltdel.sample.gateway;

import de.ctrlaltdel.sample.gateway.filter.AuthorizationRouteFilter;
import de.ctrlaltdel.sample.gateway.filter.BearerPreFilter;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.zuul.EnableZuulProxy;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableZuulProxy
@EnableScheduling
public class ApiGateway {

    public static void main(String[] args) {
        SpringApplication.run(ApiGateway.class, args);
    }

    @Bean
    public AuthorizationRouteFilter authorizationRouteFilter() {
        return new AuthorizationRouteFilter();
    }

    @Bean
    public BearerPreFilter bearerPreFilter() {
        return new BearerPreFilter();
    }

}
