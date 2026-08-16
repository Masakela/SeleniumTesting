package com.example.base;

import java.time.Duration;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Parameters;

import com.example.listeners.WebDriverEventLogger;

import org.testng.annotations.Optional;
import org.openqa.selenium.support.events.EventFiringDecorator;

/**
 * Every test class extends BaseTest, so driver setup/teardown lives in ONE place.
 *
 *  @BeforeMethod  -> fresh browser before each test (isolation: no shared state)
 *  @AfterMethod   -> always quit, even on failure (no leaked browser processes)
 *
 * Browser is parameterized so CI can pass -Dbrowser=firefox or the testng.xml
 * <parameter> can drive cross-browser runs. Headless keeps CI machines happy.
 */
public abstract class BaseTest {

    protected WebDriver driver;
    
    public WebDriver getDriver() {
        return driver;
    }
    
    @BeforeMethod
    @Parameters("browser")
    public void setUp(@Optional("chrome") String browser) {
        // System property wins over the testng.xml parameter, so CI can override.
        String target = System.getProperty("browser", browser);

        WebDriver rawDriver = null;   // build the real driver first
        
        switch (target.toLowerCase()) {
            case "firefox" -> rawDriver = new FirefoxDriver();
            default -> {
                ChromeOptions options = new ChromeOptions();
                //options.addArguments("--headless=new");   // comment out to watch it run locally
                options.addArguments("--window-size=1920,1080");
                rawDriver = new ChromeDriver(options);
            }
        }
        // Baseline implicit wait as a safety net — explicit waits (in BasePage)
        // do the real work. Keep it small; never stack long implicit + explicit.
        
        driver = new EventFiringDecorator<>(new WebDriverEventLogger()).decorate(rawDriver);
        driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(2));
    }

    @AfterMethod(alwaysRun = true)
    public void tearDown() {
        if (driver != null) {
            driver.quit();
        }
    }
}
