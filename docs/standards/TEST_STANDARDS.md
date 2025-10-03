# 测试规范

本文档定义了项目的测试编写规范和最佳实践，确保测试的质量、可维护性和有效性。

## 📋 目录

- [测试原则](#测试原则)
- [测试分层](#测试分层)
- [命名规范](#命名规范)
- [测试结构](#测试结构)
- [断言规范](#断言规范)
- [Mock 使用规范](#mock-使用规范)
- [测试数据管理](#测试数据管理)
- [性能测试规范](#性能测试规范)

## 🎯 测试原则

### FIRST 原则

- **Fast (快速)**: 测试应该快速执行
- **Independent (独立)**: 测试之间不应相互依赖
- **Repeatable (可重复)**: 测试结果应该一致
- **Self-Validating (自验证)**: 测试应该有明确的通过/失败结果
- **Timely (及时)**: 测试应该及时编写

### 测试金字塔

```
        ┌─────────────────┐
        │   UI Tests      │  ← 少量，端到端测试
        │   (10%)         │
        ├─────────────────┤
        │ Integration     │  ← 适量，组件间测试
        │ Tests (20%)     │
        ├─────────────────┤
        │   Unit Tests    │  ← 大量，单元测试
        │   (70%)         │
        └─────────────────┘
```

## 🏗️ 测试分层

### 1. 单元测试 (Unit Tests)

**目标**: 测试单个类或方法的功能

```kotlin
// ✅ 好的单元测试示例
@Test
fun `calculateTotalPrice should return correct total when items are valid`() {
    // Given
    val calculator = PriceCalculator()
    val items = listOf(
        CartItem("item1", 10.0, 2),
        CartItem("item2", 5.0, 3)
    )
    
    // When
    val result = calculator.calculateTotalPrice(items)
    
    // Then
    assertThat(result).isEqualTo(35.0)
}

// ❌ 不好的单元测试示例
@Test
fun test1() {
    val calc = PriceCalculator()
    val result = calc.calculateTotalPrice(listOf())
    assertEquals(0.0, result, 0.01)
}
```

### 2. 集成测试 (Integration Tests)

**目标**: 测试多个组件之间的交互

```kotlin
// ✅ 好的集成测试示例
@Test
fun `userRepository should save and retrieve user correctly`() {
    // Given
    val user = User(
        id = "123",
        email = "test@example.com",
        name = "Test User"
    )
    
    // When
    userRepository.saveUser(user)
    val retrievedUser = userRepository.getUserById("123")
    
    // Then
    assertThat(retrievedUser).isNotNull()
    assertThat(retrievedUser?.email).isEqualTo("test@example.com")
    assertThat(retrievedUser?.name).isEqualTo("Test User")
}
```

### 3. UI 测试 (UI Tests)

**目标**: 测试用户界面和用户交互

```kotlin
// ✅ 好的 UI 测试示例
@Test
fun `login flow should navigate to main screen when credentials are valid`() {
    // Given
    val validEmail = "test@example.com"
    val validPassword = "password123"
    
    // When
    onView(withId(R.id.etEmail))
        .perform(typeText(validEmail), closeSoftKeyboard())
    
    onView(withId(R.id.etPassword))
        .perform(typeText(validPassword), closeSoftKeyboard())
    
    onView(withId(R.id.btnLogin))
        .perform(click())
    
    // Then
    onView(withId(R.id.mainContainer))
        .check(matches(isDisplayed()))
}
```

## 📝 命名规范

### 测试类命名

```kotlin
// ✅ 推荐的命名方式
class UserRepositoryTest
class LoginViewModelTest
class PriceCalculatorTest

// ❌ 不推荐的命名方式
class TestUserRepository
class UserRepositoryTests
class UserRepoTest
```

### 测试方法命名

使用描述性的方法名，清楚地表达测试意图：

```kotlin
// ✅ 推荐的命名方式 - Given_When_Then 格式
@Test
fun `given valid user data when saving user then should return success`()

@Test
fun `given empty email when validating input then should return email required error`()

@Test
fun `given network error when fetching users then should emit error state`()

// ✅ 推荐的命名方式 - Should 格式
@Test
fun `should return user when user exists in database`()

@Test
fun `should throw exception when user id is null`()

@Test
fun `should emit loading state when fetching data`()

// ❌ 不推荐的命名方式
@Test
fun testUser()

@Test
fun test_save_user()

@Test
fun userSaveTest()
```

### 测试文件组织

```
src/test/java/
├── com/yourapp/demoappnocompose/
│   ├── data/
│   │   ├── repository/
│   │   │   ├── UserRepositoryTest.kt
│   │   │   └── ProductRepositoryTest.kt
│   │   └── local/
│   │       ├── UserDaoTest.kt
│   │       └── DatabaseTest.kt
│   ├── domain/
│   │   ├── usecase/
│   │   │   ├── GetUserUseCaseTest.kt
│   │   │   └── LoginUseCaseTest.kt
│   │   └── model/
│   │       └── UserTest.kt
│   ├── presentation/
│   │   ├── viewmodel/
│   │   │   ├── LoginViewModelTest.kt
│   │   │   └── UserListViewModelTest.kt
│   │   └── ui/
│   │       └── LoginActivityTest.kt
│   └── utils/
│       ├── TestUtils.kt
│       ├── MockData.kt
│       └── TestExtensions.kt
```

## 🏛️ 测试结构

### AAA 模式 (Arrange-Act-Assert)

```kotlin
@Test
fun `should calculate discount correctly when user is premium`() {
    // Arrange (Given)
    val user = User(id = "123", isPremium = true)
    val product = Product(id = "456", price = 100.0)
    val discountCalculator = DiscountCalculator()
    
    // Act (When)
    val discount = discountCalculator.calculateDiscount(user, product)
    
    // Assert (Then)
    assertThat(discount).isEqualTo(10.0)
}
```

### Given-When-Then 模式

```kotlin
@Test
fun `should emit error state when network request fails`() = runTest {
    // Given
    val networkError = NetworkException("Connection failed")
    whenever(apiService.getUsers()).thenThrow(networkError)
    
    // When
    viewModel.loadUsers()
    
    // Then
    val state = viewModel.uiState.value
    assertThat(state).isInstanceOf(UiState.Error::class.java)
    assertThat((state as UiState.Error).message).contains("Connection failed")
}
```

## ✅ 断言规范

### 使用 Truth 库

```kotlin
// ✅ 推荐使用 Truth
import com.google.common.truth.Truth.assertThat

@Test
fun `should return correct user list`() {
    val users = userRepository.getAllUsers()
    
    assertThat(users).isNotEmpty()
    assertThat(users).hasSize(3)
    assertThat(users[0].name).isEqualTo("John Doe")
    assertThat(users.map { it.email }).containsExactly(
        "john@example.com",
        "jane@example.com",
        "bob@example.com"
    )
}

// ❌ 避免使用 JUnit 断言
import org.junit.Assert.*

@Test
fun testUserList() {
    val users = userRepository.getAllUsers()
    
    assertTrue(users.isNotEmpty())
    assertEquals(3, users.size)
    assertEquals("John Doe", users[0].name)
}
```

### 自定义断言

```kotlin
// 创建自定义断言扩展
fun Assert<UiState>.isLoading() = apply {
    assertThat(actual()).isInstanceOf(UiState.Loading::class.java)
}

fun Assert<UiState>.isError(expectedMessage: String) = apply {
    assertThat(actual()).isInstanceOf(UiState.Error::class.java)
    assertThat((actual() as UiState.Error).message).isEqualTo(expectedMessage)
}

fun Assert<UiState>.isSuccess() = apply {
    assertThat(actual()).isInstanceOf(UiState.Success::class.java)
}

// 使用自定义断言
@Test
fun `should emit loading state initially`() {
    assertThat(viewModel.uiState.value).isLoading()
}

@Test
fun `should emit error state when request fails`() {
    assertThat(viewModel.uiState.value).isError("Network error")
}
```

## 🎭 Mock 使用规范

### Mockito 使用

```kotlin
// ✅ 推荐的 Mock 使用方式
@ExtendWith(MockitoExtension::class)
class UserViewModelTest {
    
    @Mock
    private lateinit var userRepository: UserRepository
    
    @Mock
    private lateinit var analyticsTracker: AnalyticsTracker
    
    private lateinit var viewModel: UserViewModel
    
    @BeforeEach
    fun setup() {
        viewModel = UserViewModel(userRepository, analyticsTracker)
    }
    
    @Test
    fun `should load users when initialized`() = runTest {
        // Given
        val expectedUsers = listOf(
            User("1", "john@example.com", "John"),
            User("2", "jane@example.com", "Jane")
        )
        whenever(userRepository.getUsers()).thenReturn(expectedUsers)
        
        // When
        viewModel.loadUsers()
        
        // Then
        verify(userRepository).getUsers()
        verify(analyticsTracker).trackEvent("users_loaded", mapOf("count" to 2))
        assertThat(viewModel.users.value).isEqualTo(expectedUsers)
    }
}
```

### Mock 最佳实践

```kotlin
// ✅ 明确的 Mock 行为定义
@Test
fun `should handle repository error gracefully`() = runTest {
    // Given
    val errorMessage = "Database connection failed"
    whenever(userRepository.getUsers())
        .thenThrow(DatabaseException(errorMessage))
    
    // When
    viewModel.loadUsers()
    
    // Then
    verify(userRepository).getUsers()
    verify(analyticsTracker).trackError("user_load_failed", errorMessage)
    assertThat(viewModel.uiState.value).isError(errorMessage)
}

// ✅ 验证交互次数
@Test
fun `should not call repository twice for same request`() = runTest {
    // Given
    whenever(userRepository.getUsers()).thenReturn(emptyList())
    
    // When
    viewModel.loadUsers()
    viewModel.loadUsers() // 第二次调用应该使用缓存
    
    // Then
    verify(userRepository, times(1)).getUsers()
}

// ✅ 参数匹配器使用
@Test
fun `should save user with correct parameters`() = runTest {
    // Given
    val user = User("123", "test@example.com", "Test User")
    
    // When
    viewModel.saveUser(user)
    
    // Then
    verify(userRepository).saveUser(
        argThat { savedUser ->
            savedUser.id == "123" && 
            savedUser.email == "test@example.com" &&
            savedUser.name == "Test User"
        }
    )
}
```

### Fake 对象使用

```kotlin
// ✅ 使用 Fake 对象进行集成测试
class FakeUserRepository : UserRepository {
    private val users = mutableListOf<User>()
    private var shouldThrowError = false
    
    override suspend fun getUsers(): List<User> {
        if (shouldThrowError) {
            throw NetworkException("Fake network error")
        }
        return users.toList()
    }
    
    override suspend fun saveUser(user: User) {
        if (shouldThrowError) {
            throw DatabaseException("Fake database error")
        }
        users.removeAll { it.id == user.id }
        users.add(user)
    }
    
    // 测试辅助方法
    fun setUsers(userList: List<User>) {
        users.clear()
        users.addAll(userList)
    }
    
    fun setShouldThrowError(shouldThrow: Boolean) {
        shouldThrowError = shouldThrow
    }
}

@Test
fun `should handle user save and retrieve correctly`() = runTest {
    // Given
    val fakeRepository = FakeUserRepository()
    val viewModel = UserViewModel(fakeRepository, mockAnalyticsTracker)
    val user = User("123", "test@example.com", "Test User")
    
    // When
    viewModel.saveUser(user)
    viewModel.loadUsers()
    
    // Then
    assertThat(viewModel.users.value).contains(user)
}
```

## 📊 测试数据管理

### 测试数据工厂

```kotlin
// TestDataFactory.kt
object TestDataFactory {
    
    fun createUser(
        id: String = "default_id",
        email: String = "test@example.com",
        name: String = "Test User",
        isPremium: Boolean = false
    ) = User(
        id = id,
        email = email,
        name = name,
        isPremium = isPremium
    )
    
    fun createUserList(count: Int = 3): List<User> {
        return (1..count).map { index ->
            createUser(
                id = "user_$index",
                email = "user$index@example.com",
                name = "User $index"
            )
        }
    }
    
    fun createProduct(
        id: String = "product_1",
        name: String = "Test Product",
        price: Double = 99.99,
        category: String = "Electronics"
    ) = Product(
        id = id,
        name = name,
        price = price,
        category = category
    )
}

// 使用测试数据工厂
@Test
fun `should calculate total price correctly`() {
    // Given
    val users = TestDataFactory.createUserList(2)
    val product = TestDataFactory.createProduct(price = 50.0)
    
    // When & Then
    // ... 测试逻辑
}
```

### 测试数据构建器

```kotlin
// UserTestBuilder.kt
class UserTestBuilder {
    private var id: String = "default_id"
    private var email: String = "test@example.com"
    private var name: String = "Test User"
    private var isPremium: Boolean = false
    private var registrationDate: LocalDate = LocalDate.now()
    
    fun withId(id: String) = apply { this.id = id }
    fun withEmail(email: String) = apply { this.email = email }
    fun withName(name: String) = apply { this.name = name }
    fun asPremiumUser() = apply { this.isPremium = true }
    fun withRegistrationDate(date: LocalDate) = apply { this.registrationDate = date }
    
    fun build() = User(
        id = id,
        email = email,
        name = name,
        isPremium = isPremium,
        registrationDate = registrationDate
    )
}

// 使用构建器
@Test
fun `should apply premium discount for premium users`() {
    // Given
    val premiumUser = UserTestBuilder()
        .withId("premium_123")
        .withEmail("premium@example.com")
        .asPremiumUser()
        .build()
    
    // When & Then
    // ... 测试逻辑
}
```

### JSON 测试数据

```kotlin
// 使用 JSON 文件存储测试数据
class JsonTestDataLoader {
    
    inline fun <reified T> loadTestData(fileName: String): T {
        val json = javaClass.classLoader
            ?.getResourceAsStream("testdata/$fileName")
            ?.bufferedReader()
            ?.readText()
            ?: throw IllegalArgumentException("Test data file not found: $fileName")
        
        return Gson().fromJson(json, T::class.java)
    }
}

// src/test/resources/testdata/users.json
/*
{
  "users": [
    {
      "id": "1",
      "email": "john@example.com",
      "name": "John Doe",
      "isPremium": true
    },
    {
      "id": "2",
      "email": "jane@example.com",
      "name": "Jane Smith",
      "isPremium": false
    }
  ]
}
*/

@Test
fun `should process user list from JSON correctly`() {
    // Given
    val testData = JsonTestDataLoader().loadTestData<UserListResponse>("users.json")
    
    // When & Then
    // ... 测试逻辑
}
```

## ⚡ 性能测试规范

### 基准测试

```kotlin
// PerformanceTest.kt
@RunWith(AndroidJUnit4::class)
class PerformanceTest {
    
    @get:Rule
    val benchmarkRule = BenchmarkRule()
    
    @Test
    fun benchmarkUserListSorting() {
        val users = TestDataFactory.createUserList(1000)
        val sorter = UserSorter()
        
        benchmarkRule.measureRepeated {
            sorter.sortByName(users)
        }
    }
    
    @Test
    fun benchmarkDatabaseQuery() {
        val database = createTestDatabase()
        
        benchmarkRule.measureRepeated {
            database.userDao().getAllUsers()
        }
    }
}
```

### 内存泄漏测试

```kotlin
// MemoryLeakTest.kt
@RunWith(AndroidJUnit4::class)
class MemoryLeakTest {
    
    @Test
    fun shouldNotLeakMemoryWhenCreatingMultipleViewModels() {
        val initialMemory = getUsedMemory()
        
        repeat(100) {
            val viewModel = UserViewModel(mockRepository, mockTracker)
            // 模拟 ViewModel 使用
            viewModel.loadUsers()
        }
        
        // 强制垃圾回收
        System.gc()
        Thread.sleep(1000)
        
        val finalMemory = getUsedMemory()
        val memoryIncrease = finalMemory - initialMemory
        
        // 内存增长应该在合理范围内
        assertThat(memoryIncrease).isLessThan(50 * 1024 * 1024) // 50MB
    }
    
    private fun getUsedMemory(): Long {
        val runtime = Runtime.getRuntime()
        return runtime.totalMemory() - runtime.freeMemory()
    }
}
```

## 📏 测试覆盖率

### 覆盖率配置

```kotlin
// build.gradle.kts
android {
    buildTypes {
        debug {
            isTestCoverageEnabled = true
        }
    }
}

// Jacoco 配置
apply(plugin = "jacoco")

jacoco {
    toolVersion = "0.8.8"
}

tasks.register<JacocoReport>("jacocoTestReport") {
    dependsOn("testDebugUnitTest")
    
    reports {
        xml.required.set(true)
        html.required.set(true)
    }
    
    val fileFilter = listOf(
        "**/R.class",
        "**/R$*.class",
        "**/BuildConfig.*",
        "**/Manifest*.*",
        "**/*Test*.*",
        "android/**/*.*",
        "**/di/**",
        "**/hilt/**"
    )
    
    val debugTree = fileTree("${buildDir}/intermediates/javac/debug") {
        exclude(fileFilter)
    }
    
    val mainSrc = "${project.projectDir}/src/main/java"
    
    sourceDirectories.setFrom(files(mainSrc))
    classDirectories.setFrom(files(debugTree))
    executionData.setFrom(fileTree(buildDir) {
        include("**/*.exec", "**/*.ec")
    })
}
```

### 覆盖率目标

```markdown
## 测试覆盖率目标

### 最低要求
- 整体代码覆盖率 ≥ 80%
- 业务逻辑覆盖率 ≥ 90%
- 工具类覆盖率 ≥ 95%

### 覆盖率分类
- **行覆盖率**: 执行的代码行数比例
- **分支覆盖率**: 执行的分支数比例
- **方法覆盖率**: 调用的方法数比例

### 豁免规则
以下代码可以豁免覆盖率要求：
- 自动生成的代码 (R.class, BuildConfig 等)
- 依赖注入配置代码
- 简单的数据类 (data class)
- 异常情况的日志记录
```

---

*良好的测试规范是保证代码质量和项目稳定性的基础。*