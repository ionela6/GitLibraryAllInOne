package com.library.steps;

import com.library.pages.LoginPage;
import com.library.utility.BrowserUtil;
import io.cucumber.java.en.And;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.When;
import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;
import io.restassured.response.ValidatableResponse;
import io.restassured.specification.RequestSpecification;
import org.junit.Assert;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static io.restassured.RestAssured.given;

public class UIStepDefs {
    LoginPage loginPage = new LoginPage();

    RequestSpecification givenPart = given().log().uri();

    Response response;
    ValidatableResponse thenPart;
    JsonPath jp;

    Map<String, Object> randomData= new HashMap<>();

    @Given("I logged in Library UI as {string}")
    public void i_logged_in_library_ui_as(String userType) {
        loginPage.login(userType);

    }

    @Given("I navigate to {string} page")
    public void i_navigate_to_page(String module) {
        loginPage.navigateModule(module);

    }



}
