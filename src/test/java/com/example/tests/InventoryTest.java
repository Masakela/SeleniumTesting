package com.example.tests;

import com.example.base.BaseTest;
import com.example.pages.InventoryPage;
import com.example.pages.LoginPage;
import java.util.List;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/**
 * Tests for the inventory (product-list) page.
 *
 * These tests all START from a logged-in state, so instead of repeating the
 * login in every test we do it once in a local @BeforeMethod that runs AFTER
 * BaseTest's driver setup. `inventory` is then ready for each @Test.
 *
 * Note we DON'T re-test login here — that's LoginTest's job. Keeping each test
 * class focused on one area is what keeps a suite maintainable.
 */
public class InventoryTest extends BaseTest {

    private InventoryPage inventory;

    @BeforeMethod(dependsOnMethods = "setUp")
    public void loginFirst() {
        inventory = new LoginPage(driver)
                .open()
                .loginAs("standard_user", "secret_sauce");
    }

    @Test(description = "The inventory page loads after login")
    public void inventoryPageLoads() {
        Assert.assertTrue(inventory.isLoaded(),
                "Inventory container should be visible after login");
    }

    @Test(description = "The expected number of products is listed")
    public void showsAllProducts() {
        Assert.assertEquals(inventory.getItemCount(), 6,
                "SauceDemo lists six demo products");
    }

    @Test(description = "Adding an item increments the cart badge")
    public void addingItemUpdatesCartBadge() {
        Assert.assertEquals(inventory.getCartCount(), 0,
                "Cart should start empty");

        inventory.addItemToCart("Sauce Labs Backpack");

        Assert.assertEquals(inventory.getCartCount(), 1,
                "Cart badge should read 1 after adding one item");
    }

    @Test(description = "Adding two items shows a badge count of 2")
    public void addingTwoItemsShowsCountTwo() {
        inventory.addItemToCart("Sauce Labs Backpack");
        inventory.addItemToCart("Sauce Labs Bike Light");

        Assert.assertEquals(inventory.getCartCount(), 2,
                "Cart badge should reflect both items");
    }

    @Test(description = "Every listed product has a non-empty name")
    public void productNamesArePresent() {
        List<String> names = inventory.getItemNames();

        Assert.assertFalse(names.isEmpty(), "There should be product names");
        Assert.assertTrue(names.stream().noneMatch(String::isBlank),
                "No product name should be blank");
    }
}
