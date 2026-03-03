package com.example.flahasmarty;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;

public class DatabaseTester {
    
    public static void main(String[] args) {
        System.out.println("\n=== DATABASE CONNECTION TEST ===\n");
        
        // Test 1: Check if MySQL driver is available
        testMySQLDriver();
        
        // Test 2: Try to connect to database
        testDatabaseConnection();
        
        // Test 3: Check if tables exist
        testTableExistence();
        
        System.out.println("\n=== TEST COMPLETE ===\n");
    }
    
    private static void testMySQLDriver() {
        System.out.println("[TEST 1] Checking MySQL JDBC Driver...");
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            System.out.println("  ✅ MySQL JDBC Driver (com.mysql.cj.jdbc.Driver) found!");
        } catch (ClassNotFoundException e) {
            System.out.println("  ❌ MySQL JDBC Driver NOT found!");
            System.out.println("     Make sure mysql-connector-j is in your classpath");
            e.printStackTrace();
        }
    }
    
    private static void testDatabaseConnection() {
        System.out.println("\n[TEST 2] Testing Database Connection...");
        Connection conn = null;
        try {
            conn = DBConnection.getConnection();
            if (conn != null && !conn.isClosed()) {
                System.out.println("  ✅ Successfully connected to database!");
                
                // Get database info
                DatabaseMetaData metaData = conn.getMetaData();
                System.out.println("     Database Product: " + metaData.getDatabaseProductName());
                System.out.println("     Database Version: " + metaData.getDatabaseProductVersion());
                System.out.println("     Driver Name: " + metaData.getDriverName());
                System.out.println("     Driver Version: " + metaData.getDriverVersion());
            } else {
                System.out.println("  ❌ Connection is null or closed!");
            }
        } catch (Exception e) {
            System.out.println("  ❌ Failed to connect to database!");
            System.out.println("     Check if MySQL server is running on localhost:3306");
            System.out.println("     Check if 'flahasmart' database exists");
            System.out.println("     Check username/password (root/empty)");
            e.printStackTrace();
        } finally {
            try {
                if (conn != null) {
                    conn.close();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
    
    private static void testTableExistence() {
        System.out.println("\n[TEST 3] Checking Tables...");
        Connection conn = null;
        try {
            conn = DBConnection.getConnection();
            if (conn != null && !conn.isClosed()) {
                DatabaseMetaData metaData = conn.getMetaData();
                ResultSet tables = metaData.getTables(null, null, "%", new String[]{"TABLE"});
                
                System.out.println("  Found tables:");
                boolean hasArticles = false;
                while (tables.next()) {
                    String tableName = tables.getString("TABLE_NAME");
                    System.out.println("    - " + tableName);
                    if (tableName.equals("articles")) {
                        hasArticles = true;
                    }
                }
                
                if (hasArticles) {
                    System.out.println("  ✅ 'articles' table exists!");
                } else {
                    System.out.println("  ❌ 'articles' table NOT found!");
                }
            }
        } catch (Exception e) {
            System.out.println("  ❌ Error checking tables!");
            e.printStackTrace();
        } finally {
            try {
                if (conn != null) {
                    conn.close();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
