package de.ctrlaltdel.sample.gateway;

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
public class RunAuthorizationCodeFlow extends SampleBase {


    public static void main(String[] args) {
        RunAuthorizationCodeFlow app = new RunAuthorizationCodeFlow();
        try {
            String bearer = app.login();
            app.access(bearer, "user");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    private String login() throws Exception {

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

}
