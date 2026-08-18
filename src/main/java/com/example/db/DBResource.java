package com.example.db;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * DBResource — a single place the framework goes to talk to the database.
 *
 * The CONNECTION POOL is stubbed out with placeholders (see getConnection /
 * initPool below) so you can drop in a real pool later — HikariCP, Apache DBCP,
 * a JNDI DataSource, or plain DriverManager — without touching the query methods
 * or any test that uses them.
 *
 * The query API (query / queryForValue / update) is fully implemented so you can
 * write DB-validation steps today and swap in the real pool when it's available.
 *
 * Typical use in a test:
 *     List<Map<String,Object>> rows =
 *         DBResource.query("SELECT status FROM payments WHERE id = ?", paymentId);
 *     assertEquals(rows.get(0).get("status"), "SETTLED");
 */
public class DBResource {

    // === Configuration placeholders — replace with real values / a config file ===
    private static final String JDBC_URL  = "jdbc:placeholder://host:port/database";
    private static final String USERNAME  = "REPLACE_ME";
    private static final String PASSWORD  = "REPLACE_ME";
    // e.g. "org.postgresql.Driver", "com.microsoft.sqlserver.jdbc.SQLServerDriver"
    private static final String DRIVER    = "REPLACE_ME";

    private DBResource() { }   // static-only resource; no instances

    // ---------------------------------------------------------------------------
    // CONNECTION POOL — PLACEHOLDER
    //
    // Replace the body of initPool() and getConnection() with your real pool.
    // Everything below (query/update) already calls getConnection(), so once
    // this returns a live Connection from the pool, the whole class works.
    // ---------------------------------------------------------------------------

    /** Call once at suite start (e.g. from a @BeforeSuite) to build the pool. */
    public static void initPool() {
        // TODO: initialize the real connection pool here. Example (HikariCP):
        //   HikariConfig cfg = new HikariConfig();
        //   cfg.setJdbcUrl(JDBC_URL);
        //   cfg.setUsername(USERNAME);
        //   cfg.setPassword(PASSWORD);
        //   cfg.setMaximumPoolSize(10);
        //   dataSource = new HikariDataSource(cfg);
        throw new UnsupportedOperationException(
                "Connection pool not implemented — wire up a real DataSource in initPool().");
    }

    /** Borrow a connection from the pool. Placeholder for now. */
    private static Connection getConnection() {
        // TODO: return dataSource.getConnection();  (real pool)
        // Or, without a pool, a plain DriverManager connection:
        //   Class.forName(DRIVER);
        //   return DriverManager.getConnection(JDBC_URL, USERNAME, PASSWORD);
        throw new UnsupportedOperationException(
                "getConnection() is a placeholder — implement it against your pool/DataSource.");
    }

    /** Call once at suite end (e.g. from a @AfterSuite) to close the pool. */
    public static void closePool() {
        // TODO: dataSource.close();  (real pool)
    }

    // ---------------------------------------------------------------------------
    // QUERY API — fully implemented; works as soon as getConnection() is real
    // ---------------------------------------------------------------------------

    /**
     * Run a SELECT and return the rows as a list of column-name -> value maps.
     * Uses a PreparedStatement, so pass query params as varargs to avoid SQL
     * injection and quoting issues.
     */
    public static List<Map<String, Object>> query(String sql, Object... params) {
        List<Map<String, Object>> rows = new ArrayList<>();
        // try-with-resources closes Connection, Statement, and ResultSet automatically.
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            bindParams(ps, params);
            try (ResultSet rs = ps.executeQuery()) {
                ResultSetMetaData meta = rs.getMetaData();
                int cols = meta.getColumnCount();
                while (rs.next()) {
                    Map<String, Object> row = new LinkedHashMap<>();
                    for (int c = 1; c <= cols; c++) {
                        row.put(meta.getColumnLabel(c), rs.getObject(c));
                    }
                    rows.add(row);
                }
            }
        } catch (Exception e) {
            throw new RuntimeException("Query failed: " + sql, e);
        }
        return rows;
    }

    /** Convenience: return the first column of the first row (e.g. a COUNT or a single value). */
    public static Object queryForValue(String sql, Object... params) {
        List<Map<String, Object>> rows = query(sql, params);
        if (rows.isEmpty()) return null;
        return rows.get(0).values().iterator().next();
    }

    /** Run an INSERT/UPDATE/DELETE and return the affected row count (for test data setup/teardown). */
    public static int update(String sql, Object... params) {
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            bindParams(ps, params);
            return ps.executeUpdate();
        } catch (Exception e) {
            throw new RuntimeException("Update failed: " + sql, e);
        }
    }

    private static void bindParams(PreparedStatement ps, Object... params) throws Exception {
        for (int i = 0; i < params.length; i++) {
            ps.setObject(i + 1, params[i]);
        }
    }
}
