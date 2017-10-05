package de.ctrlaltdel.sample.gateway;

import static com.jayway.restassured.RestAssured.given;

/**
 * RunResourceOwnerCredentialsGrant
 */
public class RunResourceOwnerCredentialsGrant extends SampleBase {

    private static String tokenEndpoint;

    public static void main(String[] args) {
        RunResourceOwnerCredentialsGrant app = new RunResourceOwnerCredentialsGrant();
        try {
            String bearer = app.login();
            for (String path : new String[] { "user", "master"}) {
                app.access(bearer, path);
            }

            app.logout(bearer);
            app.access(bearer, "user");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private String login() {

        return "Bearer "  + given()
                .auth().preemptive().basic("service-user", "123456")
                .redirects().follow(false)
                .formParam("grant_type", "password")
                .formParam("username", "test1")
                .formParam("password", "123456")
                .post(resolveTokenEndpoint())
                .then()
                .log().headers()
                .statusCode(200)
                .extract()
                .path("access_token")
                ;
    }



}
