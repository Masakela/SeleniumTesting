# Selenium Test Automation Framework

A Java UI-automation framework built on **Selenium 4**, **TestNG**, and **Maven**, using
the **Page Object Model**. It supports data-driven tests from Excel, Extent HTML
reporting with screenshots on failure, automatic retries, cross-browser runs, and
a Jenkins CI pipeline.

---

## Tech stack

| Concern            | Tool                                    |
|--------------------|-----------------------------------------|
| Language           | Java 17                                 |
| Browser automation | Selenium 4 (Selenium Manager auto-driver) |
| Test runner        | TestNG                                  |
| Build / deps       | Maven                                   |
| Test data          | Apache POI (Excel `.xlsx`)              |
| Reporting          | Extent Reports (Spark HTML)             |
| CI                 | Jenkins                                 |

---

## Project structure

```
selenium-framework/
├── pom.xml                     Maven build + dependencies
├── testng.xml                  Full suite definition
├── login.xml                   Login-only suite (+ registered listeners)
├── Jenkinsfile                 CI pipeline
│
├── src/main/java/com/example/
│   ├── base/
│   │   └── BasePage.java        Parent page object: driver + explicit-wait helpers
│   ├── pages/
│   │   ├── LoginPage.java       Login page: locators + actions
│   │   └── InventoryPage.java   Post-login page
│   ├── utils/
│   │   └── ExcelReader.java     Reads .xlsx test data via Apache POI
│   └── reports/
│       └── ExtentManager.java   Creates / owns the Extent report object
│
└── src/test/
    ├── java/com/example/
    │   ├── base/
    │   │   └── BaseTest.java     Driver setup/teardown (@BeforeMethod/@AfterMethod)
    │   ├── data/
    │   │   └── LoginData.java    Data providers (hardcoded + Excel-backed)
    │   ├── listeners/
    │   │   ├── TestListener.java       Screenshot + Extent log on pass/fail
    │   │   ├── SuiteListener.java       Init + flush the report once per suite
    │   │   ├── ConfigListener.java      Surfaces @Before/@After failures
    │   │   ├── RetryAnalyzer.java        Re-runs a failed test up to N times
    │   │   ├── RetryListener.java        Applies RetryAnalyzer to every @Test
    │   │   └── WebDriverEventLogger.java Logs browser actions (EventFiringDecorator)
    │   └── tests/
    │       ├── LoginTest.java     Login tests (data-driven)
    │       └── InventoryTest.java Inventory tests
    └── resources/
        └── logins.xlsx           Test data: GoodLogin + BadLogin sheets
```

---

## Architecture — layered by responsibility

Each layer has one job and depends only on the layers below it, which is what keeps
the framework maintainable.

| Layer              | What lives here                                             |
|--------------------|------------------------------------------------------------|
| **Test**           | `tests/` — TestNG `@Test` classes; assertions only, no locators |
| **Page**           | `pages/` — one class per screen; locators + action methods |
| **Base / core**    | `BasePage` (wait helpers), `BaseTest` (driver lifecycle)   |
| **Utilities**      | `utils/` (ExcelReader), `listeners/` (reporting, retry, logging) |
| **Test data & config** | `data/`, `resources/logins.xlsx`, `testng.xml` / `login.xml` |
| **Reporting**      | `ExtentManager` + `TestListener` / `SuiteListener`         |
| **Build & CI**     | `pom.xml`, `Jenkinsfile`                                    |

A UI change touches only the **page** layer; a new test touches only the **test**
layer; a browser-config change touches only the **base** layer.

---

## Prerequisites

- **JDK 17+** (`java -version`)
- **Maven** (`mvn -version`)
- A local **Chrome** or **Firefox** (Selenium Manager downloads the matching driver
  automatically — no manual driver setup)

---

## Setup

1. Clone the repo and open it in your IDE (Eclipse/IntelliJ) as a **Maven project**.
2. Let Maven resolve dependencies (first build downloads them).
3. Confirm the project root contains `pom.xml` — all Maven commands run from here.

---

## Running the tests

From the project root:

```bash
mvn clean test                          # runs the suite in testng.xml
mvn clean test -DsuiteXmlFile=login.xml # runs the login-only suite
mvn test -Dbrowser=firefox              # cross-browser (default: chrome)
mvn test -Dtest=LoginTest               # a single class (when not pinned to a suite)
```

**In Eclipse:** right-click `testng.xml` (or `login.xml`) → **Run As → TestNG Suite**.
Requires the TestNG plugin.

### Browser selection

Browser is resolved in this order (later wins):
`-Dbrowser=...` (command line) → `<parameter name="browser">` in the suite XML →
`chrome` (default). Headless mode is toggled in `BaseTest`.

---

## Test data (Excel)

Data-driven tests read from `src/test/resources/logins.xlsx`:

- **GoodLogin** sheet — columns: `username`, `password`
- **BadLogin** sheet — columns: `username`, `password`, `expectedError`

Row 1 of each sheet is a header (skipped by the reader); data starts at row 2.
`ExcelReader.readAsDataProvider(path, sheet)` returns an `Object[][]` for a
`@DataProvider`, and **the number of columns must match the test method's parameters**.

`LoginData` exposes both hardcoded and Excel-backed providers, so you can use either.

---

## Reporting

Extent Reports generates an HTML report at **`test-output/extent/ExtentReport.html`**.

- The report is created once per suite (`SuiteListener.onStart`) and flushed at the
  end (`SuiteListener.onFinish`).
- `TestListener` logs each test's pass/fail and, on failure, captures a screenshot
  to `test-output/screenshots/` and embeds it in the report.

---

## Retries (flaky-test handling)

`RetryAnalyzer` re-runs a failed test up to `MAX_RETRIES` (default **1**), and
`RetryListener` (an `IAnnotationTransformer`) attaches it to every `@Test` — so you
don't annotate tests individually. Change the count in `RetryAnalyzer`.

> Retries absorb genuine flakiness but can hide real intermittent bugs — the analyzer
> logs each retry so retried tests get noticed, not silently passed.

---

## Listeners

All registered in `login.xml` (`<listeners>` block):

| Listener                | Fires on                        | Purpose                                    |
|-------------------------|---------------------------------|--------------------------------------------|
| `TestListener`          | each `@Test` (start/pass/fail)  | Extent logging + screenshot on failure     |
| `SuiteListener`         | suite start / finish            | Init + flush the Extent report             |
| `ConfigListener`        | `@Before*` / `@After*` outcomes | Surfaces setup/teardown failures           |
| `RetryListener`         | annotation read time            | Applies `RetryAnalyzer` to every test      |
| `WebDriverEventLogger`  | every browser action            | Logs clicks/navigation (wired in `BaseTest`) |

---

## CI/CD (Jenkins)

`Jenkinsfile` defines a pipeline that:

1. Checks out the repo.
2. Runs `mvn clean test -DsuiteXmlFile=login.xml` (parameterized by `BROWSER`).
3. Publishes the Extent HTML report.
4. Archives failure screenshots as build artifacts.
5. Triggers on new commits (`pollSCM`) and nightly (`cron`).

**Jenkins prerequisites:** the **HTML Publisher** plugin, and Maven/JDK tool installs
named to match the `tools` block in the `Jenkinsfile`.

---

## Adding to the framework

**A new page:** create a class under `pages/` extending `BasePage`; keep locators
private and expose action methods that return the next page object.

**A new test:** create a class under `tests/` extending `BaseTest`; call page-object
methods and assert. Register the class in `testng.xml` (or a suite file).

**New test data:** add rows to the relevant sheet in `logins.xlsx` — no code change
needed for existing data-driven tests.

---

## Conventions

- Explicit waits only (via `BasePage`) — never `Thread.sleep()`.
- Resilient locators — prefer `id` / `data-*` / CSS over brittle absolute XPath.
- Tests never touch raw locators — they go through page-object methods.
- One responsibility per layer; nothing in a lower layer depends on the test layer.
