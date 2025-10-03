# 集成测试指南

本文档详细说明了 Android MVVM 框架中集成测试的编写规范和实施策略。

## 📋 目录

- [集成测试概述](#集成测试概述)
- [测试环境配置](#测试环境配置)
- [API 集成测试](#api-集成测试)
- [数据库集成测试](#数据库集成测试)
- [组件集成测试](#组件集成测试)
- [端到端测试](#端到端测试)
- [测试最佳实践](#测试最佳实践)

## 🎯 集成测试概述

### 集成测试的目标

- **验证组件协作**: 确保不同组件之间正确协作
- **测试数据流**: 验证数据在系统中的流转
- **发现接口问题**: 及早发现组件接口不匹配问题
- **验证配置**: 确保系统配置正确

### 测试层次

```
┌─────────────────────────────────────────┐
│              端到端测试                  │  ← 完整用户流程
├─────────────────────────────────────────┤
│              组件集成测试                │  ← Repository + DAO + API
├─────────────────────────────────────────┤
│              API 集成测试                │  ← 网络层测试
├─────────────────────────────────────────┤
│              数据库集成测试              │  ← 数据持久化测试
└─────────────────────────────────────────┘
```

## ⚙️ 测试环境配置

### 依赖配置

```kotlin
// build.gradle.kts (app module)
dependencies {
    // Android Testing
    androidTestImplementation("androidx.test.ext:junit:1.1.4")
    androidTestImplementation("androidx.test:runner:1.5.1")
    androidTestImplementation("androidx.test:rules:1.5.0")
    
    // Hilt Testing
    androidTestImplementation("com.google.dagger:hilt-android-testing:2.44")
    kaptAndroidTest("com.google.dagger:hilt-android-compiler:2.44")
    
    // Room Testing
    androidTestImplementation("androidx.room:room-testing:2.4.3")
    
    // MockWebServer
    androidTestImplementation("com.squareup.okhttp3:mockwebserver:4.10.0")
    
    // Coroutines Testing
    androidTestImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.6.4")
    
    // Truth
    androidTestImplementation("com.google.truth:truth:1.1.3")
    
    // Test Core
    androidTestImplementation("androidx.test:core:1.5.0")
    androidTestImplementation("androidx.test:core-ktx:1.5.0")
}
```

### 测试基类

```kotlin
@HiltAndroidTest
abstract class BaseIntegrationTest {
    
    @get:Rule
    var hiltRule = HiltAndroidRule(this)
    
    @BeforeEach
    fun baseSetup() {
        hiltRule.inject()
    }
    
    protected fun runIntegrationTest(block: suspend TestScope.() -> Unit) = 
        runTest(block = block)
}
```

### 测试数据库配置

```kotlin
@Module
@TestInstallIn(
    components = [SingletonComponent::class],
    replaces = [DatabaseModule::class]
)
object TestDatabaseModule {
    
    @Provides
    @Singleton
    fun provideTestDatabase(@ApplicationContext context: Context): AppDatabase {
        return Room.inMemoryDatabaseBuilder(
            context,
            AppDatabase::class.java
        )
        .allowMainThreadQueries()
        .build()
    }
}
```

## 🌐 API 集成测试

### MockWebServer 配置

```kotlin
@HiltAndroidTest
class ApiIntegrationTest : BaseIntegrationTest() {
    
    @Inject
    lateinit var apiService: ApiService
    
    private lateinit var mockWebServer: MockWebServer
    
    @BeforeEach
    fun setup() {
        super.baseSetup()
        mockWebServer = MockWebServer()
        mockWebServer.start()
    }
    
    @AfterEach
    fun tearDown() {
        mockWebServer.shutdown()
    }
    
    @Test
    fun loginApi_withValidCredentials_shouldReturnUserData() = runIntegrationTest {
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
        
        // Verify request
        val recordedRequest = mockWebServer.takeRequest()
        assertThat(recordedRequest.method).isEqualTo("POST")
        assertThat(recordedRequest.path).isEqualTo("/api/auth/login")
        assertThat(recordedRequest.getHeader("Content-Type")).contains("application/json")
        
        val requestBody = Json.decodeFromString<LoginRequest>(recordedRequest.body.readUtf8())
        assertThat(requestBody).isEqualTo(loginRequest)
    }
    
    @Test
    fun loginApi_withInvalidCredentials_shouldThrowHttpException() = runIntegrationTest {
        // Given
        val loginRequest = LoginRequest("test@example.com", "wrong_password")
        val errorResponse = ErrorResponse("Invalid credentials", 401)
        
        mockWebServer.enqueue(
            MockResponse()
                .setResponseCode(401)
                .setBody(Json.encodeToString(errorResponse))
                .setHeader("Content-Type", "application/json")
        )
        
        // When & Then
        val exception = assertThrows<HttpException> {
            runBlocking { apiService.login(loginRequest) }
        }
        
        assertThat(exception.code()).isEqualTo(401)
    }
    
    @Test
    fun getUserProfile_withAuthToken_shouldReturnUserData() = runIntegrationTest {
        // Given
        val expectedUser = UserDto("1", "test@example.com", "Test User")
        
        mockWebServer.enqueue(
            MockResponse()
                .setResponseCode(200)
                .setBody(Json.encodeToString(expectedUser))
                .setHeader("Content-Type", "application/json")
        )
        
        // When
        val response = apiService.getCurrentUser()
        
        // Then
        assertThat(response).isEqualTo(expectedUser)
        
        // Verify authorization header
        val recordedRequest = mockWebServer.takeRequest()
        assertThat(recordedRequest.getHeader("Authorization")).isNotNull()
    }
    
    @Test
    fun apiCall_withNetworkTimeout_shouldHandleGracefully() = runIntegrationTest {
        // Given
        mockWebServer.enqueue(
            MockResponse()
                .setSocketPolicy(SocketPolicy.NO_RESPONSE)
        )
        
        // When & Then
        assertThrows<SocketTimeoutException> {
            runBlocking { apiService.getCurrentUser() }
        }
    }
}
```

### API 重试机制测试

```kotlin
@HiltAndroidTest
class ApiRetryIntegrationTest : BaseIntegrationTest() {
    
    @Inject
    lateinit var apiService: ApiService
    
    private lateinit var mockWebServer: MockWebServer
    
    @BeforeEach
    fun setup() {
        super.baseSetup()
        mockWebServer = MockWebServer()
        mockWebServer.start()
    }
    
    @AfterEach
    fun tearDown() {
        mockWebServer.shutdown()
    }
    
    @Test
    fun apiCall_withTransientError_shouldRetryAndSucceed() = runIntegrationTest {
        // Given
        val expectedUser = UserDto("1", "test@example.com", "Test User")
        
        // First call fails with 500
        mockWebServer.enqueue(
            MockResponse()
                .setResponseCode(500)
                .setBody("Internal Server Error")
        )
        
        // Second call succeeds
        mockWebServer.enqueue(
            MockResponse()
                .setResponseCode(200)
                .setBody(Json.encodeToString(expectedUser))
                .setHeader("Content-Type", "application/json")
        )
        
        // When
        val response = apiService.getCurrentUser()
        
        // Then
        assertThat(response).isEqualTo(expectedUser)
        assertThat(mockWebServer.requestCount).isEqualTo(2)
    }
    
    @Test
    fun apiCall_withPermanentError_shouldNotRetry() = runIntegrationTest {
        // Given
        mockWebServer.enqueue(
            MockResponse()
                .setResponseCode(404)
                .setBody("Not Found")
        )
        
        // When & Then
        assertThrows<HttpException> {
            runBlocking { apiService.getCurrentUser() }
        }
        
        assertThat(mockWebServer.requestCount).isEqualTo(1)
    }
}
```

## 🗄️ 数据库集成测试

### Room 数据库测试

```kotlin
@RunWith(AndroidJUnit4::class)
class DatabaseIntegrationTest {
    
    private lateinit var database: AppDatabase
    private lateinit var userDao: UserDao
    private lateinit var postDao: PostDao
    
    @Before
    fun setup() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        database = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        
        userDao = database.userDao()
        postDao = database.postDao()
    }
    
    @After
    fun tearDown() {
        database.close()
    }
    
    @Test
    fun insertUserAndPosts_shouldMaintainRelationship() = runTest {
        // Given
        val user = UserEntity(
            id = "user1",
            email = "test@example.com",
            name = "Test User",
            createdAt = System.currentTimeMillis()
        )
        
        val posts = listOf(
            PostEntity(
                id = "post1",
                userId = "user1",
                title = "Post 1",
                content = "Content 1",
                createdAt = System.currentTimeMillis()
            ),
            PostEntity(
                id = "post2",
                userId = "user1",
                title = "Post 2",
                content = "Content 2",
                createdAt = System.currentTimeMillis()
            )
        )
        
        // When
        userDao.insertUser(user)
        posts.forEach { postDao.insertPost(it) }
        
        // Then
        val userWithPosts = userDao.getUserWithPosts("user1")
        assertThat(userWithPosts).isNotNull()
        assertThat(userWithPosts!!.user).isEqualTo(user)
        assertThat(userWithPosts.posts).hasSize(2)
        assertThat(userWithPosts.posts).containsExactlyElementsIn(posts)
    }
    
    @Test
    fun deleteUser_shouldCascadeDeletePosts() = runTest {
        // Given
        val user = UserEntity("user1", "test@example.com", "Test User", System.currentTimeMillis())
        val post = PostEntity("post1", "user1", "Post 1", "Content 1", System.currentTimeMillis())
        
        userDao.insertUser(user)
        postDao.insertPost(post)
        
        // When
        userDao.deleteUser(user)
        
        // Then
        val deletedUser = userDao.getUserById("user1")
        val orphanedPost = postDao.getPostById("post1")
        
        assertThat(deletedUser).isNull()
        assertThat(orphanedPost).isNull() // Should be cascade deleted
    }
    
    @Test
    fun updateUser_shouldPreservePosts() = runTest {
        // Given
        val originalUser = UserEntity("user1", "test@example.com", "Original Name", System.currentTimeMillis())
        val post = PostEntity("post1", "user1", "Post 1", "Content 1", System.currentTimeMillis())
        
        userDao.insertUser(originalUser)
        postDao.insertPost(post)
        
        // When
        val updatedUser = originalUser.copy(name = "Updated Name")
        userDao.updateUser(updatedUser)
        
        // Then
        val userWithPosts = userDao.getUserWithPosts("user1")
        assertThat(userWithPosts).isNotNull()
        assertThat(userWithPosts!!.user.name).isEqualTo("Updated Name")
        assertThat(userWithPosts.posts).hasSize(1)
    }
    
    @Test
    fun complexQuery_shouldReturnCorrectResults() = runTest {
        // Given
        val users = listOf(
            UserEntity("user1", "user1@example.com", "User 1", System.currentTimeMillis()),
            UserEntity("user2", "user2@example.com", "User 2", System.currentTimeMillis())
        )
        
        val posts = listOf(
            PostEntity("post1", "user1", "Post 1", "Content 1", System.currentTimeMillis()),
            PostEntity("post2", "user1", "Post 2", "Content 2", System.currentTimeMillis()),
            PostEntity("post3", "user2", "Post 3", "Content 3", System.currentTimeMillis())
        )
        
        users.forEach { userDao.insertUser(it) }
        posts.forEach { postDao.insertPost(it) }
        
        // When
        val usersWithMultiplePosts = userDao.getUsersWithMultiplePosts()
        
        // Then
        assertThat(usersWithMultiplePosts).hasSize(1)
        assertThat(usersWithMultiplePosts[0].user.id).isEqualTo("user1")
        assertThat(usersWithMultiplePosts[0].posts).hasSize(2)
    }
}
```

### 数据库迁移测试

```kotlin
@RunWith(AndroidJUnit4::class)
class DatabaseMigrationTest {
    
    private val TEST_DB = "migration-test"
    
    @get:Rule
    val helper: MigrationTestHelper = MigrationTestHelper(
        InstrumentationRegistry.getInstrumentation(),
        AppDatabase::class.java
    )
    
    @Test
    fun migrate1To2_shouldAddNewColumn() {
        // Given - Create database with version 1
        var db = helper.createDatabase(TEST_DB, 1).apply {
            execSQL("""
                INSERT INTO users (id, email, name, created_at) 
                VALUES ('1', 'test@example.com', 'Test User', 1640995200000)
            """.trimIndent())
            close()
        }
        
        // When - Migrate to version 2
        db = helper.runMigrationsAndValidate(TEST_DB, 2, true, MIGRATION_1_2)
        
        // Then - Verify new column exists and data is preserved
        val cursor = db.query("SELECT * FROM users WHERE id = '1'")
        assertThat(cursor.moveToFirst()).isTrue()
        
        val emailIndex = cursor.getColumnIndex("email")
        val nameIndex = cursor.getColumnIndex("name")
        val avatarIndex = cursor.getColumnIndex("avatar_url") // New column
        
        assertThat(cursor.getString(emailIndex)).isEqualTo("test@example.com")
        assertThat(cursor.getString(nameIndex)).isEqualTo("Test User")
        assertThat(cursor.isNull(avatarIndex)).isTrue() // New column should be null
        
        cursor.close()
    }
    
    @Test
    fun migrateAll_shouldPreserveData() {
        // Given - Create database with version 1 and insert test data
        var db = helper.createDatabase(TEST_DB, 1).apply {
            execSQL("""
                INSERT INTO users (id, email, name, created_at) 
                VALUES ('1', 'test@example.com', 'Test User', 1640995200000)
            """.trimIndent())
            close()
        }
        
        // When - Migrate through all versions
        db = helper.runMigrationsAndValidate(
            TEST_DB, 
            AppDatabase.LATEST_VERSION, 
            true, 
            *ALL_MIGRATIONS
        )
        
        // Then - Verify data integrity
        val cursor = db.query("SELECT * FROM users WHERE id = '1'")
        assertThat(cursor.moveToFirst()).isTrue()
        assertThat(cursor.getString(cursor.getColumnIndex("email"))).isEqualTo("test@example.com")
        cursor.close()
    }
}
```

## 🔗 组件集成测试

### Repository 集成测试

```kotlin
@HiltAndroidTest
class UserRepositoryIntegrationTest : BaseIntegrationTest() {
    
    @Inject
    lateinit var userRepository: UserRepository
    
    @Inject
    lateinit var database: AppDatabase
    
    private lateinit var mockWebServer: MockWebServer
    
    @BeforeEach
    fun setup() {
        super.baseSetup()
        mockWebServer = MockWebServer()
        mockWebServer.start()
    }
    
    @AfterEach
    fun tearDown() {
        database.clearAllTables()
        mockWebServer.shutdown()
    }
    
    @Test
    fun login_shouldSaveUserToDatabase() = runIntegrationTest {
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
        val result = userRepository.login(email, password)
        
        // Then
        assertThat(result.isSuccess).isTrue()
        
        // Verify user is saved in database
        val savedUser = database.userDao().getUserById("1")
        assertThat(savedUser).isNotNull()
        assertThat(savedUser!!.email).isEqualTo(email)
        assertThat(savedUser.name).isEqualTo("Test User")
    }
    
    @Test
    fun getCurrentUser_shouldReturnCachedUserWhenAvailable() = runIntegrationTest {
        // Given - Insert user directly into database
        val userEntity = UserEntity(
            id = "1",
            email = "test@example.com",
            name = "Test User",
            createdAt = System.currentTimeMillis()
        )
        database.userDao().insertUser(userEntity)
        
        // When
        val user = userRepository.getCurrentUser()
        
        // Then
        assertThat(user).isNotNull()
        assertThat(user!!.id).isEqualTo("1")
        assertThat(user.email).isEqualTo("test@example.com")
        
        // Verify no API call was made
        assertThat(mockWebServer.requestCount).isEqualTo(0)
    }
    
    @Test
    fun getCurrentUser_shouldFetchFromApiWhenCacheEmpty() = runIntegrationTest {
        // Given
        val apiUser = UserDto("1", "test@example.com", "Test User")
        
        mockWebServer.enqueue(
            MockResponse()
                .setResponseCode(200)
                .setBody(Json.encodeToString(apiUser))
                .setHeader("Content-Type", "application/json")
        )
        
        // When
        val user = userRepository.getCurrentUser()
        
        // Then
        assertThat(user).isNotNull()
        assertThat(user!!.id).isEqualTo("1")
        
        // Verify API was called
        assertThat(mockWebServer.requestCount).isEqualTo(1)
        
        // Verify user was cached
        val cachedUser = database.userDao().getUserById("1")
        assertThat(cachedUser).isNotNull()
    }
    
    @Test
    fun syncUserData_shouldUpdateLocalCache() = runIntegrationTest {
        // Given - User exists in local cache
        val localUser = UserEntity(
            id = "1",
            email = "test@example.com",
            name = "Old Name",
            createdAt = System.currentTimeMillis()
        )
        database.userDao().insertUser(localUser)
        
        // Remote user has updated data
        val remoteUser = UserDto("1", "test@example.com", "New Name")
        
        mockWebServer.enqueue(
            MockResponse()
                .setResponseCode(200)
                .setBody(Json.encodeToString(remoteUser))
                .setHeader("Content-Type", "application/json")
        )
        
        // When
        userRepository.syncUserData()
        
        // Then
        val updatedUser = database.userDao().getUserById("1")
        assertThat(updatedUser).isNotNull()
        assertThat(updatedUser!!.name).isEqualTo("New Name")
    }
}
```

### UseCase 集成测试

```kotlin
@HiltAndroidTest
class LoginUseCaseIntegrationTest : BaseIntegrationTest() {
    
    @Inject
    lateinit var loginUseCase: LoginUseCase
    
    @Inject
    lateinit var database: AppDatabase
    
    private lateinit var mockWebServer: MockWebServer
    
    @BeforeEach
    fun setup() {
        super.baseSetup()
        mockWebServer = MockWebServer()
        mockWebServer.start()
    }
    
    @AfterEach
    fun tearDown() {
        database.clearAllTables()
        mockWebServer.shutdown()
    }
    
    @Test
    fun execute_withValidCredentials_shouldCompleteLoginFlow() = runIntegrationTest {
        // Given
        val request = LoginRequest("test@example.com", "password123")
        val loginResponse = LoginResponse(
            user = UserDto("1", "test@example.com", "Test User"),
            token = "auth_token_123"
        )
        
        mockWebServer.enqueue(
            MockResponse()
                .setResponseCode(200)
                .setBody(Json.encodeToString(loginResponse))
                .setHeader("Content-Type", "application/json")
        )
        
        // When
        val result = loginUseCase.execute(request)
        
        // Then
        assertThat(result.isSuccess).isTrue()
        
        val user = result.getOrNull()
        assertThat(user).isNotNull()
        assertThat(user!!.email).isEqualTo("test@example.com")
        
        // Verify complete flow: API call -> Database save -> Token save
        assertThat(mockWebServer.requestCount).isEqualTo(1)
        
        val savedUser = database.userDao().getUserById("1")
        assertThat(savedUser).isNotNull()
    }
}
```

## 🔄 端到端测试

### 完整用户流程测试

```kotlin
@HiltAndroidTest
class UserJourneyIntegrationTest : BaseIntegrationTest() {
    
    @Inject
    lateinit var userRepository: UserRepository
    
    @Inject
    lateinit var postRepository: PostRepository
    
    @Inject
    lateinit var database: AppDatabase
    
    private lateinit var mockWebServer: MockWebServer
    
    @BeforeEach
    fun setup() {
        super.baseSetup()
        mockWebServer = MockWebServer()
        mockWebServer.start()
    }
    
    @AfterEach
    fun tearDown() {
        database.clearAllTables()
        mockWebServer.shutdown()
    }
    
    @Test
    fun completeUserJourney_loginCreatePostLogout() = runIntegrationTest {
        // Step 1: User Login
        val loginResponse = LoginResponse(
            user = UserDto("1", "test@example.com", "Test User"),
            token = "auth_token_123"
        )
        
        mockWebServer.enqueue(
            MockResponse()
                .setResponseCode(200)
                .setBody(Json.encodeToString(loginResponse))
                .setHeader("Content-Type", "application/json")
        )
        
        val loginResult = userRepository.login("test@example.com", "password123")
        assertThat(loginResult.isSuccess).isTrue()
        
        // Step 2: Create Post
        val createPostResponse = PostDto(
            id = "post1",
            userId = "1",
            title = "My First Post",
            content = "This is my first post content"
        )
        
        mockWebServer.enqueue(
            MockResponse()
                .setResponseCode(201)
                .setBody(Json.encodeToString(createPostResponse))
                .setHeader("Content-Type", "application/json")
        )
        
        val createPostResult = postRepository.createPost(
            title = "My First Post",
            content = "This is my first post content"
        )
        assertThat(createPostResult.isSuccess).isTrue()
        
        // Step 3: Verify Data Consistency
        val userWithPosts = database.userDao().getUserWithPosts("1")
        assertThat(userWithPosts).isNotNull()
        assertThat(userWithPosts!!.posts).hasSize(1)
        assertThat(userWithPosts.posts[0].title).isEqualTo("My First Post")
        
        // Step 4: User Logout
        userRepository.logout()
        
        val currentUser = userRepository.getCurrentUser()
        assertThat(currentUser).isNull()
    }
    
    @Test
    fun offlineToOnlineSync_shouldSyncPendingChanges() = runIntegrationTest {
        // Step 1: Login while online
        val loginResponse = LoginResponse(
            user = UserDto("1", "test@example.com", "Test User"),
            token = "auth_token_123"
        )
        
        mockWebServer.enqueue(
            MockResponse()
                .setResponseCode(200)
                .setBody(Json.encodeToString(loginResponse))
        )
        
        userRepository.login("test@example.com", "password123")
        
        // Step 2: Create post while offline (should be stored locally)
        // Simulate offline by not enqueuing response
        val offlinePostResult = postRepository.createPost(
            title = "Offline Post",
            content = "Created while offline"
        )
        
        // Should succeed locally but marked as pending sync
        assertThat(offlinePostResult.isSuccess).isTrue()
        
        val pendingPosts = database.postDao().getPendingSyncPosts()
        assertThat(pendingPosts).hasSize(1)
        
        // Step 3: Come back online and sync
        val syncResponse = PostDto(
            id = "post1",
            userId = "1",
            title = "Offline Post",
            content = "Created while offline"
        )
        
        mockWebServer.enqueue(
            MockResponse()
                .setResponseCode(201)
                .setBody(Json.encodeToString(syncResponse))
        )
        
        postRepository.syncPendingPosts()
        
        // Verify sync completed
        val pendingPostsAfterSync = database.postDao().getPendingSyncPosts()
        assertThat(pendingPostsAfterSync).isEmpty()
        
        val syncedPost = database.postDao().getPostById("post1")
        assertThat(syncedPost).isNotNull()
        assertThat(syncedPost!!.needsSync).isFalse()
    }
}
```

## 📊 测试最佳实践

### 1. 测试数据管理

```kotlin
// ✅ 使用测试数据构建器
class TestDataBuilder {
    
    fun createTestUser(id: String = "test_user"): UserEntity {
        return UserEntity(
            id = id,
            email = "$id@example.com",
            name = "Test User $id",
            createdAt = System.currentTimeMillis()
        )
    }
    
    fun createTestPost(userId: String, postId: String = "test_post"): PostEntity {
        return PostEntity(
            id = postId,
            userId = userId,
            title = "Test Post $postId",
            content = "Test content for $postId",
            createdAt = System.currentTimeMillis()
        )
    }
}

// ✅ 数据清理
@AfterEach
fun cleanupTestData() {
    database.clearAllTables()
    mockWebServer.shutdown()
}
```

### 2. Mock 服务器管理

```kotlin
class MockServerManager {
    
    private val mockWebServer = MockWebServer()
    
    fun start() {
        mockWebServer.start()
    }
    
    fun shutdown() {
        mockWebServer.shutdown()
    }
    
    fun enqueueSuccess(response: Any) {
        mockWebServer.enqueue(
            MockResponse()
                .setResponseCode(200)
                .setBody(Json.encodeToString(response))
                .setHeader("Content-Type", "application/json")
        )
    }
    
    fun enqueueError(code: Int, message: String) {
        mockWebServer.enqueue(
            MockResponse()
                .setResponseCode(code)
                .setBody("""{"error": "$message"}""")
                .setHeader("Content-Type", "application/json")
        )
    }
    
    fun enqueueNetworkError() {
        mockWebServer.enqueue(
            MockResponse()
                .setSocketPolicy(SocketPolicy.DISCONNECT_AFTER_REQUEST)
        )
    }
}
```

### 3. 测试场景覆盖

```kotlin
// ✅ 覆盖正常流程
@Test
fun happyPath_shouldWorkCorrectly() { /* ... */ }

// ✅ 覆盖错误场景
@Test
fun networkError_shouldHandleGracefully() { /* ... */ }

// ✅ 覆盖边界条件
@Test
fun emptyResponse_shouldHandleCorrectly() { /* ... */ }

// ✅ 覆盖并发场景
@Test
fun concurrentRequests_shouldHandleCorrectly() { /* ... */ }
```

### 4. 性能测试

```kotlin
@Test
fun largeDataSet_shouldPerformWithinLimits() = runIntegrationTest {
    // Given
    val largeUserList = (1..1000).map { 
        TestDataBuilder.createTestUser("user_$it") 
    }
    
    // When
    val startTime = System.currentTimeMillis()
    largeUserList.forEach { database.userDao().insertUser(it) }
    val insertTime = System.currentTimeMillis() - startTime
    
    val queryStartTime = System.currentTimeMillis()
    val allUsers = database.userDao().getAllUsers()
    val queryTime = System.currentTimeMillis() - queryStartTime
    
    // Then
    assertThat(insertTime).isLessThan(1000) // Should complete within 1 second
    assertThat(queryTime).isLessThan(500)   // Should query within 0.5 seconds
    assertThat(allUsers).hasSize(1000)
}
```

### 5. 测试隔离

```kotlin
// ✅ 每个测试使用独立的数据库
@Before
fun setupIsolatedDatabase() {
    database = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
        .allowMainThreadQueries()
        .build()
}

// ✅ 清理测试状态
@After
fun cleanupTestState() {
    database.close()
    clearSharedPreferences()
    resetSingletons()
}
```

---

*集成测试确保系统各组件能够正确协作，是验证系统整体功能的重要手段。*