package de.ctrlaltdel.sample.gateway;

import com.jayway.restassured.response.ExtractableResponse;
import com.jayway.restassured.response.Response;

import static com.jayway.restassured.RestAssured.given;

/**
 * SampleBase
 */
public class SampleBase {

    static final String SAMPLE_URL = "http://localhost:8888/sample/user";

    public static String resolveTokenEndpoint() {
        return String.format("%s/realms/%s/protocol/openid-connect/token",
                "http://keycloak1:8080/auth",
                "api-gateway");
    }

    public void access(String bearer, String path) {

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

    public void logout(String bearer) {

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
