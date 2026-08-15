# Selenium + TestNG + POM Skeleton

A minimal, runnable Java UI-automation framework — small enough to explain end to
end in an interview, complete enough to show you've built one.

## Structure & what each piece proves

```
pom.xml                          Maven build + deps; `mvn test` is the CI hook
testng.xml                       Suite config; parallel="methods" for speed
Jenkinsfile                      CI pipeline; runs mvn test, fails build on failure

src/main/java/com/example/
  base/BasePage.java             Parent page object: WebDriver + explicit-wait helpers
  pages/LoginPage.java           A page object: locators (private) + actions (public)
  pages/InventoryPage.java       Post-login page (chaining target)

src/test/java/com/example/
  base/BaseTest.java             Driver setup/teardown via @BeforeMethod/@AfterMethod
  tests/LoginTest.java           Positive + negative tests; DataProvider = data-driven
```

## The 60-second talk-through

- **Page Object Model** — each page is a class with private locators and public
  action methods. Tests never touch raw locators, so a UI change is a one-file
  fix. `LoginPage` is the example; it returns the next page object (`InventoryPage`)
  so tests read like a user story.
- **Explicit waits, centralized** — `BasePage` wraps every interaction in a
  `WebDriverWait` + `ExpectedConditions`. No `Thread.sleep()` anywhere. This is
  the single biggest anti-flakiness decision.
- **Isolation** — `BaseTest` spins up a fresh driver per test (`@BeforeMethod`)
  and always quits it (`@AfterMethod alwaysRun`). Fresh state per test is also
  what makes parallel execution safe.
- **Data-driven negatives** — the `@DataProvider` runs one test method across
  multiple bad-credential rows; that's how you scale negative coverage.
- **Speed** — `testng.xml` runs methods in parallel; scale further with Selenium
  Grid / a cloud grid using the same config.
- **CI as a gate** — the `Jenkinsfile` runs `mvn test` on every build and nightly;
  a failing test fails the build, so bad code can't merge. Browser is
  parameterized (`-Dbrowser=...`) for cross-browser runs.

## Run it locally

```
mvn test                    # headless Chrome (default)
mvn test -Dbrowser=firefox  # cross-browser
```

Requires JDK 17+ and a local Chrome/Firefox. Selenium 4's built-in Selenium
Manager auto-downloads the matching driver — no WebDriverManager needed.
```
