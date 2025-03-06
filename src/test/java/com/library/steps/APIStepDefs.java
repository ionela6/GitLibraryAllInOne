package com.library.steps;

import com.library.pages.BookPage;
import com.library.utility.BrowserUtil;
import com.library.utility.DB_Util;
import com.library.utility.Driver;
import com.library.utility.LibraryAPI_Util;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;
import io.restassured.response.ValidatableResponse;
import io.restassured.specification.RequestSpecification;
import org.junit.Assert;
import org.openqa.selenium.By;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedCondition;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;
import java.util.*;


import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

public class APIStepDefs {
    String token;
    RequestSpecification givenPart;
    Map<String, Object> randomMap;
    ValidatableResponse thenPart;
    Response response;
    JsonPath jsonPath;
    String id;
    BookPage bookPage = new BookPage();

    @Given("I logged Library api as a {string}")
    public void i_logged_library_api_as_a(String userType) {
        token = LibraryAPI_Util.getToken(userType);
        givenPart = given().log().uri();
    }

    @Given("Accept header is {string}")
    public void accept_header_is(String acceptHeader) {
        givenPart.accept(acceptHeader).header("x-library-token", token);
    }

    @Given("Request Content Type header is {string}")
    public void request_content_type_header_is(String requestContentType) {
        givenPart.contentType(requestContentType);
    }

    @Given("I create a random {string} as request body")
    public void i_create_a_random_as_request_body(String itemType) {
        switch (itemType) {
            case "book":
                randomMap = LibraryAPI_Util.getRandomBookMap();
                break;
            case "user":
                randomMap = LibraryAPI_Util.getRandomUserMap();
                break;
            default:
                throw new IllegalArgumentException("Invalid item type");
        }

        givenPart.formParams(randomMap);


    }

    @When("I send POST request to {string} endpoint")
    public void i_send_post_request_to_endpoint(String endpoint) {
        response = givenPart.post(endpoint);
        thenPart = response.then();
        jsonPath = response.jsonPath();
        id = jsonPath.getString("book_id");
    }

    @Then("status code should be {int}")
    public void status_code_should_be(Integer statusCode) {
        thenPart.statusCode(statusCode);
    }

    @Then("Response Content type is {string}")
    public void response_content_type_is(String responseContentType) {
        thenPart.contentType(responseContentType);
    }

    @Then("the field value for {string} path should be equal to {string}")
    public void the_field_value_for_path_should_be_equal_to(String path, String expectedValue) {
        thenPart.body(path, is(expectedValue));
    }

    @Then("{string} field should not be null")
    public void field_should_not_be_null(String responseBodyField) {
        thenPart.body(responseBodyField, notNullValue());
    }


    @Then("UI, Database and API created book information must match")
    public void ui_database_and_api_created_book_information_must_match() {
//DB part
        DB_Util.runQuery("select isbn, B.name, author, BC.name, year from books B join book_categories BC on B.book_category_id = BC.id where B.id =" + id + ";");
        List<String> dbList = DB_Util.getRowDataAsList(1);
        Collections.sort(dbList);

//API part
        jsonPath = givenPart.get("/get_book_by_id/" + id).then().statusCode(200).extract().jsonPath();
        Map<String, String> apiMap = jsonPath.get("");
        String bookName = apiMap.get("name");
        String categoryId = apiMap.get("book_category_id");
        jsonPath = givenPart.get("/get_book_categories").then().statusCode(200).extract().jsonPath();
        String categoryName = jsonPath.getString("find { it.id == '" + categoryId + "' }.name");
        apiMap.replace("book_category_id", categoryName);
        apiMap.remove("id");
        apiMap.remove("description");
        apiMap.remove("added_date");
        List<String> apiList = new ArrayList<>(apiMap.values());
        Collections.sort(apiList);

//UI part
        bookPage.search.sendKeys(bookName + Keys.ENTER);
        BrowserUtil.waitFor(2);
        List<String> uiList = new ArrayList<>();

        for (WebElement webElement : bookPage.bookInfo) {
            uiList.add(webElement.getText());
        }
        Collections.sort(uiList);


        Assert.assertEquals(uiList, dbList);
        Assert.assertTrue(apiList.equals(uiList));
    }


}
