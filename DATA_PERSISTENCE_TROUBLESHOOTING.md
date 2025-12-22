# Troubleshooting: Data Deletion on Application Restart

## Problem
Your database data is being deleted every time you restart the Spring Boot application.

## Root Cause Analysis

### ✅ Confirmed: Main Configuration is Correct
Your `src/main/resources/application.yml` has the correct setting:
```yaml
spring:
  jpa:
    hibernate:
      ddl-auto: update  # ✅ Correct - data should persist
```

### ⚠️ Potential Issues

#### Issue 1: Running Tests Instead of Main Application
The test configuration (`src/test/resources/application.yml`) has `create-drop`:
```yaml
spring:
  jpa:
    hibernate:
      ddl-auto: create-drop  # This is ONLY for tests
```

**Solution**: Make sure you're running the main application, not tests.

## How to Fix

### Option 1: Run from Command Line (Recommended)
```bash
# Navigate to project directory
cd "c:\Users\Andrew\Desktop\Bootcamp\Spring Java Bootcamp\Java Spring Bootcamp"

# Run the application (NOT tests)
mvn spring-boot:run
```

### Option 2: Run from IDE

#### IntelliJ IDEA:
1. Right-click on your main class (the one with `@SpringBootApplication`)
2. Select **"Run 'DemoApplication.main()'"** (NOT "Run with Coverage" or "Run Tests")
3. Check the run configuration:
   - Click **Run → Edit Configurations**
   - Ensure it's pointing to your main class
   - Verify "Use classpath of module" is set to your main module

#### VS Code:
1. Open the main application class
2. Click the **"Run"** button above the `main` method
3. Or use the Spring Boot Dashboard extension

### Option 3: Verify Application Startup Logs

When you start the application, check the console output for:

```
Hibernate: drop table if exists ...  ❌ BAD - means create-drop is active
```

vs

```
Hibernate: create table if not exists ...  ✅ GOOD - means update is active
```

## Verification Steps

### Step 1: Check Which Configuration is Loaded
Add this to your main application class temporarily:

```java
@SpringBootApplication
public class DemoApplication {
    
    @Autowired
    private Environment env;
    
    public static void main(String[] args) {
        SpringApplication.run(DemoApplication.class, args);
    }
    
    @PostConstruct
    public void logConfig() {
        String ddlAuto = env.getProperty("spring.jpa.hibernate.ddl-auto");
        System.out.println("========================================");
        System.out.println("🔍 DDL-AUTO SETTING: " + ddlAuto);
        System.out.println("========================================");
        
        if ("create-drop".equals(ddlAuto)) {
            System.err.println("⚠️ WARNING: Using create-drop! Data will be deleted!");
        } else if ("update".equals(ddlAuto)) {
            System.out.println("✅ Using update mode - data will persist");
        }
    }
}
```

### Step 2: Test Data Persistence

1. **Start the application**
2. **Create a test product** via API:
   ```bash
   curl -X POST http://localhost:8081/products \
     -H "Content-Type: application/json" \
     -H "Authorization: Bearer YOUR_TOKEN" \
     -d '{
       "code": "TEST-PERSIST",
       "name": "Persistence Test",
       "interestRate": 5.0,
       "interestRateType": "FIXED",
       "minAmount": 1000,
       "maxAmount": 10000,
       "minTenureMonths": 6,
       "maxTenureMonths": 12,
       "isActive": true
     }'
   ```

3. **Verify it exists**:
   ```bash
   curl http://localhost:8081/products
   ```

4. **Stop the application** (Ctrl+C)

5. **Restart the application**

6. **Check if data still exists**:
   ```bash
   curl http://localhost:8081/products
   ```

If the TEST-PERSIST product is still there → ✅ **Fixed!**  
If it's gone → ❌ **Still an issue**

## Additional Checks

### Check for Multiple Application Classes
```bash
# Search for @SpringBootApplication annotations
grep -r "@SpringBootApplication" src/main/java/
```

Make sure you only have ONE main application class.

### Check for Profile-Specific Configurations
```bash
# Look for profile-specific configs
ls src/main/resources/application-*.yml
```

If you have `application-dev.yml`, `application-prod.yml`, etc., check which profile is active.

### Check Active Spring Profile
Look in your logs for:
```
The following profiles are active: dev
```

Then check `application-dev.yml` for the ddl-auto setting.

## Still Not Working?

### Last Resort: Force the Setting in Code

Create a configuration class:

```java
package com.example.demo.config;

import org.springframework.boot.autoconfigure.orm.jpa.HibernatePropertiesCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class HibernateConfig {
    
    @Bean
    public HibernatePropertiesCustomizer hibernatePropertiesCustomizer() {
        return hibernateProperties -> {
            hibernateProperties.put("hibernate.hbm2ddl.auto", "update");
            System.out.println("✅ Forced Hibernate DDL-AUTO to: update");
        };
    }
}
```

This will **force** Hibernate to use `update` mode regardless of configuration files.

## Summary

Most likely, you're accidentally running tests or using a test run configuration. Follow these steps:

1. ✅ Use `mvn spring-boot:run` from command line
2. ✅ Check IDE run configuration points to main class
3. ✅ Verify logs show "update" mode on startup
4. ✅ Test data persistence with the steps above

If none of this works, share your startup logs and I'll help debug further!
