# 测试策略

本文档详细说明了 Android MVVM 框架的测试策略和实施方案。

## 📋 目录

- [测试概述](#测试概述)
- [测试金字塔](#测试金字塔)
- [单元测试](#单元测试)
- [集成测试](#集成测试)
- [UI测试](#ui测试)
- [测试工具链](#测试工具链)

## 🎯 测试概述

### 测试目标

- **质量保证**: 确保代码质量和功能正确性
- **回归防护**: 防止新功能破坏现有功能
- **重构支持**: 为代码重构提供安全保障
- **文档作用**: 测试用例作为代码行为的文档

### 测试原则

- **快速反馈**: 测试应该快速执行并提供反馈
- **可靠性**: 测试结果应该稳定可靠
- **可维护性**: 测试代码应该易于维护
- **覆盖率**: 关键业务逻辑应该有充分的测试覆盖

## 🔺 测试金字塔

### 测试层次分布

```
           ┌─────────────┐
          ╱               ╲
         ╱    UI Tests     ╲     ← 10% (慢，昂贵，脆弱)
        ╱     (E2E)        ╲
       ╱___________________╲
      ╱                     ╲
     ╱  Integration Tests    ╲    ← 20% (中等速度，中等成本)
    ╱      (API, DB)         ╲
   ╱_________________________╲
  ╱                           ╲
 ╱      Unit Tests             ╲  ← 70% (快，便宜，稳定)
╱    (Logic, ViewModels)       ╲
╲_______________________________╱
```

### 测试分层策略

#### 单元测试 (70%)
- **范围**: ViewModel、Repository、UseCase、工具类
- **特点**: 快速、独立、可重复
- **工具**: JUnit5、Mockk、Truth

#### 集成测试 (20%)
- **范围**: API 调用、数据库操作、组件交互
- **特点**: 测试组件间的协作
- **工具**: Hilt Testing、Room Testing

#### UI测试 (10%)
- **范围**: 用户界面交互、端到端流程
- **特点**: 最接近真实用户体验
- **工具**: Espresso、UI Automator

## 🧪 单元测试

### ViewModel 测试

```kotlin
@ExtendWith(MockKExtension::class)
class LoginViewModelTest {
    
    @MockK
    private lateinit var userRepository: UserRepository
    
    @MockK
    private lateinit var validationUseCase: ValidationUseCase
    
    private lateinit var viewModel: LoginViewModel
    
    @BeforeEach
    fun setup() {
        viewModel = LoginViewModel(userRepository, validationUseCase)
    }
    
    @Test
    fun `login with valid credentials should succeed`() = runTest {
        // Given
        val email = "test@example.com"
        val password = "password123"
        val expectedUser = User(id = "1", email = email, name = "Test User")
        
        every { validationUseCase.validateEmail(email) } returns ValidationResult.Valid
        every { validationUseCase.validatePassword(password) } returns ValidationResult.Valid
        coEvery { userRepository.login(email, password) } returns Result.success(expectedUser)
        
        // When
        viewModel.login(email, password)
        
        // Then
        val uiState = viewModel.uiState.value
        assertThat(uiState.isLoading).isFalse()
        assertThat(uiState.user).isEqualTo(expectedUser)
        assertThat(uiState.error).isNull()
        
        coVerify { userRepository.login(email, password) }
    }
    
    @Test
    fun `login with invalid email should show validation error`() = runTest {
        // Given
        val invalidEmail = "invalid-email"
        val password = "password123"
        val validationError = ValidationResult.Invalid("Invalid email format")
        
        every { validationUseCase.validateEmail(invalidEmail) } returns validationError
        
        // When
        viewModel.login(invalidEmail, password)
        
        // Then
        val uiState = viewModel.uiState.value
        assertThat(uiState.isLoading).isFalse()
        assertThat(uiState.emailError).isEqualTo(validationError.message)
        assertThat(uiState.user).isNull()
        
        coVerify(exactly = 0) { userRepository.login(any(), any()) }
    }
    
    @Test
    fun `login with network error should show error message`() = runTest {
        // Given
        val email = "test@example.com"
        val password = "password123"
        val networkError = Exception("Network error")
        
        every { validationUseCase.validateEmail(email) } returns ValidationResult.Valid
        every { validationUseCase.validatePassword(password) } returns ValidationResult.Valid
        coEvery { userRepository.login(email, password) } returns Result.failure(networkError)
        
        // When
        viewModel.login(email, password)
        
        // Then
        val uiState = viewModel.uiState.value
        assertThat(uiState.isLoading).isFalse()
        assertThat(uiState.error).isEqualTo("Network error")
        assertThat(uiState.user).isNull()
    }
    
    @Test
    fun `loading state should be managed correctly`() = runTest {
        // Given
        val email = "test@example.com"
        val password = "password123"
        val loginDeferred = CompletableDeferred<Result<User>>()
        
        every { validationUseCase.validateEmail(email) } returns ValidationResult.Valid
        every { validationUseCase.validatePassword(password) } returns ValidationResult.Valid
        coEvery { userRepository.login(email, password) } returns loginDeferred.await()
        
        // When
        val job = launch { viewModel.login(email, password) }
        
        // Then - loading state should be true
        assertThat(viewModel.uiState.value.isLoading).isTrue()
        
        // Complete the login
        loginDeferred.complete(Result.success(User("1", email, "Test")))
        job.join()
        
        // Then - loading state should be false
        assertThat(viewModel.uiState.value.isLoading).isFalse()
    }
}
```

### Repository 测试

```kotlin
@ExtendWith(MockKExtension::class)
class UserRepositoryImplTest {
    
    @MockK
    private lateinit var apiService: ApiService
    
    @MockK
    private lateinit var userDao: UserDao
    
    @MockK
    private lateinit var secureDataStore: SecureDataStore
    
    private lateinit var repository: UserRepositoryImpl
    
    @BeforeEach
    fun setup() {
        repository = UserRepositoryImpl(apiService, userDao, secureDataStore)
    }
    
    @Test
    fun `login should save user data and token`() = runTest {
        // Given
        val email = "test@example.com"
        val password = "password123"
        val loginResponse = LoginResponse(
            user = UserDto(id = "1", email = email, name = "Test User"),
            token = "auth_token_123"
        )
        val expectedUser = User(id = "1", email = email, name = "Test User")
        
        coEvery { apiService.login(any()) } returns loginResponse
        coEvery { userDao.insertUser(any()) } just Runs
        every { secureDataStore.saveToken(any()) } just Runs
        
        // When
        val result = repository.login(email, password)
        
        // Then
        assertThat(result.isSuccess).isTrue()
        assertThat(result.getOrNull()).isEqualTo(expectedUser)
        
        coVerify { apiService.login(LoginRequest(email, password)) }
        coVerify { userDao.insertUser(expectedUser.toEntity()) }
        verify { secureDataStore.saveToken("auth_token_123") }
    }
    
    @Test
    fun `getCurrentUser should return cached user when available`() = runTest {
        // Given
        val cachedUser = UserEntity(id = "1", email = "test@example.com", name = "Test User")
        val expectedUser = User(id = "1", email = "test@example.com", name = "Test User")
        
        coEvery { userDao.getCurrentUser() } returns cachedUser
        
        // When
        val result = repository.getCurrentUser()
        
        // Then
        assertThat(result).isEqualTo(expectedUser)
        coVerify(exactly = 0) { apiService.getCurrentUser() }
    }
    
    @Test
    fun `getCurrentUser should fetch from API when cache is empty`() = runTest {
        // Given
        val apiUser = UserDto(id = "1", email = "test@example.com", name = "Test User")
        val expectedUser = User(id = "1", email = "test@example.com", name = "Test User")
        
        coEvery { userDao.getCurrentUser() } returns null
        coEvery { apiService.getCurrentUser() } returns apiUser
        coEvery { userDao.insertUser(any()) } just Runs
        
        // When
        val result = repository.getCurrentUser()
        
        // Then
        assertThat(result).isEqualTo(expectedUser)
        coVerify { apiService.getCurrentUser() }
        coVerify { userDao.insertUser(expectedUser.toEntity()) }
    }
}
```

### UseCase 测试

```kotlin
class ValidationUseCaseTest {
    
    private val useCase = ValidationUseCase()
    
    @Test
    fun `validateEmail with valid email should return Valid`() {
        // Given
        val validEmails = listOf(
            "test@example.com",
            "user.name@domain.co.uk",
            "user+tag@example.org"
        )
        
        // When & Then
        validEmails.forEach { email ->
            val result = useCase.validateEmail(email)
            assertThat(result).isInstanceOf(ValidationResult.Valid::class.java)
        }
    }
    
    @Test
    fun `validateEmail with invalid email should return Invalid`() {
        // Given
        val invalidEmails = listOf(
            "",
            "invalid-email",
            "@example.com",
            "test@",
            "test..test@example.com"
        )
        
        // When & Then
        invalidEmails.forEach { email ->
            val result = useCase.validateEmail(email)
            assertThat(result).isInstanceOf(ValidationResult.Invalid::class.java)
        }
    }
    
    @Test
    fun `validatePassword with strong password should return Valid`() {
        // Given
        val strongPasswords = listOf(
            "Password123!",
            "MyStr0ng@Pass",
            "C0mpl3x#P@ssw0rd"
        )
        
        // When & Then
        strongPasswords.forEach { password ->
            val result = useCase.validatePassword(password)
            assertThat(result).isInstanceOf(ValidationResult.Valid::class.java)
        }
    }
    
    @Test
    fun `validatePassword with weak password should return Invalid with specific message`() {
        // Given & When & Then
        val result1 = useCase.validatePassword("123")
        assertThat(result1).isInstanceOf(ValidationResult.Invalid::class.java)
        assertThat((result1 as ValidationResult.Invalid).message)
            .contains("at least 8 characters")
        
        val result2 = useCase.validatePassword("password")
        assertThat(result2).isInstanceOf(ValidationResult.Invalid::class.java)
        assertThat((result2 as ValidationResult.Invalid).message)
            .contains("uppercase letter")
        
        val result3 = useCase.validatePassword("PASSWORD")
        assertThat(result3).isInstanceOf(ValidationResult.Invalid::class.java)
        assertThat((result3 as ValidationResult.Invalid).message)
            .contains("lowercase letter")
    }
}
```

## 🔗 集成测试

### API 集成测试

```kotlin
@HiltAndroidTest
class ApiIntegrationTest {
    
    @get:Rule
    var hiltRule = HiltAndroidRule(this)
    
    @Inject
    lateinit var apiService: ApiService
    
    private lateinit var mockWebServer: MockWebServer
    
    @BeforeEach
    fun setup() {
        hiltRule.inject()
        mockWebServer = MockWebServer()
        mockWebServer.start()
    }
    
    @AfterEach
    fun tearDown() {
        mockWebServer.shutdown()
    }
    
    @Test
    fun `login API should return user data on success`() = runTest {
        // Given
        val loginRequest = LoginRequest("test@example.com", "password123")
        val expectedResponse = LoginResponse(
            user = UserDto("1", "test@example.com", "Test User"),
            token = "auth_token_123"
        )
        
        mockWebServer.enqueue(
            MockResponse()
                .setResponseCode(200)
                .setBody(Json.encodeToString(expectedResponse))
                .setHeader("Content-Type", "application/json")
        )
        
        // When
        val response = apiService.login(loginRequest)
        
        // Then
        assertThat(response).isEqualTo(expectedResponse)
        
        val recordedRequest = mockWebServer.takeRequest()
        assertThat(recordedRequest.method).isEqualTo("POST")
        assertThat(recordedRequest.path).isEqualTo("/api/auth/login")
        assertThat(recordedRequest.body.readUtf8())
            .isEqualTo(Json.encodeToString(loginRequest))
    }
    
    @Test
    fun `login API should throw exception on error`() = runTest {
        // Given
        val loginRequest = LoginRequest("test@example.com", "wrong_password")
        
        mockWebServer.enqueue(
            MockResponse()
                .setResponseCode(401)
                .setBody("""{"error": "Invalid credentials"}""")
                .setHeader("Content-Type", "application/json")
        )
        
        // When & Then
        assertThrows<HttpException> {
            runBlocking { apiService.login(loginRequest) }
        }
    }
}
```

### 数据库集成测试

```kotlin
@RunWith(AndroidJUnit4::class)
class UserDaoTest {
    
    private lateinit var database: AppDatabase
    private lateinit var userDao: UserDao
    
    @Before
    fun setup() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        database = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        userDao = database.userDao()
    }
    
    @After
    fun tearDown() {
        database.close()
    }
    
    @Test
    fun insertAndGetUser() = runTest {
        // Given
        val user = UserEntity(
            id = "1",
            email = "test@example.com",
            name = "Test User",
            createdAt = System.currentTimeMillis()
        )
        
        // When
        userDao.insertUser(user)
        val retrievedUser = userDao.getUserById("1")
        
        // Then
        assertThat(retrievedUser).isEqualTo(user)
    }
    
    @Test
    fun updateUser() = runTest {
        // Given
        val originalUser = UserEntity(
            id = "1",
            email = "test@example.com",
            name = "Original Name",
            createdAt = System.currentTimeMillis()
        )
        
        userDao.insertUser(originalUser)
        
        // When
        val updatedUser = originalUser.copy(name = "Updated Name")
        userDao.updateUser(updatedUser)
        val retrievedUser = userDao.getUserById("1")
        
        // Then
        assertThat(retrievedUser?.name).isEqualTo("Updated Name")
    }
    
    @Test
    fun deleteUser() = runTest {
        // Given
        val user = UserEntity(
            id = "1",
            email = "test@example.com",
            name = "Test User",
            createdAt = System.currentTimeMillis()
        )
        
        userDao.insertUser(user)
        
        // When
        userDao.deleteUser(user)
        val retrievedUser = userDao.getUserById("1")
        
        // Then
        assertThat(retrievedUser).isNull()
    }
    
    @Test
    fun getAllUsers() = runTest {
        // Given
        val users = listOf(
            UserEntity("1", "user1@example.com", "User 1", System.currentTimeMillis()),
            UserEntity("2", "user2@example.com", "User 2", System.currentTimeMillis()),
            UserEntity("3", "user3@example.com", "User 3", System.currentTimeMillis())
        )
        
        users.forEach { userDao.insertUser(it) }
        
        // When
        val allUsers = userDao.getAllUsers()
        
        // Then
        assertThat(allUsers).hasSize(3)
        assertThat(allUsers).containsExactlyElementsIn(users)
    }
}
```

### Repository 集成测试

```kotlin
@HiltAndroidTest
class UserRepositoryIntegrationTest {
    
    @get:Rule
    var hiltRule = HiltAndroidRule(this)
    
    @Inject
    lateinit var repository: UserRepository
    
    @Inject
    lateinit var database: AppDatabase
    
    private lateinit var mockWebServer: MockWebServer
    
    @BeforeEach
    fun setup() {
        hiltRule.inject()
        mockWebServer = MockWebServer()
        mockWebServer.start()
    }
    
    @AfterEach
    fun tearDown() {
        database.clearAllTables()
        mockWebServer.shutdown()
    }
    
    @Test
    fun `login should save user to database and return user`() = runTest {
        // Given
        val email = "test@example.com"
        val password = "password123"
        val loginResponse = LoginResponse(
            user = UserDto("1", email, "Test User"),
            token = "auth_token_123"
        )
        
        mockWebServer.enqueue(
            MockResponse()
                .setResponseCode(200)
                .setBody(Json.encodeToString(loginResponse))
                .setHeader("Content-Type", "application/json")
        )
        
        // When
        val result = repository.login(email, password)
        
        // Then
        assertThat(result.isSuccess).isTrue()
        
        val user = result.getOrNull()
        assertThat(user?.id).isEqualTo("1")
        assertThat(user?.email).isEqualTo(email)
        
        // Verify user is saved in database
        val savedUser = database.userDao().getUserById("1")
        assertThat(savedUser).isNotNull()
        assertThat(savedUser?.email).isEqualTo(email)
    }
    
    @Test
    fun `getCurrentUser should return cached user when available`() = runTest {
        // Given - Insert user directly into database
        val userEntity = UserEntity(
            id = "1",
            email = "test@example.com",
            name = "Test User",
            createdAt = System.currentTimeMillis()
        )
        database.userDao().insertUser(userEntity)
        
        // When
        val user = repository.getCurrentUser()
        
        // Then
        assertThat(user).isNotNull()
        assertThat(user?.id).isEqualTo("1")
        assertThat(user?.email).isEqualTo("test@example.com")
        
        // Verify no API call was made
        assertThat(mockWebServer.requestCount).isEqualTo(0)
    }
}
```

## 📱 UI测试

### Espresso UI 测试

```kotlin
@HiltAndroidTest
@RunWith(AndroidJUnit4::class)
class LoginActivityTest {
    
    @get:Rule
    var hiltRule = HiltAndroidRule(this)
    
    @get:Rule
    var activityRule = ActivityScenarioRule(LoginActivity::class.java)
    
    @BeforeEach
    fun setup() {
        hiltRule.inject()
    }
    
    @Test
    fun loginWithValidCredentials_shouldNavigateToMainActivity() {
        // Given
        val email = "test@example.com"
        val password = "password123"
        
        // When
        onView(withId(R.id.etEmail))
            .perform(typeText(email), closeSoftKeyboard())
        
        onView(withId(R.id.etPassword))
            .perform(typeText(password), closeSoftKeyboard())
        
        onView(withId(R.id.btnLogin))
            .perform(click())
        
        // Then
        intended(hasComponent(MainActivity::class.java.name))
    }
    
    @Test
    fun loginWithInvalidEmail_shouldShowErrorMessage() {
        // Given
        val invalidEmail = "invalid-email"
        val password = "password123"
        
        // When
        onView(withId(R.id.etEmail))
            .perform(typeText(invalidEmail), closeSoftKeyboard())
        
        onView(withId(R.id.etPassword))
            .perform(typeText(password), closeSoftKeyboard())
        
        onView(withId(R.id.btnLogin))
            .perform(click())
        
        // Then
        onView(withId(R.id.tilEmail))
            .check(matches(hasTextInputLayoutErrorText("Invalid email format")))
    }
    
    @Test
    fun loginWithEmptyFields_shouldShowValidationErrors() {
        // When
        onView(withId(R.id.btnLogin))
            .perform(click())
        
        // Then
        onView(withId(R.id.tilEmail))
            .check(matches(hasTextInputLayoutErrorText("Email is required")))
        
        onView(withId(R.id.tilPassword))
            .check(matches(hasTextInputLayoutErrorText("Password is required")))
    }
    
    @Test
    fun loginInProgress_shouldShowLoadingState() {
        // Given
        val email = "test@example.com"
        val password = "password123"
        
        // When
        onView(withId(R.id.etEmail))
            .perform(typeText(email), closeSoftKeyboard())
        
        onView(withId(R.id.etPassword))
            .perform(typeText(password), closeSoftKeyboard())
        
        onView(withId(R.id.btnLogin))
            .perform(click())
        
        // Then
        onView(withId(R.id.progressBar))
            .check(matches(isDisplayed()))
        
        onView(withId(R.id.btnLogin))
            .check(matches(not(isEnabled())))
    }
}
```

### 自定义 Matcher

```kotlin
object CustomMatchers {
    
    fun hasTextInputLayoutErrorText(expectedErrorText: String): Matcher<View> {
        return object : BoundedMatcher<View, TextInputLayout>(TextInputLayout::class.java) {
            
            override fun describeTo(description: Description) {
                description.appendText("with error text: $expectedErrorText")
            }
            
            override fun matchesSafely(textInputLayout: TextInputLayout): Boolean {
                val error = textInputLayout.error
                return error != null && expectedErrorText == error.toString()
            }
        }
    }
    
    fun withRecyclerView(recyclerViewId: Int): RecyclerViewMatcher {
        return RecyclerViewMatcher(recyclerViewId)
    }
}

class RecyclerViewMatcher(private val recyclerViewId: Int) {
    
    fun atPosition(position: Int): Matcher<View> {
        return atPositionOnView(position, -1)
    }
    
    fun atPositionOnView(position: Int, targetViewId: Int): Matcher<View> {
        return object : TypeSafeMatcher<View>() {
            var resources: Resources? = null
            var childView: View? = null
            
            override fun describeTo(description: Description) {
                var idDescription = recyclerViewId.toString()
                if (resources != null) {
                    idDescription = try {
                        resources!!.getResourceName(recyclerViewId)
                    } catch (var4: Resources.NotFoundException) {
                        "$recyclerViewId (resource name not found)"
                    }
                }
                description.appendText("RecyclerView with id: $idDescription at position: $position")
            }
            
            override fun matchesSafely(view: View): Boolean {
                resources = view.resources
                
                if (childView == null) {
                    val recyclerView = view.rootView.findViewById<RecyclerView>(recyclerViewId)
                        ?: return false
                    
                    if (recyclerView.id == recyclerViewId) {
                        val viewHolder = recyclerView.findViewHolderForAdapterPosition(position)
                            ?: return false
                        
                        childView = viewHolder.itemView
                    } else {
                        return false
                    }
                }
                
                return if (targetViewId == -1) {
                    view == childView
                } else {
                    val targetView = childView!!.findViewById<View>(targetViewId)
                    view == targetView
                }
            }
        }
    }
}
```

## 🛠️ 测试工具链

### 测试配置

```kotlin
// build.gradle.kts (app module)
android {
    testOptions {
        unitTests {
            isIncludeAndroidResources = true
            isReturnDefaultValues = true
        }
        
        animationsDisabled = true
    }
}

dependencies {
    // Unit Testing
    testImplementation("junit:junit:4.13.2")
    testImplementation("org.junit.jupiter:junit-jupiter:5.8.2")
    testImplementation("io.mockk:mockk:1.13.2")
    testImplementation("com.google.truth:truth:1.1.3")
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.6.4")
    testImplementation("app.cash.turbine:turbine:0.12.1")
    
    // Android Testing
    androidTestImplementation("androidx.test.ext:junit:1.1.4")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.0")
    androidTestImplementation("androidx.test.espresso:espresso-intents:3.5.0")
    androidTestImplementation("androidx.test.espresso:espresso-contrib:3.5.0")
    androidTestImplementation("androidx.test:runner:1.5.1")
    androidTestImplementation("androidx.test:rules:1.5.0")
    
    // Hilt Testing
    androidTestImplementation("com.google.dagger:hilt-android-testing:2.44")
    kaptAndroidTest("com.google.dagger:hilt-android-compiler:2.44")
    
    // Room Testing
    testImplementation("androidx.room:room-testing:2.4.3")
    
    // MockWebServer
    testImplementation("com.squareup.okhttp3:mockwebserver:4.10.0")
}
```

### 测试基类

```kotlin
// 单元测试基类
abstract class BaseUnitTest {
    
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()
    
    @BeforeEach
    fun baseSetup() {
        MockKAnnotations.init(this)
    }
}

// UI测试基类
@HiltAndroidTest
abstract class BaseUITest {
    
    @get:Rule
    var hiltRule = HiltAndroidRule(this)
    
    @BeforeEach
    fun baseSetup() {
        hiltRule.inject()
    }
    
    protected fun waitForView(viewMatcher: Matcher<View>, timeout: Long = 5000) {
        onView(isRoot()).perform(waitForMatch(viewMatcher, timeout))
    }
    
    private fun waitForMatch(viewMatcher: Matcher<View>, millis: Long): ViewAction {
        return object : ViewAction {
            override fun getConstraints(): Matcher<View> {
                return isRoot()
            }
            
            override fun getDescription(): String {
                return "wait for a specific view with matcher <$viewMatcher> during $millis millis."
            }
            
            override fun perform(uiController: UiController, view: View) {
                uiController.loopMainThreadUntilIdle()
                val startTime = System.currentTimeMillis()
                val endTime = startTime + millis
                
                do {
                    for (child in TreeIterables.breadthFirstViewTraversal(view)) {
                        if (viewMatcher.matches(child)) {
                            return
                        }
                    }
                    uiController.loopMainThreadForAtLeast(50)
                } while (System.currentTimeMillis() < endTime)
                
                throw PerformException.Builder()
                    .withActionDescription(this.description)
                    .withViewDescription(HumanReadables.describe(view))
                    .withCause(TimeoutException())
                    .build()
            }
        }
    }
}
```

### 测试数据工厂

```kotlin
object TestDataFactory {
    
    fun createUser(
        id: String = "test_id",
        email: String = "test@example.com",
        name: String = "Test User"
    ): User {
        return User(
            id = id,
            email = email,
            name = name
        )
    }
    
    fun createUserEntity(
        id: String = "test_id",
        email: String = "test@example.com",
        name: String = "Test User",
        createdAt: Long = System.currentTimeMillis()
    ): UserEntity {
        return UserEntity(
            id = id,
            email = email,
            name = name,
            createdAt = createdAt
        )
    }
    
    fun createLoginResponse(
        user: UserDto = createUserDto(),
        token: String = "test_token"
    ): LoginResponse {
        return LoginResponse(
            user = user,
            token = token
        )
    }
    
    fun createUserDto(
        id: String = "test_id",
        email: String = "test@example.com",
        name: String = "Test User"
    ): UserDto {
        return UserDto(
            id = id,
            email = email,
            name = name
        )
    }
}
```

## 📊 测试覆盖率

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

tasks.register("jacocoTestReport", JacocoReport::class) {
    dependsOn("testDebugUnitTest", "createDebugCoverageReport")
    
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
        "**/hilt_aggregated_deps/**"
    )
    
    val debugTree = fileTree("${buildDir}/tmp/kotlin-classes/debug") {
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

- **整体覆盖率**: ≥ 80%
- **业务逻辑**: ≥ 90%
- **ViewModel**: ≥ 95%
- **Repository**: ≥ 85%
- **UseCase**: ≥ 90%

## 🎯 测试最佳实践

### 1. 测试命名
- **描述性命名**: 测试名称应该描述测试的行为和期望结果
- **Given-When-Then**: 使用 GWT 模式组织测试代码
- **一致性**: 保持团队内测试命名的一致性

### 2. 测试独立性
- **无依赖**: 测试之间不应该有依赖关系
- **可重复**: 测试应该可以重复执行并得到相同结果
- **隔离**: 使用 Mock 隔离外部依赖

### 3. 测试维护
- **简洁**: 保持测试代码简洁易懂
- **重构**: 定期重构测试代码
- **更新**: 及时更新测试以反映代码变更

### 4. 持续集成
- **自动化**: 在 CI/CD 中自动运行测试
- **快速反馈**: 确保测试能够快速执行
- **质量门禁**: 设置测试覆盖率和质量标准

---

*测试是保证代码质量的重要手段，应该贯穿整个开发过程。*