package de.ctrlaltdel.sample.zuul;

import com.jayway.restassured.response.ExtractableResponse;
import com.jayway.restassured.response.Response;

import java.util.Properties;

import static com.jayway.restassured.RestAssured.given;

/**
 * RunResourceOwnerCredentialsGrant
 */
public class RunResourceOwnerCredentialsGrant {

    private static String tokenEndpoint;

    public static void main(String[] args) {

        try {
            Properties appProps = new Properties();
            appProps.load(RunResourceOwnerCredentialsGrant.class.getResourceAsStream("/application.properties"));

            tokenEndpoint = String.format("%s/realms/%s/protocol/openid-connect/token",
                    appProps.getProperty("keycloak.auth-server-url"),
                    appProps.getProperty("keycloak.realm"));

            System.out.printf("Token-Endpoint: %s%n", tokenEndpoint);

            String bearer = runLogin();
//            for (String path : new String[] { "user", "master"}) {
//                access(bearer, path);
//            }

            logout(bearer);
            access(bearer, "user");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static String runLogin() {

        return "Bearer "  + given()
                .auth().preemptive().basic("service-user", "123456")
                .redirects().follow(false)
                .formParam("grant_type", "password")
                .formParam("username", "test1")
                .formParam("password", "123456")
                .post(tokenEndpoint)
                .then()
                .log().headers()
                .statusCode(200)
                .extract()
                .path("access_token")
                ;
    }

    private static void access(String bearer, String path) {

        String url = "http://localhost:8888/sample/" + path;
        System.out.printf("%nGet with bearer: %s%n", url);

        ExtractableResponse<Response> response = given()
                .when()
                .redirects().follow(false)
                .header("Authorization", bearer)
                .get(url)
                .then()
                .log().headers()
                .extract();

        System.out.printf("Status: %d%n", response.statusCode());
        System.out.println(response.body().asString());
    }

    private static void logout(String bearer) {

        String url = "http://localhost:8888/logout";
        System.out.printf("%nGet with bearer: %s%n", url);


        ExtractableResponse<Response> response = given()
                .when()
                .redirects().follow(false)
                .header("Authorization", bearer)
                .get(url)
                .then()
                .log().headers()
                .statusCode(204)
                .extract();

        System.out.printf("Status: %d%n", response.statusCode());
        System.out.println(response.body().asString());
    }


}
