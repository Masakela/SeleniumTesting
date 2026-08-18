package com.example.base;

import java.time.Duration;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Parameters;
import org.testng.annotations.Optional;

/**
 * Every test class extends BaseTest, so driver setup/teardown lives in ONE place.
 *
 * PARALLEL-SAFE: the driver is held in a ThreadLocal, so each test thread gets
 * its OWN browser instance. With a plain static field, parallel tests would all
 * share one browser and corrupt each other's sessions. Access the driver through
 * getDriver() everywhere (tests, page objects, listeners).
 *
 *  @BeforeMethod -> fresh browser for THIS thread before each test
 *  @AfterMethod  -> quit AND remove() so a reused thread doesn't inherit a stale driver
 */
public abstract class BaseTest {

    // One holder, but each thread stores/reads its own WebDriver inside it.
    private static final ThreadLocal<WebDriver> DRIVER = new ThreadLocal<>();

    /** The current thread's driver — use this everywhere instead of a field. */
    public WebDriver getDriver() {
        return DRIVER.get();
    }

    @BeforeMethod
    @Parameters("browser")
    public void setUp(@Optional("chrome") String browser) {
        // System property wins over the testng.xml parameter, so CI can override.
        String target = System.getProperty("browser", browser);

        WebDriver drv;
        switch (target.toLowerCase()) {
            case "firefox" -> drv = new FirefoxDriver();
            default -> {
                ChromeOptions options = new ChromeOptions();
                options.addArguments("--headless=new");   // comment out to watch it run locally
                options.addArguments("--window-size=1920,1080");
                drv = new ChromeDriver(options);
            }
        }
        // Baseline implicit wait; explicit waits (in BasePage) do the real work.
        drv.manage().timeouts().implicitlyWait(Duration.ofSeconds(2));

        DRIVER.set(drv);   // store THIS thread's driver
    }

    @AfterMethod(alwaysRun = true)
    public void tearDown() {
        WebDriver drv = DRIVER.get();
        if (drv != null) {
            drv.quit();      // close this thread's browser
            DRIVER.remove(); // clear the ThreadLocal so a reused thread starts clean
        }
    }
}
