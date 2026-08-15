package com.example.pages;

import com.example.base.BasePage;
import java.util.List;
import java.util.stream.Collectors;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

/**
 * The product-list page shown after a successful login.
 * Expanded from the skeleton stub so there's real behavior to test:
 * reading the product list, adding an item to the cart, and the cart badge.
 * (Target: https://www.saucedemo.com inventory page.)
 */
public class InventoryPage extends BasePage {

    private static final By INVENTORY_CONTAINER = By.id("inventory_container");
    private static final By ITEM_NAMES          = By.cssSelector(".inventory_item_name");
    private static final By CART_BADGE          = By.cssSelector(".shopping_cart_badge");

    public InventoryPage(WebDriver driver) {
        super(driver);
    }

    /** True once the product grid is visible — used to confirm login landed here. */
    public boolean isLoaded() {
        return isVisible(INVENTORY_CONTAINER);
    }

    /** How many products are listed. */
    public int getItemCount() {
        return driver.findElements(ITEM_NAMES).size();
    }

    /** The visible product names, in page order. */
    public List<String> getItemNames() {
        return driver.findElements(ITEM_NAMES)
                .stream()
                .map(WebElement::getText)
                .collect(Collectors.toList());
    }

    /**
     * Add a product to the cart by its display name. The "Add to cart" button's
     * id on this site is derived from the product name (lower-case, dashed),
     * e.g. "Sauce Labs Backpack" -> "add-to-cart-sauce-labs-backpack".
     */
    public InventoryPage addItemToCart(String productName) {
        String slug = productName.toLowerCase().replace(" ", "-");
        click(By.id("add-to-cart-" + slug));
        return this;
    }

    /** Cart badge count as an int; 0 when the badge isn't present (empty cart). */
    public int getCartCount() {
        List<WebElement> badge = driver.findElements(CART_BADGE);
        return badge.isEmpty() ? 0 : Integer.parseInt(badge.get(0).getText());
    }
}
