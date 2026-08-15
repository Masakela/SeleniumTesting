package com.example.data;
	
import java.util.List;

import org.testng.annotations.DataProvider;

	public class LoginData {
		
		public static final String ACCEPTED_USERNAMES_HEADING = "Accepted usernames are:";
		public static final String PASSWORD_FOR_ALL_USERS = "Password for all users:";
		public static final String PASSWORD = "secret_sauce";
		public static final String LOGIN_PAGE_HEADER = "Swag Labs";

		 /**
	     * DataProvider = TestNG's data-driven mechanism. The one @Test above runs
	     * once per row here — the way you scale negative coverage without copy-paste.
	     */
	    @DataProvider(name = "badCredentials")
	    public static Object[][] badCredentials() {
	        return new Object[][] {
	            {"locked_out_user", "secret_sauce",   "locked out"},
	            {"standard_user",   "wrong_password", "do not match"},
	            {"",                "secret_sauce",   "Username is required"},
	            {"standard_user", "", "Password is required"},
	            {"wrong_username", "wrong_password", "Username and password do not match any user in this service"},
	            {"wrong_username", "secret_sauce", "Username and password do not match any user in this service"}
	        };
	    }
	    
	    /**
	     * List of usernames provided at the bottom of the login page.
	     * You can alter this list if any changes were made to the UI.
	     */
	    public static List<String> acceptedUsernames() {
	        return List.of(
	                "standard_user",
	                "locked_out_user",
	                "problem_user",
	                "performance_glitch_user",
	                "error_user",
	                "visual_user"
	        );
	    } 
	    
	    public static List<String> acceptedPasswords() {
	        return List.of(
	                "secret_sauce"
	        );
	    } 
	}
	
	//Check for polling build every minute

