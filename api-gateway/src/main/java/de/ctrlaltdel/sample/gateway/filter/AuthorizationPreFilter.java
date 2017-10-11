package de.ctrlaltdel.sample.gateway.filter;

import com.netflix.zuul.context.RequestContext;
import org.keycloak.adapters.AdapterDeploymentContext;
import org.keycloak.adapters.KeycloakDeployment;
import org.keycloak.adapters.NodesRegistrationManagement;
import org.keycloak.adapters.servlet.KeycloakOIDCFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import java.security.Principal;

/**
 * Delegates the Request to an KeycloakOIDCFilter (Servlet-API filter).
 * This filter goes on only if an authentificated user is available (Filterchain.doFilter)
 */
@Component
public class AuthorizationPreFilter extends KeycloakFilter {

    private KeycloakServletFilter keycloakServletFilter;

    @Autowired
    public void setKeycloakDeployment(KeycloakDeployment keycloakDeployment) {
        keycloakServletFilter = new KeycloakServletFilter(new AdapterDeploymentContext(keycloakDeployment));
    }

    @Override
    protected boolean isPreFilter() {
        return true;
    }

    @Override
    public int filterOrder() {
        return Integer.MIN_VALUE;
    }

    @Override
    public boolean shouldFilter() {
        return getUserPrincipal() == null;
    }

    @Override
    public Object run() {
        RequestContext context = RequestContext.getCurrentContext();

        try {
            keycloakServletFilter.doFilter(context.getRequest(), context.getResponse(), (servletRequest, servletResponse) -> {
                Principal principal = ((HttpServletRequest) servletRequest).getUserPrincipal();
                if (principal != null) {
                    context.set(Principal.class.getName(), principal);
                }
            });
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }

        context.setSendZuulResponse(getUserPrincipal() != null);

        return null;
    }

    private static class KeycloakServletFilter extends KeycloakOIDCFilter {

        public KeycloakServletFilter(AdapterDeploymentContext deploymentContext) {
            this.deploymentContext = deploymentContext;
            this.nodesRegistrationManagement = new NodesRegistrationManagement();
        }
    }
}
