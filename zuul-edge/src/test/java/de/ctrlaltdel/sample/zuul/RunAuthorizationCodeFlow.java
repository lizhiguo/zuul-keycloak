package de.ctrlaltdel.sample.zuul;

import com.jayway.restassured.response.ExtractableResponse;
import com.jayway.restassured.response.Response;
import org.hamcrest.Matchers;

import java.net.URLDecoder;
import java.util.Arrays;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.jayway.restassured.RestAssured.given;
import static org.junit.Assert.assertTrue;

/**
 * RunServiceCheck
 */
public class RunAuthorizationCodeFlow {

    private static final String SAMPLE_URL = "http://localhost:8888/sample/user";

    private static final String APP_URL = "http://localhost:8080/app/user";

    public static void main(String[] args) {
        try {
            String bearer = login();
            access(bearer);
            accessDirect(bearer);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    private static String login() throws Exception {

        ExtractableResponse<Response> response = given()
                .redirects().follow(false)
                .when()
                .get(SAMPLE_URL)
                .then()
                .log().headers()
                .statusCode(302)
                .header("Location", Matchers.containsString("auth/realms"))
                .extract();

        // redirect zum IDP
        String location = URLDecoder.decode(response.header("Location"), "UTF-8");
        System.out.printf("%nRedirect to IDP %s%n", location);

        Map<String, String> cookies = response.cookies();

        response = given()
                .when()
                .get(location)
                .then()
                .log().headers()
                .statusCode(200)
                .extract()
        ;

        // login-form
        String form = Arrays.stream(response.asString().split("\n"))
                .filter(s -> s.contains("<form "))
                .findFirst()
                .get().trim();

        Matcher matcher = Pattern.compile("(^<form.*action=\")(?<action>[^\"]*)(.*)").matcher(form);
        assertTrue(matcher.matches());

        location = URLDecoder.decode(matcher.group("action"), "UTF-8");

        // post usename/password
        System.out.printf("%nPOST username/password to %s%n", location);

        response = given()
                .cookies(response.cookies())
                .redirects().follow(false)
                .formParam("username", "test1")
                .formParam("password", "123456")
                .when()
                .urlEncodingEnabled(false)
                .post(location)
                .then()
                .log().headers()
                .statusCode(302)
                .extract();

        location = URLDecoder.decode(response.header("Location"), "UTF-8");

        System.out.printf("%nRedirect to %s%n", location);

        response = given()
                .when()
                .redirects().follow(false)
                .cookies(cookies)
                .get(location)
                .then()
                .log().headers()
                .statusCode(302)
                .extract();


        location = URLDecoder.decode(response.header("Location"), "UTF-8");

        System.out.printf("%nGet %s%n", location);

        response = given()
                .when()
                .redirects().follow(false)
                .cookies(cookies)
                .get(location)
                .then()
                .log().headers()
                .statusCode(200)
                .extract();

        String bearer = response.header("Authorization");
        System.out.println(response.body().asString());

        return bearer;
    }

    private static void access(String bearer) {

        System.out.printf("%nGet with bearer: %s%n", SAMPLE_URL);

        ExtractableResponse<Response> response = given()
                .when()
                .redirects().follow(false)
                .header("Authorization", bearer)
                .get(SAMPLE_URL)
                .then()
                .log().headers()
                .statusCode(200)
                .extract();

        System.out.println(response.body().asString());
    }

    private static void accessDirect(String bearer) {
        System.out.printf("%nGet direct with bearer: %s%n", APP_URL);

        ExtractableResponse<Response> response = given()
                .when()
                .redirects().follow(false)
                .header("Authorization", bearer)
                .get(APP_URL)
                .then()
                .log().headers()
                .statusCode(200)
                .extract();

        System.out.println(response.body().asString());

    }

}
