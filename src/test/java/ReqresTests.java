import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.github.javafaker.Faker;
import io.restassured.builder.RequestSpecBuilder;
import io.restassured.filter.log.LogDetail;
import io.restassured.path.json.JsonPath;
import io.restassured.specification.RequestSpecification;
import org.junit.jupiter.api.Test;

import java.util.Locale;

import static io.restassured.RestAssured.given;
import static io.restassured.http.ContentType.JSON;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.is;

public class ReqresTests {

    Faker faker = new Faker(new Locale("en"));
    String name = faker.funnyName().name();
    String job = faker.job().position();
    String email = "eve.holt@reqres.in";
    String password = faker.bothify("#########");

    private RequestSpecification specs() {
        return new RequestSpecBuilder()
                .setBaseUri("https://reqres.in/")
                .log(LogDetail.ALL)
                .build();
    }

    @Test
    void createUserTest() {

        ObjectNode jsonBody = new ObjectMapper().createObjectNode();
        jsonBody.put("name", name);
        jsonBody.put("job", job);

        given()
                .spec(specs())
                .contentType(JSON)
                .body(jsonBody)
                .when()
                .post("api/users")
                .then()
                .statusCode(201)
                .contentType(JSON)
                .body("name", is(name), "job", is(job));
    }

    @Test
    void registrationTest() {

        ObjectNode jsonBody = new ObjectMapper().createObjectNode();
        jsonBody.put("email", email);
        jsonBody.put("password", password);

        given()
                .spec(specs())
                .contentType(JSON)
                .body(jsonBody)
                .when()
                .post("api/register")
                .then()
                .statusCode(200)
                .contentType(JSON);
    }

    @Test
    void registrationUnsuccessfulTest() {

        ObjectNode jsonBody = new ObjectMapper().createObjectNode();
        jsonBody.put("email", email);

        given()
                .spec(specs())
                .contentType(JSON)
                .body(jsonBody)
                .when()
                .post("api/register")
                .then()
                .statusCode(400)
                .contentType(JSON)
                .body("error", is("Missing password"));
    }

    @Test
    void getSingleUserTest() {

        JsonPath jsonPath = given()
                .spec(specs())
                .param("page", 2)
                .when()
                .get("/api/users")
                .then()
                .statusCode(200)
                .contentType(JSON)
                .extract().body().jsonPath();

        assertThat(jsonPath.getList("data.email"))
                .contains("michael.lawson@reqres.in", "george.edwards@reqres.in");
    }

    @Test
    void loginTest() {

        ObjectNode jsonBody = new ObjectMapper().createObjectNode();
        jsonBody.put("email", email);
        jsonBody.put("password", password);

        String token = given()
                .spec(specs())
                .contentType(JSON)
                .body(jsonBody)
                .when()
                .post("api/register")
                .then()
                .statusCode(200)
                .contentType(JSON)
                .extract().body().jsonPath().getString("token");

        given()
                .spec(specs())
                .contentType(JSON)
                .body(jsonBody)
                .when()
                .post("api/login")
                .then()
                .statusCode(200)
                .contentType(JSON)
                .body("token", is(token));
    }
}
