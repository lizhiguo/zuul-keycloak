package de.ctrlaltdel.sample.app.service;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import org.keycloak.KeycloakPrincipal;
import org.keycloak.representations.AccessToken;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.security.Principal;

@RestController
public class ServiceController {

    private static final Logger LOG = LoggerFactory.getLogger(ServiceController.class);


    private static final ObjectMapper objectMapper = new ObjectMapper();

    static {
        objectMapper.enable(SerializationFeature.INDENT_OUTPUT);
        objectMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
    }

    @RequestMapping(path = "user", produces = MediaType.APPLICATION_JSON_VALUE)
    public String user(Principal principal, HttpServletResponse response, HttpServletRequest request) throws Exception {
        if (principal instanceof KeycloakPrincipal) {
            AccessToken accessToken = ((KeycloakPrincipal) principal).getKeycloakSecurityContext().getToken();
            return objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(accessToken);

        }
        return String.format("{\"principal\":\"%s\"}", principal == null ? "null" : principal);
    }


    @RequestMapping(path = "master", produces = MediaType.APPLICATION_JSON_VALUE)
    public String master(Principal principal, HttpServletResponse response, HttpServletRequest request) {
        boolean hasPermission = request.isUserInRole("master");
        return String.format("{\"principal\":\"%s\", \"permission\":\"%b\" }", principal == null ? "null" : principal.getName(), hasPermission);
    };
}
