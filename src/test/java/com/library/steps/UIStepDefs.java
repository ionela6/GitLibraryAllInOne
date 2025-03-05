package com.library.steps;

import com.library.pages.LoginPage;
import com.library.utility.BrowserUtil;
import io.cucumber.java.en.Given;

public class UIStepDefs {
    LoginPage loginPage = new LoginPage();

    @Given("I logged in Library UI as {string}")
    public void i_logged_in_library_ui_as(String userType) {
        loginPage.login(userType);

    }

    @Given("I navigate to {string} page")
    public void i_navigate_to_page(String module) {
        loginPage.navigateModule(module);

    }
}
