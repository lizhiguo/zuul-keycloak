package de.ctrlaltdel.sample.zuul;

import com.jayway.restassured.response.ExtractableResponse;
import com.jayway.restassured.response.Response;

import static com.jayway.restassured.RestAssured.given;

/**
 * RunResourceOwnerCredentialsGrant
 */
public class RunResourceOwnerCredentialsGrant {

    private static final String TOKEN_ENDPOINT = "http://keycloak1:8080/auth/realms/ds/protocol/openid-connect/token";

    public static void main(String[] args) {
        try {
            String bearer = runLogin();
            for (String path : new String[] { "user", "master"}) {
                access(bearer, path);
            }
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
                .post(TOKEN_ENDPOINT)
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

        int expectedStatus = "user".equals(path) ? 200 : 403;

        ExtractableResponse<Response> response = given()
                .when()
                .redirects().follow(false)
                .header("Authorization", bearer)
                .get(url)
                .then()
                .log().headers()
                .statusCode(expectedStatus)
                .extract();

        System.out.println(response.body().asString());
    }


}
