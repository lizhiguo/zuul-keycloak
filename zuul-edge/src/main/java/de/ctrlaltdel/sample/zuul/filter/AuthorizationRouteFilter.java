package de.ctrlaltdel.sample.zuul.filter;

import com.netflix.zuul.context.RequestContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;

/**
 * AuthorizationRouteFilter
 */
@Controller
public class AuthorizationRouteFilter extends KeycloakFilter {

    private static final Logger LOG = LoggerFactory.getLogger(AuthorizationRouteFilter.class);

    @Override
    protected boolean isRouteFilter() {
        return true;
    }

    @Override
    public Object run() {
        RequestContext.getCurrentContext().addZuulRequestHeader(AUTHORIZATION, extractBearer());
        return null;
    }
}
