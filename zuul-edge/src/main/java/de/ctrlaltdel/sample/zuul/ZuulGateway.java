package de.ctrlaltdel.sample.zuul;

import de.ctrlaltdel.sample.zuul.filter.AuthorizationRouteFilter;
import de.ctrlaltdel.sample.zuul.filter.BearerPreFilter;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.zuul.EnableZuulProxy;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
@EnableZuulProxy
public class ZuulGateway {

    public static void main(String[] args) {
        SpringApplication.run(ZuulGateway.class, args);
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
