package de.ctrlaltdel.sample.zuul.filter;

import com.netflix.zuul.ZuulFilter;
import com.netflix.zuul.context.RequestContext;
import org.keycloak.KeycloakPrincipal;

import java.security.Principal;

/**
 * KeycloakFilter
 */
public abstract class KeycloakFilter extends ZuulFilter {

    protected static final String AUTHORIZATION = "Authorization";

    @Override
    public int filterOrder() {
        return 0;
    }

    @Override
    public boolean shouldFilter() {
        return true;
    }

    @Override
    public String filterType() {
        if (isRouteFilter()) {
            return "route";
        }
        if (isPreFilter()) {
            return "pre";
        }
        if (isPostFilter()) {
            return "post";
        }
        throw new IllegalStateException("One of isRouteFilter/isPreFilter/isPostFilter must be overriden");
    }

    protected boolean isRouteFilter() {
        return false;
    }

    protected boolean isPreFilter() {
        return false;
    }

    protected boolean isPostFilter() {
        return false;
    }

    protected String extractBearer() {
        return String.format("Bearer %s", extractToken());
    }

    protected String extractToken() {
        RequestContext context = RequestContext.getCurrentContext();
        Principal principal = context.getRequest().getUserPrincipal();
        return principal instanceof KeycloakPrincipal ? ((KeycloakPrincipal) principal).getKeycloakSecurityContext().getTokenString() : "";

    }
}
