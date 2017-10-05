package de.ctrlaltdel.sample.gateway;

import static com.jayway.restassured.RestAssured.given;

/**
 * RunClientCredentialsGrantFlow
 */
public class RunClientCredentialsGrantFlow  extends SampleBase {


    public static void main(String[] args) {
        RunClientCredentialsGrantFlow app = new RunClientCredentialsGrantFlow();
        try {
            String bearer = app.login();
            app.access(bearer, "user");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private String login() throws Exception {

        return "Bearer "  +  given()
                .when()
                .auth().preemptive().basic("service-user", "123456")
                .formParam("grant_type", "client_credentials")
                .post(resolveTokenEndpoint())
                .then()
                .log().headers()
                .statusCode(200)
                .extract()
                .path("access_token")
                ;
    }

}