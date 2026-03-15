# Simple Start Guide - Fix Database Issue

## The Problem
Your application can't connect to PostgreSQL database.

## Solution: Run These 3 Commands

```bash
# 1. Create database (try this first)
sudo -u postgres createdb noveldb

# 2. If above fails, try this:
createdb noveldb

# 3. Start application
cd backend
mvn spring-boot:run
```

## If You Still Get Errors

The issue is PostgreSQL authentication. Here's the fix:

### Step 1: Check PostgreSQL is Running
```bash
sudo systemctl status postgresql
```

If not running:
```bash
sudo systemctl start postgresql
```

### Step 2: Create Database
```bash
# Try method 1:
sudo -u postgres createdb noveldb

# If that fails, try method 2:
sudo -u postgres psql
# Then type:
CREATE DATABASE noveldb;
\q
```

### Step 3: Fix Authentication (if needed)
```bash
# Edit PostgreSQL config
sudo nano /etc/postgresql/*/main/pg_hba.conf

# Find this line:
local   all             postgres                                peer

# Change to:
local   all             postgres                                trust

# Save and restart:
sudo systemctl restart postgresql
```

### Step 4: Try Again
```bash
sudo -u postgres createdb noveldb
cd backend
mvn spring-boot:run
```

## Alternative: Use H2 Database (In-Memory)

If PostgreSQL is too complicated, use H2 (no setup needed):

1. Update `backend/pom.xml`, add:
```xml
<dependency>
    <groupId>com.h2database</groupId>
    <artifactId>h2</artifactId>
    <scope>runtime</scope>
</dependency>
```

2. Update `backend/src/main/resources/application.yml`:
```yaml
spring:
  datasource:
    url: jdbc:h2:mem:noveldb
    username: sa
    password:
  h2:
    console:
      enabled: true
```

3. Start:
```bash
cd backend
mvn spring-boot:run
```

This will work immediately without any database setup!

## Quick Test

After database is created, test connection:
```bash
psql -U postgres -d noveldb -c "SELECT 1;"
```

If this works, your database is ready!

## Summary

**Easiest fix:**
```bash
sudo -u postgres createdb noveldb
cd backend
mvn spring-boot:run
```

**If that doesn't work, use H2 database (see Alternative above)**
