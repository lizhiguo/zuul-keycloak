package de.ctrlaltdel.sample.gateway.filter;

import com.netflix.zuul.context.RequestContext;
import org.keycloak.KeycloakSecurityContext;
import org.keycloak.representations.AccessToken;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import java.net.HttpURLConnection;
import java.net.URL;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

/**
 * LogoutRouteFilter
 */
@Component
public class LogoutRouteFilter extends KeycloakFilter {

    private static final Logger LOG = LoggerFactory.getLogger(LogoutRouteFilter.class);

    // TODO use shared cache ..
    private static final Map<String, Instant> INVALID_KEYS = new HashMap<>();

    @Override
    protected boolean isRouteFilter() {
        return true;
    }

    @Override
    public Object run() {

        RequestContext context = RequestContext.getCurrentContext();
        HttpServletRequest request = context.getRequest();
        KeycloakSecurityContext securityContext = (KeycloakSecurityContext) request.getAttribute(KeycloakSecurityContext.class.getName());
        if (securityContext == null) {
            return null;
        }

        String proxy = (String) context.get("proxy");

        int status;

        if ("logout".equals(proxy)) {
            doLogout(request, securityContext);
            status = 204;
        } else {
            status = INVALID_KEYS.containsKey(securityContext.getTokenString()) ? 403 : 0;
        }

        if (0 < status) {
            context.setSendZuulResponse(false);
            context.getResponse().setStatus(status);
        }

        return null;
    }


    private void doLogout(HttpServletRequest request, KeycloakSecurityContext securityContext) {
        HttpURLConnection connection = null;
        try {
            AccessToken accessToken = securityContext.getToken();
            String bearer = securityContext.getTokenString();

            INVALID_KEYS.put(bearer, Instant.ofEpochSecond(accessToken.getExpiration()));

            String logoutUrl = String.format("%s/protocol/openid-connect/logout?id_token_hint=%s", accessToken.getIssuer(), bearer);
            connection = (HttpURLConnection) new URL(logoutUrl).openConnection();
            int status = connection.getResponseCode();
            LOG.info("logout response {} : {}", accessToken.getSubject(), status);
            request.logout();
        } catch (Exception e) {
            LOG.error("", e);
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }
    }

    @Scheduled(fixedDelay = 30000)
    public void housekeeping() {
        Instant now = Instant.now();
        INVALID_KEYS.forEach((key, time) -> {
            if (time.isBefore(now)) {
                INVALID_KEYS.remove(key);
            }
        });
    }
}
