package de.ctrlaltdel.sample.zuul.filter;

import com.netflix.zuul.context.RequestContext;

import javax.servlet.http.HttpServletResponse;

/**
 * BearerPreFilter
 * Returns the bearer to the client as Authorization-Header
 */
public class BearerPreFilter extends KeycloakFilter {

    @Override
    protected boolean isPreFilter() {
        return true;
    }

    @Override
    public Object run() {
        RequestContext context = RequestContext.getCurrentContext();
        HttpServletResponse response = context.getResponse();
        if (!response.containsHeader(AUTHORIZATION)) {
            response.addHeader(AUTHORIZATION, extractBearer());
        }
        return null;
    }
}
