package com.example.listeners;

import org.testng.IRetryAnalyzer;
import org.testng.ITestResult;

/**
 * IRetryAnalyzer — automatic re-run of a FAILED test.
 *
 * TestNG calls retry(...) every time a test fails. Return true and TestNG runs
 * the SAME test again; return false and it accepts the failure. We cap the
 * number of retries so a genuinely-broken test doesn't loop forever.
 *
 * Why bother: UI tests are occasionally flaky for reasons unrelated to a real
 * bug (a slow network blip, a momentary render delay). A single retry filters
 * out that noise. BUT — retries can also HIDE real intermittent bugs, so the
 * honest practice is to retry AND flag it (see RetryListener), never to retry
 * silently and pretend the flake didn't happen.
 */
public class RetryAnalyzer implements IRetryAnalyzer {

    private int attempt = 0;
    private static final int MAX_RETRIES = 1;   // 1 retry = up to 2 total runs

    @Override
    public boolean retry(ITestResult result) {
        if (attempt < MAX_RETRIES) {
            attempt++;
            System.out.printf("Retrying %s (attempt %d of %d)%n",
                    result.getName(), attempt, MAX_RETRIES);
            return true;   // run it again
        }
        return false;      // out of retries — accept the failure
    }
}
