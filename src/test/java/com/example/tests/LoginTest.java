package com.example.tests;

import com.example.base.BaseTest;
import com.example.data.LoginData;
import com.example.pages.InventoryPage;
import com.example.pages.LoginPage;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * Tests read like user stories because the page objects hide the mechanics.
 * Note the mix: a positive (happy path) AND negatives — a mature suite spends
 * more effort on negatives than on the happy path.
 */
public class LoginTest extends BaseTest {

    @Test(description = "Valid credentials land on the inventory page", dataProvider = "goodCredentialsExcel",
    		dataProviderClass = LoginData.class)
    public void validLoginSucceeds(String username, String password) {
        InventoryPage inventory = new LoginPage(getDriver())
                .open()
                .loginAs(username, password);

        Assert.assertTrue(inventory.isLoaded(),
                "Inventory page should load after a valid login");
    }

    @Test(description = "A locked-out user is rejected with an error",
          dataProvider = "badCredentialsExcel", dataProviderClass = LoginData.class)
    public void invalidLoginShowsError(String user, String pass, String expectedFragment) {
        String error = new LoginPage(getDriver())
                .open()
                .loginExpectingFailure(user, pass)
                .getErrorMessage();

        Assert.assertTrue(error.contains(expectedFragment),
                "Expected error containing '" + expectedFragment + "' but got: " + error);
    }
    
	/*
	 * @Test(description = "Checking page header") public void headerCheck() {
	 * 
	 * LoginPage lp = new LoginPage(driver).open(); String header =
	 * lp.headerChecker();
	 * 
	 * Assert.assertEquals(header, "Swag Labs"); }
	 */
    //@Test(description = "Checking page header")
	/*
	 * public void AcceptedUsernamesHeading() {
	 * 
	 * LoginPage lp = new LoginPage(driver).open(); String actual =
	 * lp.getAcceptedUsernamesHeading();
	 * 
	 * Assert.assertTrue(actual.contains("Accepted usernames"),
	 * "Expected heading to contain 'Accepted usernames' but got: " + actual); }
	 */
    
    @Test(description = "Accepted usernames/password panel: heading and list are correct")
    public void acceptedUsernamesPanel() {
      
    	LoginPage lp = new LoginPage(getDriver()).open();
    	
    	Assert.assertEquals(lp.headerChecker(), 
    			LoginData.LOGIN_PAGE_HEADER, "Login Page header is a mismatch");

    	Assert.assertEquals(lp.getAcceptedUsernamesHeading(),
                LoginData.ACCEPTED_USERNAMES_HEADING, "Username Heading mismatch");

        Assert.assertEquals(lp.getAcceptedUsernames(),
                LoginData.acceptedUsernames(), "Username list mismatch");
        
        Assert.assertEquals(lp.getUserPasswordHeader(), 
        		LoginData.PASSWORD_FOR_ALL_USERS, "Password header is a mismatch");
        
        Assert.assertEquals(lp.getPasswordValue(), 
        		LoginData.PASSWORD, "Password(s) is a mismatch");
    }
}