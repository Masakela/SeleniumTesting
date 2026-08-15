package com.example.listeners;

import org.testng.IConfigurationListener;
import org.testng.ITestResult;

/**
 * IConfigurationListener — fires around CONFIGURATION methods
 * (@BeforeMethod, @BeforeClass, @AfterMethod, ...), which ITestListener does
 * NOT see (it only sees @Test methods).
 *
 * Why it's useful: when a @BeforeMethod fails (say the browser won't start),
 * TestNG normally just SKIPS the dependent test with almost no detail — a
 * confusing "why did everything skip?" moment. This listener catches that
 * setup failure explicitly and logs it, so the real cause is visible instead
 * of hidden behind a wall of skipped tests.
 */
public class ConfigListener implements IConfigurationListener {

    @Override
    public void onConfigurationSuccess(ITestResult result) {
        System.out.println("Setup/teardown OK: " + result.getName());
    }

    @Override
    public void onConfigurationFailure(ITestResult result) {
        System.err.println("Setup/teardown FAILED: " + result.getName()
                + " -> " + result.getThrowable());
    }
}

