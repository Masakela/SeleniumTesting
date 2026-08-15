package com.example.pages;

import com.example.base.BasePage;

import java.util.List;
import java.util.stream.Collectors;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

/**
 * A Page Object models ONE page: its locators (private) and the actions a user
 * can take on it (public methods). Tests talk to these methods, never to raw
 * locators — so when the login page's HTML changes, THIS is the only file that
 * changes, not every test that logs in. That is the entire point of POM.
 *
 * Sample target: https://www.saucedemo.com (a public Selenium practice site).
 */
public class LoginPage extends BasePage {

    // --- Locators: the ONLY place these selectors live ---
    private static final By USERNAME = By.id("user-name");
    private static final By PASSWORD = By.id("password");
    private static final By LOGIN_BTN = By.id("login-button");
    private static final By LOGIN_HEADER = By.cssSelector(".login_logo");
    private static final By CREDENTIALS_HEADING = By.cssSelector(".login_credentials h4");
    private static final By ACCEPTED_USERNAMES = By.cssSelector(".login_credentials");
    private static final By PASSWORD_HEADER = By.cssSelector(".login_password h4");
    private static final By LOGIN_PASSWORD = By.cssSelector(".login_password");
    private static final By ERROR_MSG = By.cssSelector("[data-test='error']");

    private static final String URL = "https://www.saucedemo.com/";

    public LoginPage(WebDriver driver) {
        super(driver);
    }

    public LoginPage open() {
        driver.get(URL);
        return this;
    }

    /**
     * A successful login lands on the inventory page, so the method RETURNS the
     * next page object. This "fluent" chaining is a common POM idiom and keeps
     * the test reading like a user story.
     */
    public InventoryPage loginAs(String username, String password) {
        type(USERNAME, username);
        type(PASSWORD, password);
        click(LOGIN_BTN);
        return new InventoryPage(driver);
    }

    /** For negative tests: log in but stay on the login page to read the error. */
    public LoginPage loginExpectingFailure(String username, String password) {
        type(USERNAME, username);
        type(PASSWORD, password);
        click(LOGIN_BTN);
        return this;
    }
    
    public String headerChecker() {
    	return getText(LOGIN_HEADER);
    }
    
    public String getAcceptedUsernamesHeading() {
    	return getText(CREDENTIALS_HEADING);
    }
    
    public List<String> getAcceptedUsernames(){
    	
    	String raw = getText(ACCEPTED_USERNAMES);   // whole block incl. the heading
        return raw.lines()                          // split on newlines -> Stream<String>
                  .map(String::trim)                // drop stray whitespace
                  .filter(line -> !line.isBlank())  // skip empty lines
                  .filter(line -> !line.startsWith("Accepted usernames")) // drop the heading
                  .collect(Collectors.toList());
    }
    
    public String getUserPasswordHeader() {
    	return getText(PASSWORD_HEADER);
    }
    
    public String getPasswordValue() {
        return getText(LOGIN_PASSWORD).lines()
                .map(String::trim)
                .filter(line -> !line.isBlank())
                .filter(line -> !line.startsWith("Password for all users"))
                .findFirst()
                .orElse("");
    }
    
    public String getErrorMessage() {
        return getText(ERROR_MSG);
    }
}
