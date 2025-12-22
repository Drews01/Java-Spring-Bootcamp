# Redis Cache Implementation Guide

## Table of Contents
1. [Introduction](#introduction)
2. [Prerequisites](#prerequisites)
3. [Step-by-Step Implementation](#step-by-step-implementation)
4. [Testing Your Cache](#testing-your-cache)
5. [Implementing Cache for Other Entities](#implementing-cache-for-other-entities)
6. [Best Practices](#best-practices)
7. [Troubleshooting](#troubleshooting)

---

## Introduction

This guide demonstrates how to implement Redis caching in a Spring Boot application. Caching reduces database load by storing frequently accessed data in memory, significantly improving application performance.

### What is Redis?
Redis is an in-memory data structure store used as a database, cache, and message broker. It's extremely fast because data is stored in RAM.

### Benefits of Caching
- ⚡ **Performance**: Faster response times (microseconds vs milliseconds)
- 📉 **Reduced Database Load**: Fewer queries to your database
- 💰 **Cost Savings**: Lower database resource consumption
- 🚀 **Scalability**: Handle more concurrent users

---

## Prerequisites

Before starting, ensure you have:
- Spring Boot 4.0+ application
- Docker installed (for running Redis)
- Maven or Gradle build tool
- Basic understanding of Spring annotations

---

## Step-by-Step Implementation

### Step 1: Set Up Redis with Docker

Create a `docker-compose.yml` file in your project root:

```yaml
version: '3.8'

services:
  redis:
    image: redis:7-alpine
    container_name: redis-server
    ports:
      - "6379:6379"
    volumes:
      - redis_data:/data
    command: redis-server --appendonly yes
    restart: unless-stopped
    networks:
      - redis-network

  redis-insight:
    image: redis/redisinsight:latest
    container_name: redis-insight
    ports:
      - "5540:5540"
    volumes:
      - redis_insight_data:/data
    restart: unless-stopped
    depends_on:
      - redis
    networks:
      - redis-network

volumes:
  redis_data:
    driver: local
  redis_insight_data:
    driver: local

networks:
  redis-network:
    driver: bridge
```

Start Redis:
```bash
docker-compose up -d
```

Verify Redis is running:
```bash
docker ps
```

### Step 2: Add Dependencies

Add Redis dependencies to your `pom.xml`:

```xml
<dependencies>
    <!-- Redis Support -->
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-data-redis</artifactId>
    </dependency>
    
    <!-- Cache Abstraction -->
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-cache</artifactId>
    </dependency>
    
    <!-- JSON Serialization for Java 8 Date/Time -->
    <dependency>
        <groupId>com.fasterxml.jackson.datatype</groupId>
        <artifactId>jackson-datatype-jsr310</artifactId>
    </dependency>
</dependencies>
```

### Step 3: Configure Redis Connection

Add Redis configuration to `application.yml`:

```yaml
spring:
  # Redis Configuration
  data:
    redis:
      host: localhost
      port: 6379
      timeout: 60000
  
  # Cache Configuration
  cache:
    type: redis
    redis:
      time-to-live: 600000  # 10 minutes in milliseconds
```

### Step 4: Create Redis Configuration Class

Create `config/RedisConfig.java`:

```java
package com.example.demo.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import java.time.Duration;

@Configuration
@EnableCaching  // Enable Spring's annotation-driven cache management
public class RedisConfig {

    @Bean
    public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory connectionFactory) {
        RedisTemplate<String, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(connectionFactory);
        
        // Use String serializer for keys
        template.setKeySerializer(new StringRedisSerializer());
        template.setHashKeySerializer(new StringRedisSerializer());
        
        // Use JSON serializer for values
        template.setValueSerializer(RedisSerializer.json());
        template.setHashValueSerializer(RedisSerializer.json());
        
        template.afterPropertiesSet();
        return template;
    }

    @Bean
    public RedisCacheManager cacheManager(RedisConnectionFactory connectionFactory) {
        RedisCacheConfiguration config = RedisCacheConfiguration.defaultCacheConfig()
                .entryTtl(Duration.ofMinutes(10))  // Cache TTL: 10 minutes
                .serializeKeysWith(
                        RedisSerializationContext.SerializationPair.fromSerializer(new StringRedisSerializer())
                )
                .serializeValuesWith(
                        RedisSerializationContext.SerializationPair.fromSerializer(RedisSerializer.json())
                )
                .disableCachingNullValues();

        return RedisCacheManager.builder(connectionFactory)
                .cacheDefaults(config)
                .build();
    }
    
    @Bean
    public ObjectMapper objectMapper() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        return mapper;
    }
}
```

#### How `RedisConfig` Works

- **Class-level annotations**: `@Configuration` lets Spring discover this class during component scanning, while `@EnableCaching` switches on annotation-driven caching so `@Cacheable`, `@CacheEvict`, and friends actually hook into Redis.
- **`redisTemplate()` bean**: Builds the low-level helper used anywhere you need to interact with Redis manually. Keys (and hash keys) use `StringRedisSerializer` for human-readable entries, and values use the generic JSON serializer so complex objects round-trip cleanly.
- **`cacheManager()` bean**: Wires Spring's cache abstraction to Redis, defines a default TTL of 10 minutes, specifies the same key/value serializers used in the template, and turns off caching of `null` results to avoid polluting Redis with "not found" markers.
- **`objectMapper()` bean**: Registers `JavaTimeModule` and disables timestamp serialization so any entity containing `LocalDate`, `Instant`, etc., serializes/deserializes consistently—this mapper is reused by the Redis serializer under the hood.
- **End-to-end flow**: When a `@Cacheable` method is invoked, Spring asks `RedisCacheManager` for a cache. That cache reads/writes through the configured serializers, which in turn delegate to the Jackson `ObjectMapper`, ensuring byte-safe storage plus predictable JSON payloads in Redis.

### Step 5: Make Your Entity Serializable

Update your entity class (e.g., `Product.java`):

```java
import java.io.Serializable;

@Entity
@Table(name = "products")
@Data
public class Product implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    // ... other fields
}
```

### Step 6: Add Cache Annotations to Service Layer

Update your service class (e.g., `ProductService.java`):

```java
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

@Service
public class ProductService {
    
    // READ OPERATIONS - Cache the results
    
    @Cacheable(value = "products")
    public List<Product> getAllProducts() {
        return productRepository.findAll();
    }
    
    @Cacheable(value = "activeProducts")
    public List<Product> getActiveProducts() {
        return productRepository.findByIsActive(true);
    }
    
    @Cacheable(value = "productByCode", key = "#code")
    public Product getProductByCode(String code) {
        return productRepository.findByCode(code)
                .orElseThrow(() -> new IllegalArgumentException("Product not found"));
    }
    
    // WRITE OPERATIONS - Invalidate cache
    
    @CacheEvict(value = {"products", "activeProducts"}, allEntries = true)
    public Product createProduct(Product product) {
        // validation logic
        return productRepository.save(product);
    }
    
    @CacheEvict(value = {"products", "activeProducts", "productByCode"}, allEntries = true)
    public Product updateProductStatus(Long id, Boolean isActive) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Product not found"));
        product.setIsActive(isActive);
        return productRepository.save(product);
    }
}
```

---

## Testing Your Cache

### Manual Testing with cURL

#### Test 1: Cache Miss (First Request)
```bash
# First request - hits database
curl http://localhost:8081/products
```
**Expected**: Check application logs - you'll see SQL queries.

#### Test 2: Cache Hit (Second Request)
```bash
# Second request - from cache
curl http://localhost:8081/products
```
**Expected**: Check logs - NO SQL queries! Data served from Redis.

#### Test 3: Cache Invalidation
```bash
# Create a new product - clears cache
curl -X POST http://localhost:8081/products \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  -d '{
    "code": "TEST001",
    "name": "Test Product",
    "interestRate": 5.5,
    "interestRateType": "FIXED",
    "minAmount": 1000,
    "maxAmount": 50000,
    "minTenureMonths": 6,
    "maxTenureMonths": 36,
    "isActive": true
  }'

# Next GET request will hit database again
curl http://localhost:8081/products
```
**Expected**: SQL queries appear again in logs.

### Automated Testing

Create a test class `ProductServiceCacheTest.java`:

```java
@SpringBootTest
@AutoConfigureCache
class ProductServiceCacheTest {

    @Autowired
    private ProductService productService;
    
    @Autowired
    private CacheManager cacheManager;
    
    @BeforeEach
    void setUp() {
        // Clear all caches before each test
        cacheManager.getCacheNames()
                .forEach(cacheName -> cacheManager.getCache(cacheName).clear());
    }
    
    @Test
    void testProductCaching() {
        // First call - should hit database
        List<Product> products1 = productService.getAllProducts();
        
        // Second call - should hit cache
        List<Product> products2 = productService.getAllProducts();
        
        // Verify same instance (from cache)
        assertSame(products1, products2);
        
        // Verify cache contains data
        Cache cache = cacheManager.getCache("products");
        assertNotNull(cache.get("getAllProducts"));
    }
    
    @Test
    void testCacheEviction() {
        // Populate cache
        productService.getAllProducts();
        
        // Create new product - should evict cache
        Product newProduct = Product.builder()
                .code("TEST001")
                .name("Test Product")
                .build();
        productService.createProduct(newProduct);
        
        // Verify cache is empty
        Cache cache = cacheManager.getCache("products");
        assertNull(cache.get("getAllProducts"));
    }
}
```

### Visual Testing with Redis Insight

1. Open browser: `http://localhost:5540`
2. Connect to Redis server: `redis:6379`
3. Browse keys to see cached data
4. Monitor cache hits/misses in real-time

---

## Implementing Cache for Other Entities

### Example: Adding Cache to User Entity

#### Step 1: Make Entity Serializable

```java
@Entity
@Table(name = "users")
@Data
public class User implements Serializable {
    private static final long serialVersionUID = 1L;
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    private String username;
    private String email;
    // ... other fields
}
```

#### Step 2: Add Cache Annotations to UserService

```java
@Service
public class UserService {
    
    @Cacheable(value = "users")
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }
    
    @Cacheable(value = "userById", key = "#id")
    public User getUserById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("User not found"));
    }
    
    @Cacheable(value = "userByEmail", key = "#email")
    public User getUserByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new EntityNotFoundException("User not found"));
    }
    
    @CacheEvict(value = {"users", "userById", "userByEmail"}, allEntries = true)
    public User createUser(User user) {
        return userRepository.save(user);
    }
    
    @CacheEvict(value = {"users", "userById", "userByEmail"}, allEntries = true)
    public User updateUser(Long id, User user) {
        User existingUser = getUserById(id);
        // update logic
        return userRepository.save(existingUser);
    }
    
    @CacheEvict(value = {"users", "userById", "userByEmail"}, allEntries = true)
    public void deleteUser(Long id) {
        userRepository.deleteById(id);
    }
}
```

#### Step 3: Configure Different TTL (Optional)

If you want different cache durations for different entities, update `RedisConfig.java`:

```java
@Bean
public RedisCacheManager cacheManager(RedisConnectionFactory connectionFactory) {
    // Default configuration (10 minutes)
    RedisCacheConfiguration defaultConfig = RedisCacheConfiguration.defaultCacheConfig()
            .entryTtl(Duration.ofMinutes(10))
            .serializeKeysWith(RedisSerializationContext.SerializationPair.fromSerializer(new StringRedisSerializer()))
            .serializeValuesWith(RedisSerializationContext.SerializationPair.fromSerializer(RedisSerializer.json()))
            .disableCachingNullValues();
    
    // Custom configuration for users (30 minutes)
    RedisCacheConfiguration userConfig = RedisCacheConfiguration.defaultCacheConfig()
            .entryTtl(Duration.ofMinutes(30))
            .serializeKeysWith(RedisSerializationContext.SerializationPair.fromSerializer(new StringRedisSerializer()))
            .serializeValuesWith(RedisSerializationContext.SerializationPair.fromSerializer(RedisSerializer.json()))
            .disableCachingNullValues();
    
    // Custom configuration for products (5 minutes)
    RedisCacheConfiguration productConfig = RedisCacheConfiguration.defaultCacheConfig()
            .entryTtl(Duration.ofMinutes(5))
            .serializeKeysWith(RedisSerializationContext.SerializationPair.fromSerializer(new StringRedisSerializer()))
            .serializeValuesWith(RedisSerializationContext.SerializationPair.fromSerializer(RedisSerializer.json()))
            .disableCachingNullValues();

    return RedisCacheManager.builder(connectionFactory)
            .cacheDefaults(defaultConfig)
            .withCacheConfiguration("users", userConfig)
            .withCacheConfiguration("userById", userConfig)
            .withCacheConfiguration("userByEmail", userConfig)
            .withCacheConfiguration("products", productConfig)
            .withCacheConfiguration("activeProducts", productConfig)
            .withCacheConfiguration("productByCode", productConfig)
            .build();
}
```

---

## Best Practices

### 1. Cache Naming Convention
Use descriptive cache names:
- ✅ `products`, `activeProducts`, `productByCode`
- ❌ `cache1`, `data`, `temp`

### 2. Choose Appropriate TTL
- **Frequently changing data**: 1-5 minutes
- **Moderately stable data**: 10-30 minutes
- **Rarely changing data**: 1-24 hours
- **Static data**: No expiration (manual eviction only)

### 3. Cache Key Strategy
```java
// Simple key
@Cacheable(value = "products")

// Custom key with parameter
@Cacheable(value = "productByCode", key = "#code")

// Complex key with multiple parameters
@Cacheable(value = "productsByFilter", key = "#category + '-' + #status")

// Conditional caching
@Cacheable(value = "products", condition = "#result.size() > 0")
```

### 4. Cache Eviction Strategies

```java
// Evict all entries in specific caches
@CacheEvict(value = {"products", "activeProducts"}, allEntries = true)

// Evict specific entry
@CacheEvict(value = "productByCode", key = "#code")

// Evict before method execution
@CacheEvict(value = "products", beforeInvocation = true)

// Update cache instead of evicting
@CachePut(value = "productById", key = "#result.id")
public Product updateProduct(Product product) {
    return productRepository.save(product);
}
```

### 5. Don't Cache Everything
**Cache these**:
- Frequently read, rarely written data
- Expensive database queries
- External API responses
- Computed/aggregated data

**Don't cache these**:
- User-specific sensitive data (without proper isolation)
- Real-time data
- Large objects (>1MB)
- Data that changes on every request

### 6. Monitor Cache Performance

Add logging to track cache behavior:

```java
@Aspect
@Component
public class CacheLoggingAspect {
    
    private static final Logger log = LoggerFactory.getLogger(CacheLoggingAspect.class);
    
    @Around("@annotation(cacheable)")
    public Object logCacheable(ProceedingJoinPoint joinPoint, Cacheable cacheable) throws Throwable {
        String methodName = joinPoint.getSignature().getName();
        log.info("Cache lookup for method: {}, cache: {}", methodName, cacheable.value());
        
        long startTime = System.currentTimeMillis();
        Object result = joinPoint.proceed();
        long duration = System.currentTimeMillis() - startTime;
        
        log.info("Method {} executed in {}ms", methodName, duration);
        return result;
    }
}
```

---

## Troubleshooting

### Issue 1: Cache Not Working

**Symptoms**: Every request hits the database

**Solutions**:
1. Verify `@EnableCaching` is present in configuration
2. Check Redis is running: `docker ps`
3. Verify connection: `docker exec -it redis-server redis-cli ping` (should return "PONG")
4. Check logs for Redis connection errors
5. Ensure entity implements `Serializable`

### Issue 2: Stale Data in Cache

**Symptoms**: Old data returned after updates

**Solutions**:
1. Add `@CacheEvict` to all write operations
2. Verify cache names match between `@Cacheable` and `@CacheEvict`
3. Use `allEntries = true` for broad eviction
4. Reduce TTL for frequently changing data

### Issue 3: Serialization Errors

**Symptoms**: `SerializationException` in logs

**Solutions**:
1. Ensure entity implements `Serializable`
2. Add `serialVersionUID` to entity
3. Check for non-serializable fields (e.g., `transient` keyword)
4. Verify Jackson can serialize LocalDateTime (add `jackson-datatype-jsr310`)

### Issue 4: Memory Issues

**Symptoms**: Redis running out of memory

**Solutions**:
1. Reduce TTL values
2. Implement cache size limits
3. Use `@Cacheable(condition = "...")` to cache selectively
4. Monitor Redis memory: `docker exec -it redis-server redis-cli INFO memory`

### Issue 5: Cache Key Collisions

**Symptoms**: Wrong data returned from cache

**Solutions**:
1. Use unique cache names for different methods
2. Include parameters in cache key: `key = "#id + '-' + #status"`
3. Use SpEL for complex keys: `key = "#root.methodName + #id"`

---

## Advanced Topics

### Cache Warming (Pre-loading Cache)

```java
@Component
public class CacheWarmer implements ApplicationListener<ContextRefreshedEvent> {
    
    @Autowired
    private ProductService productService;
    
    @Override
    public void onApplicationEvent(ContextRefreshedEvent event) {
        // Pre-load frequently accessed data
        productService.getAllProducts();
        productService.getActiveProducts();
    }
}
```

### Conditional Caching

```java
// Only cache if result is not empty
@Cacheable(value = "products", condition = "#result != null && #result.size() > 0")
public List<Product> getProducts() {
    return productRepository.findAll();
}

// Don't cache for admin users
@Cacheable(value = "products", unless = "#user.role == 'ADMIN'")
public List<Product> getProducts(User user) {
    return productRepository.findAll();
}
```

### Distributed Caching (Production)

For production environments with multiple application instances:

```yaml
spring:
  data:
    redis:
      cluster:
        nodes:
          - redis-node1:6379
          - redis-node2:6379
          - redis-node3:6379
      password: ${REDIS_PASSWORD}
      ssl:
        enabled: true
```

---

## Summary

You now have a complete Redis caching implementation! Remember:

1. ✅ Start with Redis via Docker
2. ✅ Add dependencies and configuration
3. ✅ Make entities Serializable
4. ✅ Add `@Cacheable` for reads, `@CacheEvict` for writes
5. ✅ Test thoroughly
6. ✅ Monitor cache performance
7. ✅ Extend to other entities as needed

Happy caching! 🚀
