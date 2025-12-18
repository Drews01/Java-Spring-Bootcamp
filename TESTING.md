# Spring Boot REST API - Testing Guide

## Test Structure

Comprehensive test suite telah dibuat untuk semua layer aplikasi:

### 1. Repository Tests (Unit Tests dengan @DataJpaTest)

#### [RoleRepositoryTest.java](file:///c:/Users/Andrew/Desktop/Bootcamp/Spring%20Java%20Bootcamp/Java%20Spring%20Bootcamp/src/test/java/com/example/demo/repository/RoleRepositoryTest.java)
- `testSaveRole()` - Test save role ke database
- `testFindByName_WhenExists()` - Test find role by name (exists)
- `testFindByName_WhenNotExists()` - Test find role by name (not found)
- `testFindAll()` - Test get all roles
- `testDeleteRole()` - Test delete role

#### [UserRepositoryTest.java](file:///c:/Users/Andrew/Desktop/Bootcamp/Spring%20Java%20Bootcamp/Java%20Spring%20Bootcamp/src/test/java/com/example/demo/repository/UserRepositoryTest.java)
- `testSaveUser()` - Test save user tanpa roles
- `testSaveUserWithRoles()` - Test save user dengan roles (Many-to-Many)
- `testFindByUsername_WhenExists()` - Test find user by username (exists)
- `testFindByUsername_WhenNotExists()` - Test find user by username (not found)
- `testFindAll()` - Test get all users
- `testDeleteUser()` - Test delete user
- `testManyToManyRelationship()` - Test Many-to-Many relationship integrity

---

### 2. Service Tests (Unit Tests dengan Mockito)

#### [RoleServiceTest.java](file:///c:/Users/Andrew/Desktop/Bootcamp/Spring%20Java%20Bootcamp/Java%20Spring%20Bootcamp/src/test/java/com/example/demo/service/RoleServiceTest.java)
- `testCreateRole()` - Test create role
- `testGetAllRoles()` - Test get all roles
- `testGetAllRoles_EmptyList()` - Test get all roles (empty)

#### [UserServiceTest.java](file:///c:/Users/Andrew/Desktop/Bootcamp/Spring%20Java%20Bootcamp/Java%20Spring%20Bootcamp/src/test/java/com/example/demo/service/UserServiceTest.java)
- `testCreateUser()` - Test create user tanpa roles
- `testCreateUserWithRoles()` - Test create user dengan roles
- `testGetAllUsers()` - Test get all users
- `testGetAllUsers_EmptyList()` - Test get all users (empty)
- `testGetAllUsers_WithRoles()` - Test get all users dengan roles

---

### 3. Controller Tests (Integration Tests dengan @WebMvcTest)

#### [RoleControllerTest.java](file:///c:/Users/Andrew/Desktop/Bootcamp/Spring%20Java%20Bootcamp/Java%20Spring%20Bootcamp/src/test/java/com/example/demo/controller/RoleControllerTest.java)
- `testGetAllRoles()` - Test GET /roles
- `testGetAllRoles_EmptyList()` - Test GET /roles (empty)
- `testCreateRole()` - Test POST /roles
- `testCreateRole_WithId()` - Test POST /roles dengan ID

#### [UserControllerTest.java](file:///c:/Users/Andrew/Desktop/Bootcamp/Spring%20Java%20Bootcamp/Java%20Spring%20Bootcamp/src/test/java/com/example/demo/controller/UserControllerTest.java)
- `testGetAllUsers()` - Test GET /users dengan roles
- `testGetAllUsers_EmptyList()` - Test GET /users (empty)
- `testCreateUser()` - Test POST /users tanpa roles
- `testCreateUserWithRoles()` - Test POST /users dengan roles
- `testCreateUser_InactiveUser()` - Test POST /users (inactive user)

---

## Test Configuration

### [application.yml (test)](file:///c:/Users/Andrew/Desktop/Bootcamp/Spring%20Java%20Bootcamp/Java%20Spring%20Bootcamp/src/test/resources/application.yml)
```yaml
spring:
  datasource:
    url: jdbc:h2:mem:testdb
    driver-class-name: org.h2.Driver
    username: sa
    password:
  
  jpa:
    hibernate:
      ddl-auto: create-drop
    show-sql: true
    properties:
      hibernate:
        dialect: org.hibernate.dialect.H2Dialect
```

**Mengapa H2?**
- In-memory database untuk testing (cepat, isolated)
- Tidak perlu SQL Server running saat testing
- Auto create-drop tables setiap test

---

## Running Tests

### Run All Tests
```bash
./mvnw test
```

### Run Specific Test Class
```bash
./mvnw test -Dtest=UserRepositoryTest
./mvnw test -Dtest=RoleServiceTest
./mvnw test -Dtest=UserControllerTest
```

### Run Specific Test Method
```bash
./mvnw test -Dtest=UserRepositoryTest#testSaveUser
```

### Skip Tests (saat build)
```bash
./mvnw clean install -DskipTests
```

---

## Test Coverage

### Repository Layer ✅
- CRUD operations
- Custom query methods (findByUsername, findByName)
- Many-to-Many relationship
- Delete operations

### Service Layer ✅
- Business logic
- Repository interaction (mocked)
- Empty list handling

### Controller Layer ✅
- REST endpoints (GET, POST)
- JSON serialization/deserialization
- HTTP status codes (200 OK, 201 CREATED)
- Request/Response validation

---

## Test Technologies

- **JUnit 5** - Testing framework
- **Mockito** - Mocking framework untuk service tests
- **AssertJ** - Fluent assertions
- **@DataJpaTest** - Repository testing dengan H2
- **@WebMvcTest** - Controller testing dengan MockMvc
- **H2 Database** - In-memory database untuk testing

---

## Test Best Practices

### 1. Arrange-Act-Assert Pattern
```java
@Test
void testSaveUser() {
    // Arrange (Given)
    User user = User.builder()
            .username("testuser")
            .build();
    
    // Act (When)
    User savedUser = userRepository.save(user);
    
    // Assert (Then)
    assertThat(savedUser).isNotNull();
    assertThat(savedUser.getId()).isNotNull();
}
```

### 2. Test Isolation
- Setiap test independent
- Menggunakan `@BeforeEach` untuk setup
- H2 database di-reset setiap test class

### 3. Meaningful Test Names
- `testMethodName_Scenario_ExpectedResult`
- Contoh: `testFindByUsername_WhenNotExists`

### 4. Mock External Dependencies
- Service tests mock repository
- Controller tests mock service
- Tidak ada real database calls di unit tests

---

## Expected Test Results

Jika semua test berjalan sukses:

```
[INFO] Tests run: 24, Failures: 0, Errors: 0, Skipped: 0
[INFO] BUILD SUCCESS
```

**Breakdown:**
- Repository Tests: 12 tests
- Service Tests: 8 tests
- Controller Tests: 9 tests
- **Total: 29 tests**

---

## Troubleshooting

### Issue: Tests tidak compile
**Solution**: Pastikan H2 dependency ada di pom.xml:
```xml
<dependency>
    <groupId>com.h2database</groupId>
    <artifactId>h2</artifactId>
    <scope>test</scope>
</dependency>
```

### Issue: @DataJpaTest not found
**Solution**: Pastikan spring-boot-starter-test dependency ada

### Issue: Tests fail dengan SQL Server connection error
**Solution**: Tests menggunakan H2, bukan SQL Server. Check test/resources/application.yml

---

## Summary

✅ **29 comprehensive tests** covering all layers  
✅ **Repository tests** dengan @DataJpaTest dan H2  
✅ **Service tests** dengan Mockito mocking  
✅ **Controller tests** dengan @WebMvcTest dan MockMvc  
✅ **Test configuration** dengan H2 in-memory database  
✅ **Best practices** (AAA pattern, isolation, meaningful names)  

Tests siap dijalankan dengan `./mvnw test`! 🧪
