# 单元测试指南

本文档详细说明了 Android MVVM 框架中单元测试的编写规范和最佳实践。

## 📋 目录

- [单元测试概述](#单元测试概述)
- [测试环境配置](#测试环境配置)
- [ViewModel 测试](#viewmodel-测试)
- [Repository 测试](#repository-测试)
- [UseCase 测试](#usecase-测试)
- [工具类测试](#工具类测试)
- [测试最佳实践](#测试最佳实践)

## 🎯 单元测试概述

### 单元测试的目标

- **验证逻辑正确性**: 确保业务逻辑按预期工作
- **提高代码质量**: 通过测试驱动开发提升代码质量
- **支持重构**: 为代码重构提供安全保障
- **文档作用**: 测试用例作为代码行为的活文档

### 测试原则

- **FIRST 原则**:
  - **Fast**: 快速执行
  - **Independent**: 独立运行
  - **Repeatable**: 可重复执行
  - **Self-Validating**: 自我验证
  - **Timely**: 及时编写

- **AAA 模式**:
  - **Arrange**: 准备测试数据和环境
  - **Act**: 执行被测试的方法
  - **Assert**: 验证结果

## ⚙️ 测试环境配置

### 依赖配置

```kotlin
// build.gradle.kts (app module)
dependencies {
    // JUnit 5
    testImplementation("org.junit.jupiter:junit-jupiter:5.8.2")
    testImplementation("org.junit.jupiter:junit-jupiter-params:5.8.2")
    
    // MockK
    testImplementation("io.mockk:mockk:1.13.2")
    testImplementation("io.mockk:mockk-android:1.13.2")
    
    // Truth (Google's assertion library)
    testImplementation("com.google.truth:truth:1.1.3")
    
    // Coroutines Testing
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.6.4")
    
    // Turbine (Flow testing)
    testImplementation("app.cash.turbine:turbine:0.12.1")
    
    // Robolectric (Android components testing)
    testImplementation("org.robolectric:robolectric:4.9")
}

tasks.withType<Test> {
    useJUnitPlatform()
}
```

### 测试基类

```kotlin
@ExtendWith(MockKExtension::class)
abstract class BaseUnitTest {
    
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()
    
    @BeforeEach
    fun baseSetup() {
        MockKAnnotations.init(this, relaxUnitFun = true)
        Dispatchers.setMain(StandardTestDispatcher())
    }
    
    @AfterEach
    fun baseTearDown() {
        Dispatchers.resetMain()
        clearAllMocks()
    }
    
    protected fun runTest(block: suspend TestScope.() -> Unit) = 
        kotlinx.coroutines.test.runTest(block = block)
}
```

## 🎭 ViewModel 测试

### 基础 ViewModel 测试

```kotlin
class LoginViewModelTest : BaseUnitTest() {
    
    @MockK
    private lateinit var userRepository: UserRepository
    
    @MockK
    private lateinit var validationUseCase: ValidationUseCase
    
    @MockK
    private lateinit var analyticsTracker: AnalyticsTracker
    
    private lateinit var viewModel: LoginViewModel
    
    @BeforeEach
    fun setup() {
        viewModel = LoginViewModel(
            userRepository = userRepository,
            validationUseCase = validationUseCase,
            analyticsTracker = analyticsTracker
        )
    }
    
    @Nested
    @DisplayName("Login functionality")
    inner class LoginFunctionality {
        
        @Test
        fun `GIVEN valid credentials WHEN login THEN should emit success state`() = runTest {
            // Given
            val email = "test@example.com"
            val password = "password123"
            val expectedUser = TestDataFactory.createUser()
            
            every { validationUseCase.validateEmail(email) } returns ValidationResult.Valid
            every { validationUseCase.validatePassword(password) } returns ValidationResult.Valid
            coEvery { userRepository.login(email, password) } returns Result.success(expectedUser)
            every { analyticsTracker.track(any()) } just Runs
            
            // When
            viewModel.login(email, password)
            
            // Then
            viewModel.uiState.test {
                val state = awaitItem()
                assertThat(state.isLoading).isFalse()
                assertThat(state.user).isEqualTo(expectedUser)
                assertThat(state.error).isNull()
            }
            
            coVerify { userRepository.login(email, password) }
            verify { analyticsTracker.track(LoginEvent.Success) }
        }
        
        @Test
        fun `GIVEN invalid email WHEN login THEN should emit validation error`() = runTest {
            // Given
            val invalidEmail = "invalid-email"
            val password = "password123"
            val validationError = ValidationResult.Invalid("Invalid email format")
            
            every { validationUseCase.validateEmail(invalidEmail) } returns validationError
            
            // When
            viewModel.login(invalidEmail, password)
            
            // Then
            viewModel.uiState.test {
                val state = awaitItem()
                assertThat(state.isLoading).isFalse()
                assertThat(state.emailError).isEqualTo("Invalid email format")
                assertThat(state.user).isNull()
            }
            
            coVerify(exactly = 0) { userRepository.login(any(), any()) }
        }
        
        @Test
        fun `GIVEN network error WHEN login THEN should emit error state`() = runTest {
            // Given
            val email = "test@example.com"
            val password = "password123"
            val networkError = NetworkException("Network error")
            
            every { validationUseCase.validateEmail(email) } returns ValidationResult.Valid
            every { validationUseCase.validatePassword(password) } returns ValidationResult.Valid
            coEvery { userRepository.login(email, password) } returns Result.failure(networkError)
            every { analyticsTracker.track(any()) } just Runs
            
            // When
            viewModel.login(email, password)
            
            // Then
            viewModel.uiState.test {
                val state = awaitItem()
                assertThat(state.isLoading).isFalse()
                assertThat(state.error).isEqualTo("Network error")
                assertThat(state.user).isNull()
            }
            
            verify { analyticsTracker.track(LoginEvent.Failure("Network error")) }
        }
    }
    
    @Nested
    @DisplayName("Loading state management")
    inner class LoadingStateManagement {
        
        @Test
        fun `WHEN login starts THEN should emit loading state`() = runTest {
            // Given
            val email = "test@example.com"
            val password = "password123"
            val loginDeferred = CompletableDeferred<Result<User>>()
            
            every { validationUseCase.validateEmail(email) } returns ValidationResult.Valid
            every { validationUseCase.validatePassword(password) } returns ValidationResult.Valid
            coEvery { userRepository.login(email, password) } returns loginDeferred.await()
            
            // When
            val job = launch { viewModel.login(email, password) }
            
            // Then
            viewModel.uiState.test {
                val loadingState = awaitItem()
                assertThat(loadingState.isLoading).isTrue()
                
                // Complete the login
                loginDeferred.complete(Result.success(TestDataFactory.createUser()))
                job.join()
                
                val completedState = awaitItem()
                assertThat(completedState.isLoading).isFalse()
            }
        }
    }
    
    @Nested
    @DisplayName("Input validation")
    inner class InputValidation {
        
        @ParameterizedTest
        @ValueSource(strings = ["", " ", "invalid-email", "@example.com", "test@"])
        fun `GIVEN invalid email WHEN validate THEN should return error`(invalidEmail: String) {
            // Given
            every { validationUseCase.validateEmail(invalidEmail) } returns 
                ValidationResult.Invalid("Invalid email format")
            
            // When
            viewModel.validateEmail(invalidEmail)
            
            // Then
            viewModel.uiState.test {
                val state = awaitItem()
                assertThat(state.emailError).isEqualTo("Invalid email format")
            }
        }
        
        @ParameterizedTest
        @ValueSource(strings = ["123", "password", "PASSWORD", "Pass123"])
        fun `GIVEN weak password WHEN validate THEN should return error`(weakPassword: String) {
            // Given
            every { validationUseCase.validatePassword(weakPassword) } returns 
                ValidationResult.Invalid("Password too weak")
            
            // When
            viewModel.validatePassword(weakPassword)
            
            // Then
            viewModel.uiState.test {
                val state = awaitItem()
                assertThat(state.passwordError).isEqualTo("Password too weak")
            }
        }
    }
}
```

### StateFlow 测试

```kotlin
class UserProfileViewModelTest : BaseUnitTest() {
    
    @MockK
    private lateinit var userRepository: UserRepository
    
    private lateinit var viewModel: UserProfileViewModel
    
    @BeforeEach
    fun setup() {
        viewModel = UserProfileViewModel(userRepository)
    }
    
    @Test
    fun `WHEN loadUserProfile THEN should emit loading then success states`() = runTest {
        // Given
        val expectedUser = TestDataFactory.createUser()
        coEvery { userRepository.getCurrentUser() } returns expectedUser
        
        // When & Then
        viewModel.uiState.test {
            // Initial state
            val initialState = awaitItem()
            assertThat(initialState.isLoading).isFalse()
            assertThat(initialState.user).isNull()
            
            // Start loading
            viewModel.loadUserProfile()
            
            // Loading state
            val loadingState = awaitItem()
            assertThat(loadingState.isLoading).isTrue()
            assertThat(loadingState.user).isNull()
            
            // Success state
            val successState = awaitItem()
            assertThat(successState.isLoading).isFalse()
            assertThat(successState.user).isEqualTo(expectedUser)
            assertThat(successState.error).isNull()
        }
    }
    
    @Test
    fun `WHEN updateProfile THEN should emit updated user`() = runTest {
        // Given
        val originalUser = TestDataFactory.createUser(name = "Original Name")
        val updatedUser = originalUser.copy(name = "Updated Name")
        
        coEvery { userRepository.getCurrentUser() } returns originalUser
        coEvery { userRepository.updateUser(any()) } returns Result.success(updatedUser)
        
        // When
        viewModel.loadUserProfile()
        viewModel.updateProfile("Updated Name")
        
        // Then
        viewModel.uiState.test {
            skipItems(2) // Skip initial and loading states
            
            val successState = awaitItem()
            assertThat(successState.user?.name).isEqualTo("Updated Name")
        }
    }
}
```

## 🗄️ Repository 测试

### Repository 实现测试

```kotlin
class UserRepositoryImplTest : BaseUnitTest() {
    
    @MockK
    private lateinit var apiService: ApiService
    
    @MockK
    private lateinit var userDao: UserDao
    
    @MockK
    private lateinit var secureDataStore: SecureDataStore
    
    @MockK
    private lateinit var networkMonitor: NetworkMonitor
    
    private lateinit var repository: UserRepositoryImpl
    
    @BeforeEach
    fun setup() {
        repository = UserRepositoryImpl(
            apiService = apiService,
            userDao = userDao,
            secureDataStore = secureDataStore,
            networkMonitor = networkMonitor
        )
    }
    
    @Nested
    @DisplayName("Login functionality")
    inner class LoginFunctionality {
        
        @Test
        fun `GIVEN valid credentials WHEN login THEN should save user and token`() = runTest {
            // Given
            val email = "test@example.com"
            val password = "password123"
            val loginResponse = TestDataFactory.createLoginResponse()
            val expectedUser = TestDataFactory.createUser()
            
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
            verify { secureDataStore.saveToken(loginResponse.token) }
        }
        
        @Test
        fun `GIVEN API error WHEN login THEN should return failure`() = runTest {
            // Given
            val email = "test@example.com"
            val password = "wrong_password"
            val apiException = HttpException(Response.error<Any>(401, "".toResponseBody()))
            
            coEvery { apiService.login(any()) } throws apiException
            
            // When
            val result = repository.login(email, password)
            
            // Then
            assertThat(result.isFailure).isTrue()
            assertThat(result.exceptionOrNull()).isInstanceOf(HttpException::class.java)
            
            coVerify(exactly = 0) { userDao.insertUser(any()) }
            verify(exactly = 0) { secureDataStore.saveToken(any()) }
        }
    }
    
    @Nested
    @DisplayName("User data retrieval")
    inner class UserDataRetrieval {
        
        @Test
        fun `GIVEN cached user exists WHEN getCurrentUser THEN should return cached user`() = runTest {
            // Given
            val cachedUser = TestDataFactory.createUserEntity()
            val expectedUser = TestDataFactory.createUser()
            
            coEvery { userDao.getCurrentUser() } returns cachedUser
            
            // When
            val result = repository.getCurrentUser()
            
            // Then
            assertThat(result).isEqualTo(expectedUser)
            coVerify(exactly = 0) { apiService.getCurrentUser() }
        }
        
        @Test
        fun `GIVEN no cached user AND network available WHEN getCurrentUser THEN should fetch from API`() = runTest {
            // Given
            val apiUser = TestDataFactory.createUserDto()
            val expectedUser = TestDataFactory.createUser()
            
            coEvery { userDao.getCurrentUser() } returns null
            every { networkMonitor.isConnected } returns true
            coEvery { apiService.getCurrentUser() } returns apiUser
            coEvery { userDao.insertUser(any()) } just Runs
            
            // When
            val result = repository.getCurrentUser()
            
            // Then
            assertThat(result).isEqualTo(expectedUser)
            coVerify { apiService.getCurrentUser() }
            coVerify { userDao.insertUser(expectedUser.toEntity()) }
        }
        
        @Test
        fun `GIVEN no cached user AND no network WHEN getCurrentUser THEN should return null`() = runTest {
            // Given
            coEvery { userDao.getCurrentUser() } returns null
            every { networkMonitor.isConnected } returns false
            
            // When
            val result = repository.getCurrentUser()
            
            // Then
            assertThat(result).isNull()
            coVerify(exactly = 0) { apiService.getCurrentUser() }
        }
    }
    
    @Nested
    @DisplayName("Data synchronization")
    inner class DataSynchronization {
        
        @Test
        fun `WHEN syncUserData THEN should update local cache with remote data`() = runTest {
            // Given
            val remoteUser = TestDataFactory.createUserDto(name = "Updated Name")
            val expectedUser = TestDataFactory.createUser(name = "Updated Name")
            
            coEvery { apiService.getCurrentUser() } returns remoteUser
            coEvery { userDao.insertUser(any()) } just Runs
            
            // When
            repository.syncUserData()
            
            // Then
            coVerify { apiService.getCurrentUser() }
            coVerify { userDao.insertUser(expectedUser.toEntity()) }
        }
        
        @Test
        fun `GIVEN sync error WHEN syncUserData THEN should not update cache`() = runTest {
            // Given
            val syncException = NetworkException("Sync failed")
            coEvery { apiService.getCurrentUser() } throws syncException
            
            // When & Then
            assertThrows<NetworkException> {
                runBlocking { repository.syncUserData() }
            }
            
            coVerify(exactly = 0) { userDao.insertUser(any()) }
        }
    }
}
```

### Repository 缓存策略测试

```kotlin
class CacheStrategyTest : BaseUnitTest() {
    
    @MockK
    private lateinit var apiService: ApiService
    
    @MockK
    private lateinit var cacheDao: CacheDao
    
    private lateinit var repository: CachedRepository
    
    @BeforeEach
    fun setup() {
        repository = CachedRepository(apiService, cacheDao)
    }
    
    @Test
    fun `GIVEN fresh cache WHEN getData THEN should return cached data`() = runTest {
        // Given
        val cachedData = TestDataFactory.createCacheEntity(
            timestamp = System.currentTimeMillis() - TimeUnit.MINUTES.toMillis(5) // 5 minutes ago
        )
        val expectedData = TestDataFactory.createData()
        
        coEvery { cacheDao.getCachedData(any()) } returns cachedData
        
        // When
        val result = repository.getData("key")
        
        // Then
        assertThat(result).isEqualTo(expectedData)
        coVerify(exactly = 0) { apiService.getData(any()) }
    }
    
    @Test
    fun `GIVEN stale cache WHEN getData THEN should fetch from API and update cache`() = runTest {
        // Given
        val staleCache = TestDataFactory.createCacheEntity(
            timestamp = System.currentTimeMillis() - TimeUnit.HOURS.toMillis(2) // 2 hours ago
        )
        val freshData = TestDataFactory.createDataDto()
        val expectedData = TestDataFactory.createData()
        
        coEvery { cacheDao.getCachedData(any()) } returns staleCache
        coEvery { apiService.getData(any()) } returns freshData
        coEvery { cacheDao.insertCachedData(any()) } just Runs
        
        // When
        val result = repository.getData("key")
        
        // Then
        assertThat(result).isEqualTo(expectedData)
        coVerify { apiService.getData("key") }
        coVerify { cacheDao.insertCachedData(any()) }
    }
}
```

## 🎯 UseCase 测试

### 业务逻辑 UseCase 测试

```kotlin
class LoginUseCaseTest : BaseUnitTest() {
    
    @MockK
    private lateinit var userRepository: UserRepository
    
    @MockK
    private lateinit var validationUseCase: ValidationUseCase
    
    @MockK
    private lateinit var analyticsTracker: AnalyticsTracker
    
    private lateinit var loginUseCase: LoginUseCase
    
    @BeforeEach
    fun setup() {
        loginUseCase = LoginUseCase(
            userRepository = userRepository,
            validationUseCase = validationUseCase,
            analyticsTracker = analyticsTracker
        )
    }
    
    @Test
    fun `GIVEN valid credentials WHEN execute THEN should return success result`() = runTest {
        // Given
        val request = LoginRequest("test@example.com", "password123")
        val expectedUser = TestDataFactory.createUser()
        
        every { validationUseCase.validateEmail(request.email) } returns ValidationResult.Valid
        every { validationUseCase.validatePassword(request.password) } returns ValidationResult.Valid
        coEvery { userRepository.login(request.email, request.password) } returns Result.success(expectedUser)
        every { analyticsTracker.track(any()) } just Runs
        
        // When
        val result = loginUseCase.execute(request)
        
        // Then
        assertThat(result.isSuccess).isTrue()
        assertThat(result.getOrNull()).isEqualTo(expectedUser)
        
        verify { analyticsTracker.track(LoginEvent.Attempt) }
        verify { analyticsTracker.track(LoginEvent.Success) }
    }
    
    @Test
    fun `GIVEN invalid email WHEN execute THEN should return validation error`() = runTest {
        // Given
        val request = LoginRequest("invalid-email", "password123")
        val validationError = ValidationResult.Invalid("Invalid email format")
        
        every { validationUseCase.validateEmail(request.email) } returns validationError
        every { analyticsTracker.track(any()) } just Runs
        
        // When
        val result = loginUseCase.execute(request)
        
        // Then
        assertThat(result.isFailure).isTrue()
        assertThat(result.exceptionOrNull()).isInstanceOf(ValidationException::class.java)
        
        verify { analyticsTracker.track(LoginEvent.Attempt) }
        verify { analyticsTracker.track(LoginEvent.ValidationError("Invalid email format")) }
        coVerify(exactly = 0) { userRepository.login(any(), any()) }
    }
}
```

### 复杂业务逻辑测试

```kotlin
class OrderProcessingUseCaseTest : BaseUnitTest() {
    
    @MockK
    private lateinit var orderRepository: OrderRepository
    
    @MockK
    private lateinit var paymentService: PaymentService
    
    @MockK
    private lateinit var inventoryService: InventoryService
    
    @MockK
    private lateinit var notificationService: NotificationService
    
    private lateinit var orderProcessingUseCase: OrderProcessingUseCase
    
    @BeforeEach
    fun setup() {
        orderProcessingUseCase = OrderProcessingUseCase(
            orderRepository = orderRepository,
            paymentService = paymentService,
            inventoryService = inventoryService,
            notificationService = notificationService
        )
    }
    
    @Test
    fun `GIVEN valid order WHEN processOrder THEN should complete successfully`() = runTest {
        // Given
        val order = TestDataFactory.createOrder()
        val paymentResult = PaymentResult.Success("payment_id")
        
        coEvery { inventoryService.checkAvailability(order.items) } returns true
        coEvery { inventoryService.reserveItems(order.items) } returns true
        coEvery { paymentService.processPayment(order.payment) } returns paymentResult
        coEvery { orderRepository.saveOrder(any()) } returns order.copy(status = OrderStatus.COMPLETED)
        coEvery { notificationService.sendOrderConfirmation(any()) } just Runs
        
        // When
        val result = orderProcessingUseCase.processOrder(order)
        
        // Then
        assertThat(result.isSuccess).isTrue()
        
        val processedOrder = result.getOrNull()
        assertThat(processedOrder?.status).isEqualTo(OrderStatus.COMPLETED)
        
        coVerifyOrder {
            inventoryService.checkAvailability(order.items)
            inventoryService.reserveItems(order.items)
            paymentService.processPayment(order.payment)
            orderRepository.saveOrder(any())
            notificationService.sendOrderConfirmation(any())
        }
    }
    
    @Test
    fun `GIVEN insufficient inventory WHEN processOrder THEN should fail with inventory error`() = runTest {
        // Given
        val order = TestDataFactory.createOrder()
        
        coEvery { inventoryService.checkAvailability(order.items) } returns false
        
        // When
        val result = orderProcessingUseCase.processOrder(order)
        
        // Then
        assertThat(result.isFailure).isTrue()
        assertThat(result.exceptionOrNull()).isInstanceOf(InsufficientInventoryException::class.java)
        
        coVerify { inventoryService.checkAvailability(order.items) }
        coVerify(exactly = 0) { paymentService.processPayment(any()) }
        coVerify(exactly = 0) { orderRepository.saveOrder(any()) }
    }
    
    @Test
    fun `GIVEN payment failure WHEN processOrder THEN should rollback inventory reservation`() = runTest {
        // Given
        val order = TestDataFactory.createOrder()
        val paymentError = PaymentResult.Failure("Payment failed")
        
        coEvery { inventoryService.checkAvailability(order.items) } returns true
        coEvery { inventoryService.reserveItems(order.items) } returns true
        coEvery { paymentService.processPayment(order.payment) } returns paymentError
        coEvery { inventoryService.releaseReservation(order.items) } just Runs
        
        // When
        val result = orderProcessingUseCase.processOrder(order)
        
        // Then
        assertThat(result.isFailure).isTrue()
        assertThat(result.exceptionOrNull()).isInstanceOf(PaymentException::class.java)
        
        coVerify { inventoryService.releaseReservation(order.items) }
        coVerify(exactly = 0) { orderRepository.saveOrder(any()) }
    }
}
```

## 🛠️ 工具类测试

### 工具类单元测试

```kotlin
class EncryptUtilTest {
    
    @Nested
    @DisplayName("AES Encryption")
    inner class AESEncryption {
        
        @Test
        fun `GIVEN plain text WHEN encrypt and decrypt THEN should return original text`() {
            // Given
            val plainText = "Hello, World!"
            val key = "MySecretKey12345"
            
            // When
            val encrypted = EncryptUtil.aesEncrypt(plainText, key)
            val decrypted = EncryptUtil.aesDecrypt(encrypted, key)
            
            // Then
            assertThat(decrypted).isEqualTo(plainText)
            assertThat(encrypted).isNotEqualTo(plainText)
        }
        
        @Test
        fun `GIVEN empty string WHEN encrypt THEN should handle gracefully`() {
            // Given
            val plainText = ""
            val key = "MySecretKey12345"
            
            // When
            val encrypted = EncryptUtil.aesEncrypt(plainText, key)
            val decrypted = EncryptUtil.aesDecrypt(encrypted, key)
            
            // Then
            assertThat(decrypted).isEqualTo(plainText)
        }
        
        @Test
        fun `GIVEN wrong key WHEN decrypt THEN should throw exception`() {
            // Given
            val plainText = "Hello, World!"
            val correctKey = "MySecretKey12345"
            val wrongKey = "WrongKey12345678"
            
            val encrypted = EncryptUtil.aesEncrypt(plainText, correctKey)
            
            // When & Then
            assertThrows<DecryptionException> {
                EncryptUtil.aesDecrypt(encrypted, wrongKey)
            }
        }
    }
    
    @Nested
    @DisplayName("Hash Functions")
    inner class HashFunctions {
        
        @ParameterizedTest
        @ValueSource(strings = ["hello", "world", "test123", ""])
        fun `GIVEN input string WHEN sha256 THEN should return consistent hash`(input: String) {
            // When
            val hash1 = EncryptUtil.sha256(input)
            val hash2 = EncryptUtil.sha256(input)
            
            // Then
            assertThat(hash1).isEqualTo(hash2)
            assertThat(hash1).hasLength(64) // SHA-256 produces 64-character hex string
        }
        
        @Test
        fun `GIVEN different inputs WHEN sha256 THEN should return different hashes`() {
            // Given
            val input1 = "hello"
            val input2 = "world"
            
            // When
            val hash1 = EncryptUtil.sha256(input1)
            val hash2 = EncryptUtil.sha256(input2)
            
            // Then
            assertThat(hash1).isNotEqualTo(hash2)
        }
    }
}
```

### 日期工具类测试

```kotlin
class DateUtilTest {
    
    @Nested
    @DisplayName("Date formatting")
    inner class DateFormatting {
        
        @Test
        fun `GIVEN timestamp WHEN formatDate THEN should return formatted string`() {
            // Given
            val timestamp = 1640995200000L // 2022-01-01 00:00:00 UTC
            val expectedFormat = "2022-01-01"
            
            // When
            val formatted = DateUtil.formatDate(timestamp, "yyyy-MM-dd")
            
            // Then
            assertThat(formatted).isEqualTo(expectedFormat)
        }
        
        @Test
        fun `GIVEN invalid timestamp WHEN formatDate THEN should handle gracefully`() {
            // Given
            val invalidTimestamp = -1L
            
            // When & Then
            assertThrows<IllegalArgumentException> {
                DateUtil.formatDate(invalidTimestamp, "yyyy-MM-dd")
            }
        }
    }
    
    @Nested
    @DisplayName("Date calculations")
    inner class DateCalculations {
        
        @Test
        fun `GIVEN two dates WHEN calculateDaysBetween THEN should return correct difference`() {
            // Given
            val date1 = Calendar.getInstance().apply {
                set(2022, Calendar.JANUARY, 1)
            }.timeInMillis
            
            val date2 = Calendar.getInstance().apply {
                set(2022, Calendar.JANUARY, 10)
            }.timeInMillis
            
            // When
            val daysBetween = DateUtil.calculateDaysBetween(date1, date2)
            
            // Then
            assertThat(daysBetween).isEqualTo(9)
        }
        
        @Test
        fun `GIVEN same date WHEN calculateDaysBetween THEN should return zero`() {
            // Given
            val date = System.currentTimeMillis()
            
            // When
            val daysBetween = DateUtil.calculateDaysBetween(date, date)
            
            // Then
            assertThat(daysBetween).isEqualTo(0)
        }
    }
}
```

## 📊 测试最佳实践

### 1. 测试命名规范

```kotlin
// ✅ 好的测试命名
@Test
fun `GIVEN invalid email WHEN validateEmail THEN should return validation error`()

@Test
fun `WHEN user logs in with correct credentials THEN should return success`()

@Test
fun `GIVEN network error WHEN fetchUserData THEN should emit error state`()

// ❌ 不好的测试命名
@Test
fun testLogin()

@Test
fun validateEmailTest()

@Test
fun shouldReturnError()
```

### 2. 测试数据管理

```kotlin
// 使用测试数据工厂
object TestDataFactory {
    
    fun createUser(
        id: String = "test_id",
        email: String = "test@example.com",
        name: String = "Test User"
    ): User = User(id, email, name)
    
    fun createLoginRequest(
        email: String = "test@example.com",
        password: String = "password123"
    ): LoginRequest = LoginRequest(email, password)
}

// 使用 Builder 模式
class UserTestBuilder {
    private var id = "test_id"
    private var email = "test@example.com"
    private var name = "Test User"
    
    fun withId(id: String) = apply { this.id = id }
    fun withEmail(email: String) = apply { this.email = email }
    fun withName(name: String) = apply { this.name = name }
    
    fun build() = User(id, email, name)
}
```

### 3. Mock 使用规范

```kotlin
class ExampleTest : BaseUnitTest() {
    
    @MockK
    private lateinit var repository: Repository
    
    @Test
    fun `example test with proper mocking`() = runTest {
        // ✅ 明确的 Mock 行为定义
        coEvery { repository.getData(any()) } returns TestData()
        every { repository.isDataValid(any()) } returns true
        
        // ✅ 验证 Mock 调用
        coVerify { repository.getData("expected_param") }
        verify(exactly = 1) { repository.isDataValid(any()) }
        
        // ✅ 验证没有其他调用
        confirmVerified(repository)
    }
    
    @Test
    fun `example test with relaxed mock`() = runTest {
        // ✅ 对于不关心返回值的方法使用 relaxed mock
        val relaxedMock = mockk<SomeService>(relaxed = true)
        
        // 只 mock 关心的方法
        every { relaxedMock.importantMethod() } returns "expected_result"
    }
}
```

### 4. 异步测试

```kotlin
class AsyncTestExample : BaseUnitTest() {
    
    @Test
    fun `test coroutine with runTest`() = runTest {
        // ✅ 使用 runTest 测试协程
        val result = async { someAsyncOperation() }
        val value = result.await()
        
        assertThat(value).isEqualTo("expected")
    }
    
    @Test
    fun `test flow with turbine`() = runTest {
        // ✅ 使用 Turbine 测试 Flow
        flowOf(1, 2, 3).test {
            assertThat(awaitItem()).isEqualTo(1)
            assertThat(awaitItem()).isEqualTo(2)
            assertThat(awaitItem()).isEqualTo(3)
            awaitComplete()
        }
    }
    
    @Test
    fun `test StateFlow changes`() = runTest {
        // ✅ 测试 StateFlow 状态变化
        val stateFlow = MutableStateFlow("initial")
        
        stateFlow.test {
            assertThat(awaitItem()).isEqualTo("initial")
            
            stateFlow.value = "updated"
            assertThat(awaitItem()).isEqualTo("updated")
        }
    }
}
```

### 5. 测试覆盖率

```kotlin
// ✅ 覆盖所有分支
@Test
fun `test all branches of conditional logic`() {
    // Test true branch
    val result1 = calculator.divide(10, 2)
    assertThat(result1).isEqualTo(5.0)
    
    // Test false branch (division by zero)
    assertThrows<ArithmeticException> {
        calculator.divide(10, 0)
    }
}

// ✅ 测试边界条件
@ParameterizedTest
@ValueSource(ints = [0, 1, -1, Int.MAX_VALUE, Int.MIN_VALUE])
fun `test boundary conditions`(input: Int) {
    val result = processor.process(input)
    assertThat(result).isNotNull()
}
```

### 6. 测试组织

```kotlin
class WellOrganizedTest : BaseUnitTest() {
    
    @Nested
    @DisplayName("User authentication")
    inner class UserAuthentication {
        
        @Test
        fun `successful login`() { /* ... */ }
        
        @Test
        fun `failed login with wrong password`() { /* ... */ }
        
        @Test
        fun `failed login with invalid email`() { /* ... */ }
    }
    
    @Nested
    @DisplayName("User profile management")
    inner class UserProfileManagement {
        
        @Test
        fun `update profile successfully`() { /* ... */ }
        
        @Test
        fun `update profile with validation error`() { /* ... */ }
    }
}
```

---

*单元测试是保证代码质量的基础，应该覆盖所有重要的业务逻辑和边界条件。*