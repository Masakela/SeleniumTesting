package com.example.listeners;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import org.testng.IAnnotationTransformer;
import org.testng.annotations.ITestAnnotation;

/**
 * IAnnotationTransformer — modifies test annotations at runtime.
 *
 * Without this, you'd have to write @Test(retryAnalyzer = RetryAnalyzer.class)
 * on every single test method. This listener instead attaches RetryAnalyzer to
 * EVERY @Test in the suite from one place — so retry behavior is applied
 * suite-wide with zero per-test annotation. Register it once (in the suite XML)
 * and it transforms every test's annotation as TestNG reads it.
 */
public class RetryListener implements IAnnotationTransformer {

    @Override
    public void transform(ITestAnnotation annotation, Class testClass,
                          Constructor testConstructor, Method testMethod) {
        // Only set our analyzer if the test hasn't already declared its own.
        if (annotation.getRetryAnalyzerClass() == null) {
            annotation.setRetryAnalyzer(RetryAnalyzer.class);
        }
    }
}

