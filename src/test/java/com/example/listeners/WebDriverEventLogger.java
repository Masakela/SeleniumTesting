package com.example.listeners;

import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.events.WebDriverListener;

public class WebDriverEventLogger implements WebDriverListener {

    @Override
    public void beforeClick(WebElement element) {
        System.out.println("About to click: " + element);
    }

    @Override
    public void afterClick(WebElement element) {
        System.out.println("Clicked: " + element);
    }

    @Override
    public void beforeGet(org.openqa.selenium.WebDriver driver, String url) {
        System.out.println("Navigating to: " + url);
    }

    @Override
    public void beforeFindElement(org.openqa.selenium.WebDriver driver, By locator) {
        System.out.println("Finding element: " + locator);
    }
}
