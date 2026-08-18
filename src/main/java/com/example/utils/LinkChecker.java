package com.example.utils;

import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

/**
 * LinkChecker — validates links, images, and iframes for broken resources,
 * either on a single page or by CRAWLING a site.
 *
 * Crawl pattern: start at a seed URL, check every link/image/iframe on the page,
 * then follow same-domain links to discover more pages — repeating until the
 * page queue is empty or a max-page cap is hit. A 'visited' set prevents loops,
 * and a 'checked' set means each resource URL is HTTP-checked only once across
 * the whole crawl.
 *
 * Selenium finds elements and navigates; a raw HTTP request validates each URL
 * (status >= 400 = broken). Browser session cookies are carried on each request
 * so resources behind auth aren't false 401/403s.
 */
public class LinkChecker {

    private LinkChecker() { }   // static-only utility; no instances

    /** One broken resource: URL, why, kind, and which page it was found on. */
    public static class BrokenResource {
        public final String url;
        public final String reason;   // "404" or an exception message
        public final String type;     // "link", "image", "iframe", or "page"
        public final String foundOn;  // the page URL where it was found

        public BrokenResource(String url, String reason, String type, String foundOn) {
            this.url = url;
            this.reason = reason;
            this.type = type;
            this.foundOn = foundOn;
        }

        @Override
        public String toString() {
            return "[" + type + "] " + reason + " -> " + url + "   (on " + foundOn + ")";
        }
    }

    // ---------------- single-page API ----------------

    public static List<BrokenResource> findBrokenLinks(WebDriver driver) {
        return checkTags(driver, new HashSet<>(), "a", "href", "link");
    }

    public static List<BrokenResource> findBrokenImages(WebDriver driver) {
        return checkTags(driver, new HashSet<>(), "img", "src", "image");
    }

    public static List<BrokenResource> findBrokenIframes(WebDriver driver) {
        return checkTags(driver, new HashSet<>(), "iframe", "src", "iframe");
    }

    /** Links + images + iframes on the current page, each URL checked once. */
    public static List<BrokenResource> findAllBroken(WebDriver driver) {
        Set<String> checked = new HashSet<>();
        List<BrokenResource> out = new ArrayList<>();
        out.addAll(checkTags(driver, checked, "a", "href", "link"));
        out.addAll(checkTags(driver, checked, "img", "src", "image"));
        out.addAll(checkTags(driver, checked, "iframe", "src", "iframe"));
        return out;
    }

    // ---------------- crawl API ----------------

    /**
     * Crawl from startUrl, checking links/images/iframes on every same-domain
     * page reached, up to maxPages. Returns all broken resources found.
     */
    public static List<BrokenResource> crawl(WebDriver driver, String startUrl, int maxPages) {
        String domain = hostOf(startUrl);

        Set<String> seen = new HashSet<>();     // every URL ever queued (avoids re-queuing)
        Set<String> visited = new HashSet<>();  // pages actually processed (caps at maxPages)
        Set<String> checked = new HashSet<>();  // resource URLs already HTTP-checked (dedup)
        Deque<String> queue = new ArrayDeque<>();

        queue.add(startUrl);
        seen.add(startUrl);

        List<BrokenResource> broken = new ArrayList<>();

        while (!queue.isEmpty() && visited.size() < maxPages) {
            String pageUrl = queue.poll();
            if (!visited.add(pageUrl)) {
                continue;   // already processed
            }

            try {
                driver.get(pageUrl);
            } catch (Exception e) {
                broken.add(new BrokenResource(pageUrl, "page load failed: " + e.getMessage(),
                        "page", pageUrl));
                continue;
            }

            // Check this page's resources (dedup HTTP checks via the shared 'checked' set).
            broken.addAll(checkTags(driver, checked, "a", "href", "link"));
            broken.addAll(checkTags(driver, checked, "img", "src", "image"));
            broken.addAll(checkTags(driver, checked, "iframe", "src", "iframe"));

            // Discover more pages: enqueue same-domain links not seen yet.
            for (WebElement a : driver.findElements(By.tagName("a"))) {
                String href = a.getAttribute("href");
                if (href == null || !href.startsWith("http")) {
                    continue;
                }
                href = stripFragment(href);
                if (sameDomain(href, domain) && seen.add(href)) {
                    queue.add(href);
                }
            }
        }
        return broken;
    }

    // ---------------- core check ----------------

    /** Check every <tag> on the current page; skip URLs already in 'checked'. */
    private static List<BrokenResource> checkTags(WebDriver driver, Set<String> checked,
                                                  String tag, String urlAttr, String type) {
        String cookieHeader = driver.manage().getCookies().stream()
                .map(c -> c.getName() + "=" + c.getValue())
                .collect(Collectors.joining("; "));
        String foundOn = driver.getCurrentUrl();

        List<BrokenResource> broken = new ArrayList<>();

        for (WebElement el : driver.findElements(By.tagName(tag))) {
            String url = el.getAttribute(urlAttr);

            // Skip null, empty, non-HTTP, and data: URLs (inline base64).
            if (url == null || url.isEmpty()
                    || !url.startsWith("http") || url.startsWith("data:")) {
                continue;
            }
            if (!checked.add(url)) {
                continue;   // already checked this URL somewhere in the crawl
            }

            try {
                HttpURLConnection conn = (HttpURLConnection) new URL(url).openConnection();
                conn.setRequestMethod("HEAD");
                conn.setRequestProperty("User-Agent", "Mozilla/5.0");
                if (!cookieHeader.isEmpty()) {
                    conn.setRequestProperty("Cookie", cookieHeader);
                }
                conn.connect();

                int code = conn.getResponseCode();
                if (code >= 400) {
                    broken.add(new BrokenResource(url, String.valueOf(code), type, foundOn));
                }
            } catch (Exception e) {
                broken.add(new BrokenResource(url, e.getMessage(), type, foundOn));
            }
        }
        return broken;
    }

    // ---------------- helpers ----------------

    private static String hostOf(String url) {
        try {
            return new URI(url).getHost();
        } catch (Exception e) {
            return "";
        }
    }

    private static boolean sameDomain(String url, String domain) {
        String h = hostOf(url);
        return h != null && h.equalsIgnoreCase(domain);
    }

    private static String stripFragment(String url) {
        int i = url.indexOf('#');
        return i >= 0 ? url.substring(0, i) : url;
    }
}
