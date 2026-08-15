package com.example.listeners;

import com.aventstack.extentreports.ExtentReports;
import com.aventstack.extentreports.ExtentTest;
import com.aventstack.extentreports.Status;
import com.example.reports.ExtentManager;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;
import org.testng.ITestContext;
import org.testng.ITestListener;
import org.testng.ITestResult;

public class TestListener implements ITestListener {

    private static final ExtentReports extent = ExtentManager.getInstance();
    private static final ThreadLocal<ExtentTest> test = new ThreadLocal<>();

    @Override
    public void onTestStart(ITestResult result) {
        test.set(extent.createTest(result.getMethod().getMethodName()));
    }

    @Override
    public void onTestSuccess(ITestResult result) {
        test.get().log(Status.PASS, "Passed");
    }

    @Override
    public void onTestFailure(ITestResult result) {
        test.get().log(Status.FAIL, result.getThrowable());
        String path = captureScreenshot(result);
        if (path != null) {
            test.get().addScreenCaptureFromPath(path,
                    result.getMethod().getMethodName());
        }
    }

    @Override
    public void onTestSkipped(ITestResult result) {
        test.get().log(Status.SKIP, "Skipped");
    }

    @Override
    public void onFinish(ITestContext context) {
        extent.flush();   // writes the HTML — essential, or the report is empty
    }

    /** Pulls the WebDriver out of the test instance and saves a PNG. */
    private String captureScreenshot(ITestResult result) {
        try {
            // getDriver() must be exposed on BaseTest (see note)
            WebDriver driver = ((com.example.base.BaseTest) result.getInstance()).getDriver();
            if (driver == null) return null;

            File src = ((TakesScreenshot) driver).getScreenshotAs(OutputType.FILE);
            Path dest = Path.of("test-output/screenshots",
                    result.getMethod().getMethodName() + "_" + System.currentTimeMillis() + ".png");
            Files.createDirectories(dest.getParent());
            Files.copy(src.toPath(), dest);
            return dest.toString();
        } catch (Exception e) {
            System.err.println("Screenshot failed: " + e.getMessage());
            return null;
        }
    }
}
