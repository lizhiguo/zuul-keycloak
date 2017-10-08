package de.ctrlaltdel.sample.gateway.filter;

import com.netflix.util.Pair;
import com.netflix.zuul.context.RequestContext;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletResponse;
import java.util.List;

/**
 * SuppressKeycloakRedirectFilter
 */
@Component
public class SuppressKeycloakRedirectFilter extends KeycloakFilter {

    @Value("${keycloak.auth-server-url}")
    private String keyCloakUrl;

    @Override
    protected boolean isPostFilter() {
        return true;
    }

    @Override
    public boolean shouldFilter() {
        return true;
    }

    @Override
    public Object run() {
        RequestContext context = RequestContext.getCurrentContext();
        HttpServletResponse response = getResponse();
        int status = context.getResponseStatusCode();

        List<Pair<String, String>> pairs = context.getZuulResponseHeaders();
        int keycloakRedirect = -1;

        for (int i = 0; i < pairs.size(); i++) {
            if (pairs.get(i).first().equalsIgnoreCase("Location") && pairs.get(i).second().startsWith(keyCloakUrl)) {
                keycloakRedirect = i;
                break;
            }
        }

        if (status == 302 && 0 < keycloakRedirect) {
            pairs.remove(keycloakRedirect);
            context.setResponseStatusCode(403);

        }

        return null;
    }
}
