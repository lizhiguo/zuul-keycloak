package de.ctrlaltdel.sample.gateway.filter;

import org.springframework.stereotype.Component;

/**
 * BearerPostFilter
 * Returns the bearer to the client as Authorization-Header
 */
@Component
public class BearerPostFilter extends KeycloakFilter {

    @Override
    protected boolean isPostFilter() {
        return true;
    }

    @Override
    public boolean shouldFilter() {
        return !getResponse().containsHeader(AUTHORIZATION);
    }

    @Override
    public Object run() {
        getResponse().addHeader(AUTHORIZATION, extractBearer());
        return null;
    }
}
