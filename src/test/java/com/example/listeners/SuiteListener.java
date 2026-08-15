package com.example.listeners;

import com.example.reports.ExtentManager;
import org.testng.ISuite;
import org.testng.ISuiteListener;

/**
 * ISuiteListener — fires ONCE around the whole suite, not per test.
 *
 *   onStart(ISuite)  -> runs before any test in the suite
 *   onFinish(ISuite) -> runs after the last test in the suite
 *
 * This is the cleanest place to own the report LIFECYCLE: create the Extent
 * report once at the start of the run, and flush() it once at the very end.
 * Doing the flush here (rather than in ITestListener.onFinish) is more correct
 * because a suite can contain multiple <test> tags — you want to write the HTML
 * exactly once, after everything has run.
 */
public class SuiteListener implements ISuiteListener {

    @Override
    public void onStart(ISuite suite) {
        System.out.println("Starting suite: " + suite.getName());
        ExtentManager.getInstance();   // initialize the report up front
    }

    @Override
    public void onFinish(ISuite suite) {
        System.out.println("Finished suite: " + suite.getName());
        ExtentManager.getInstance().flush();   // write the HTML once, at the end
    }
}

