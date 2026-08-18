package com.example.practicetests.delete;

import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;

public class BrokenLinksChecker {

    public static void main(String[] args) {
        WebDriver driver = new ChromeDriver();

        try {
            // The page to check — status_codes has predictable 200/301/404/500 links
            driver.get("https://the-internet.herokuapp.com/status_codes");

            // 1. Grab every anchor tag on the page
            List<WebElement> links = driver.findElements(By.tagName("a"));
            System.out.println("Found " + links.size() + " links on the page.\n");

            int brokenCount = 0;

            // 2. Check each link
            for (WebElement link : links) {
                String url = link.getAttribute("href");

                // Skip empty or non-HTTP links (mailto:, javascript:, #, etc.)
                if (url == null || url.isEmpty() || !url.startsWith("http")) {
                    continue;
                }

                try {
                    // 3. Send an HTTP request and read the response code
                    HttpURLConnection conn = (HttpURLConnection) new URL(url).openConnection();
                    conn.setRequestMethod("HEAD");                 // headers only — faster
                    conn.setRequestProperty("User-Agent", "Mozilla/5.0"); // avoid 403s from bot-blocking
                    //conn.setRequestProperty("Cookie", cookieHeader); //passing auth of a user already logged in
                    conn.connect();
                    int responseCode = conn.getResponseCode();

                    // 4. A code >= 400 means the link is broken
                    if (responseCode >= 400) {
                        System.out.println("BROKEN (" + responseCode + "): " + url);
                        brokenCount++;
                    } else {
                        System.out.println("OK     (" + responseCode + "): " + url);
                    }
                } catch (Exception e) {
                    System.out.println("ERROR  (" + e.getMessage() + "): " + url);
                    brokenCount++;
                }
            }

            System.out.println("\nDone. Broken links found: " + brokenCount);

        } finally {
            driver.quit();   // always close the browser
        }
    }
}
