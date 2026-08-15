package com.example.base;

import java.time.Duration;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

/**
 * BasePage is the parent of every page object.
 *
 * Its whole job is to centralize the WebDriver + WebDriverWait and expose
 * safe, wait-backed helpers (click/type/read) so that NO page object ever
 * calls driver.findElement(...) raw or uses Thread.sleep(). Every interaction
 * goes through an EXPLICIT wait first. That single decision is what kills most
 * Selenium flakiness.
 */
public abstract class BasePage {

    protected final WebDriver driver;
    protected final WebDriverWait wait;

    protected BasePage(WebDriver driver) {
        this.driver = driver;
        // One explicit wait, reused by every helper below.
        this.wait = new WebDriverWait(driver, Duration.ofSeconds(10));
    }

    /** Wait until the element is clickable, then click it. */
    protected void click(By locator) {
        wait.until(ExpectedConditions.elementToBeClickable(locator)).click();
    }

    /** Wait until visible, clear it, then type. */
    protected void type(By locator, String text) {
        WebElement el = wait.until(ExpectedConditions.visibilityOfElementLocated(locator));
        el.clear();
        el.sendKeys(text);
    }

    /** Wait until visible, then return its text. */
    protected String getText(By locator) {
        return wait.until(ExpectedConditions.visibilityOfElementLocated(locator)).getText();
    }

    /** True if the element becomes visible within the wait window; false otherwise. */
    protected boolean isVisible(By locator) {
        try {
            wait.until(ExpectedConditions.visibilityOfElementLocated(locator));
            return true;
        } catch (org.openqa.selenium.TimeoutException e) {
            return false;
        }
    }
}
