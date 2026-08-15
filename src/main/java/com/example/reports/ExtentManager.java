package com.example.reports;

import com.aventstack.extentreports.ExtentReports;
import com.aventstack.extentreports.reporter.ExtentSparkReporter;

public class ExtentManager {
    private static ExtentReports extent;

    public static ExtentReports getInstance() {
        if (extent == null) {
            ExtentSparkReporter spark =
                    new ExtentSparkReporter("test-output/extent/ExtentReport.html");
            spark.config().setDocumentTitle("Login Test Report");
            spark.config().setReportName("Login Suite");
            extent = new ExtentReports();
            extent.attachReporter(spark);
        }
        return extent;
    }
}
