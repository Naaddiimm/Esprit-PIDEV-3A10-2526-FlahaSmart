# Database Connection Fix Guide

## Problem
The application gets "No suitable driver found for jdbc:mysql" error when trying to add articles.

## Root Cause
The MySQL JDBC driver is not being loaded or is not available in the classpath.

## Solutions to Try (in order)

### Solution 1: Clean Rebuild (RECOMMENDED - Try This First)

1. **Delete Maven cache:**
   - Delete the `.m2/repository/com/mysql` folder in your local repository
   - On Windows: `C:\Users\[USERNAME]\.m2\repository\com\mysql`
   - On Mac/Linux: `~/.m2/repository/com/mysql`

2. **Clean and rebuild:**
   ```bash
   mvn clean install
   ```

3. **Run the DatabaseTester to verify:**
   ```bash
   mvn exec:java -Dexec.mainClass="com.example.flahasmarty.DatabaseTester"
   ```

### Solution 2: Ensure MySQL is Running

1. **Start MySQL server:**
   - Windows: Open XAMPP Control Panel and click Start next to MySQL
   - Mac: `brew services start mysql@8.0`
   - Linux: `sudo systemctl start mysql`

2. **Verify connection:**
   - MySQL should be listening on `localhost:3306`

### Solution 3: Create the Database (if not exists)

Run this SQL command in MySQL:

```sql
CREATE DATABASE IF NOT EXISTS flahasmart;

USE flahasmart;

CREATE TABLE IF NOT EXISTS articles (
    id_article INT PRIMARY KEY AUTO_INCREMENT,
    nom VARCHAR(255) NOT NULL,
    description TEXT,
    categorie VARCHAR(100),
    prix DECIMAL(10, 2),
    stock INT DEFAULT 0,
    poids DECIMAL(8, 2),
    unite VARCHAR(50),
    image_url VARCHAR(500),
    id_user INT DEFAULT 1,
    date_creation TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
```

### Solution 4: Update POM.XML Manually

If the Maven dependency isn't updating, manually edit `pom.xml`:

```xml
<dependency>
    <groupId>com.mysql</groupId>
    <artifactId>mysql-connector-j</artifactId>
    <version>9.0.0</version>
</dependency>
```

Then rebuild with:
```bash
mvn clean install -DskipTests
```

### Solution 5: Check IDE Configuration (IntelliJ)

1. Go to **File → Project Structure**
2. Click **Libraries** on the left
3. Check if `mysql-connector-j-9.0.0.jar` is listed
4. If not, click the **+** button and add: `.m2/repository/com/mysql/mysql-connector-j/9.0.0/mysql-connector-j-9.0.0.jar`

## Testing Steps

1. **Run DatabaseTester:**
   ```bash
   mvn exec:java -Dexec.mainClass="com.example.flahasmarty.DatabaseTester"
   ```
   This will tell you:
   - If the MySQL driver is loaded ✅
   - If the database connection works ✅
   - If the tables exist ✅

2. **Check the output:**
   - You should see all three tests pass with ✅
   - If any test fails, it will show the specific issue

## Console Output to Look For

### If Everything is Working:
```
=== DATABASE CONNECTION TEST ===

[TEST 1] Checking MySQL JDBC Driver...
  ✅ MySQL JDBC Driver (com.mysql.cj.jdbc.Driver) found!

[TEST 2] Testing Database Connection...
  ✅ Successfully connected to database!
     Database Product: MySQL
     Database Version: ...
     
[TEST 3] Checking Tables...
  Found tables:
    - articles
  ✅ 'articles' table exists!

=== TEST COMPLETE ===
```

### If Driver is Missing:
```
[TEST 1] Checking MySQL JDBC Driver...
  ❌ MySQL JDBC Driver NOT found!
  → Fix: Run `mvn clean install` and check pom.xml
```

### If Connection Fails:
```
[TEST 2] Testing Database Connection...
  ❌ Failed to connect to database!
     → Check if MySQL server is running on localhost:3306
     → Check if 'flahasmart' database exists
     → Check username/password (root/empty)
```

## After Fixing

1. Run the DatabaseTester and verify all tests pass
2. Close and restart the JavaFX application
3. Try adding an article
4. Check your MySQL database to verify the data was saved

## Still Not Working?

If you've tried all solutions:

1. **Check MySQL credentials in DBConnection.java:**
   ```java
   private static final String USER = "root";
   private static final String PASSWORD = "";
   ```
   Change if your MySQL uses different credentials

2. **Check MySQL port:**
   Default is `3306`. If different, change in:
   ```java
   private static final String URL = "jdbc:mysql://localhost:3306/...";
   ```

3. **Enable MySQL network access:**
   Make sure MySQL is bound to `localhost` or `127.0.0.1`

## Key Changes Made

1. ✅ Added explicit MySQL driver loading: `Class.forName("com.mysql.cj.jdbc.Driver")`
2. ✅ Updated to `mysql-connector-j` 9.0.0 (latest driver)
3. ✅ Added null checking for database connections
4. ✅ Added detailed debug logging
5. ✅ Created DatabaseTester for easy verification
