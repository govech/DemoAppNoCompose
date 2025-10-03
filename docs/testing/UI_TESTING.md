# UI 测试指南

本文档详细说明了 Android MVVM 框架中 UI 测试的编写规范和实施策略。

## 📋 目录

- [UI测试概述](#ui测试概述)
- [测试环境配置](#测试环境配置)
- [Espresso 测试](#espresso-测试)
- [UI Automator 测试](#ui-automator-测试)
- [Compose 测试](#compose-测试)
- [页面对象模式](#页面对象模式)
- [测试最佳实践](#测试最佳实践)

## 🎯 UI测试概述

### UI测试的目标

- **用户体验验证**: 确保用户界面按预期工作
- **交互流程测试**: 验证用户操作流程的正确性
- **视觉回归测试**: 防止UI变更破坏现有功能
- **可访问性测试**: 确保应用的可访问性

### 测试层次

```
┌─────────────────────────────────────────┐
│              端到端 UI 测试              │  ← 完整用户流程
├─────────────────────────────────────────┤
│              页面级 UI 测试              │  ← 单个页面功能
├─────────────────────────────────────────┤
│              组件级 UI 测试              │  ← 单个组件交互
├─────────────────────────────────────────┤
│              视图级 UI 测试              │  ← 单个视图元素
└─────────────────────────────────────────┘
```

## ⚙️ 测试环境配置

### 依赖配置

```kotlin
// build.gradle.kts (app module)
dependencies {
    // Espresso
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.0")
    androidTestImplementation("androidx.test.espresso:espresso-intents:3.5.0")
    androidTestImplementation("androidx.test.espresso:espresso-contrib:3.5.0")
    androidTestImplementation("androidx.test.espresso:espresso-accessibility:3.5.0")
    androidTestImplementation("androidx.test.espresso:espresso-web:3.5.0")
    
    // UI Automator
    androidTestImplementation("androidx.test.uiautomator:uiautomator:2.2.0")
    
    // Test Rules and Runners
    androidTestImplementation("androidx.test:runner:1.5.1")
    androidTestImplementation("androidx.test:rules:1.5.0")
    androidTestImplementation("androidx.test.ext:junit:1.1.4")
    
    // Compose Testing
    androidTestImplementation("androidx.compose.ui:ui-test-junit4:$compose_version")
    debugImplementation("androidx.compose.ui:ui-test-manifest:$compose_version")
    
    // Hilt Testing
    androidTestImplementation("com.google.dagger:hilt-android-testing:2.44")
    kaptAndroidTest("com.google.dagger:hilt-android-compiler:2.44")
    
    // Truth
    androidTestImplementation("com.google.truth:truth:1.1.3")
}

android {
    testOptions {
        animationsDisabled = true
    }
}
```

### 测试基类

```kotlin
@HiltAndroidTest
abstract class BaseUITest {
    
    @get:Rule
    var hiltRule = HiltAndroidRule(this)
    
    @get:Rule
    var activityRule = ActivityScenarioRule(MainActivity::class.java)
    
    @BeforeEach
    fun baseSetup() {
        hiltRule.inject()
        
        // 禁用动画
        disableAnimations()
        
        // 设置测试环境
        setupTestEnvironment()
    }
    
    @AfterEach
    fun baseTearDown() {
        // 清理测试数据
        cleanupTestData()
    }
    
    private fun disableAnimations() {
        InstrumentationRegistry.getInstrumentation().uiAutomation.executeShellCommand(
            "settings put global window_animation_scale 0"
        )
        InstrumentationRegistry.getInstrumentation().uiAutomation.executeShellCommand(
            "settings put global transition_animation_scale 0"
        )
        InstrumentationRegistry.getInstrumentation().uiAutomation.executeShellCommand(
            "settings put global animator_duration_scale 0"
        )
    }
    
    protected fun waitForView(viewMatcher: Matcher<View>, timeout: Long = 5000) {
        onView(isRoot()).perform(waitForMatch(viewMatcher, timeout))
    }
    
    protected fun waitForText(text: String, timeout: Long = 5000) {
        waitForView(withText(text), timeout)
    }
}
```

## ☕ Espresso 测试

### 基础 Espresso 测试

```kotlin
@HiltAndroidTest
@RunWith(AndroidJUnit4::class)
class LoginActivityTest : BaseUITest() {
    
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
        waitForView(withId(R.id.mainContainer))
        onView(withId(R.id.mainContainer))
            .check(matches(isDisplayed()))
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

### RecyclerView 测试

```kotlin
@HiltAndroidTest
class UserListActivityTest : BaseUITest() {
    
    @Test
    fun userList_shouldDisplayUsers() {
        // Given - Mock data is injected through Hilt
        
        // When
        onView(withId(R.id.rvUsers))
            .check(matches(isDisplayed()))
        
        // Then
        onView(withId(R.id.rvUsers))
            .check(matches(hasMinimumChildCount(1)))
        
        // Check first item
        onView(withRecyclerView(R.id.rvUsers).atPosition(0))
            .check(matches(hasDescendant(withText("Test User 1"))))
        
        onView(withRecyclerView(R.id.rvUsers).atPosition(0))
            .check(matches(hasDescendant(withText("test1@example.com"))))
    }
    
    @Test
    fun clickUserItem_shouldNavigateToUserDetail() {
        // When
        onView(withRecyclerView(R.id.rvUsers).atPosition(0))
            .perform(click())
        
        // Then
        waitForView(withId(R.id.userDetailContainer))
        onView(withId(R.id.userDetailContainer))
            .check(matches(isDisplayed()))
    }
    
    @Test
    fun swipeToRefresh_shouldRefreshUserList() {
        // When
        onView(withId(R.id.swipeRefreshLayout))
            .perform(swipeDown())
        
        // Then
        onView(withId(R.id.swipeRefreshLayout))
            .check(matches(isRefreshing()))
        
        // Wait for refresh to complete
        waitForView(not(isRefreshing()))
    }
    
    @Test
    fun scrollToBottom_shouldLoadMoreUsers() {
        // When
        onView(withId(R.id.rvUsers))
            .perform(scrollToPosition(19)) // Assuming 20 items per page
        
        // Then
        waitForView(withText("Loading more..."))
        
        // Wait for more items to load
        Thread.sleep(2000)
        
        onView(withId(R.id.rvUsers))
            .check(matches(hasMinimumChildCount(20)))
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
    
    fun isRefreshing(): Matcher<View> {
        return object : BoundedMatcher<View, SwipeRefreshLayout>(SwipeRefreshLayout::class.java) {
            
            override fun describeTo(description: Description) {
                description.appendText("is refreshing")
            }
            
            override fun matchesSafely(view: SwipeRefreshLayout): Boolean {
                return view.isRefreshing
            }
        }
    }
    
    fun hasMinimumChildCount(minCount: Int): Matcher<View> {
        return object : BoundedMatcher<View, RecyclerView>(RecyclerView::class.java) {
            
            override fun describeTo(description: Description) {
                description.appendText("has minimum child count: $minCount")
            }
            
            override fun matchesSafely(recyclerView: RecyclerView): Boolean {
                return recyclerView.adapter?.itemCount ?: 0 >= minCount
            }
        }
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

### Intent 测试

```kotlin
@HiltAndroidTest
class IntentTest : BaseUITest() {
    
    @get:Rule
    var intentsTestRule = IntentsTestRule(MainActivity::class.java)
    
    @Test
    fun clickShareButton_shouldLaunchShareIntent() {
        // Given
        val expectedText = "Check out this awesome app!"
        
        // When
        onView(withId(R.id.btnShare))
            .perform(click())
        
        // Then
        intended(allOf(
            hasAction(Intent.ACTION_SEND),
            hasType("text/plain"),
            hasExtra(Intent.EXTRA_TEXT, expectedText)
        ))
    }
    
    @Test
    fun clickEmailButton_shouldLaunchEmailIntent() {
        // When
        onView(withId(R.id.btnEmail))
            .perform(click())
        
        // Then
        intended(allOf(
            hasAction(Intent.ACTION_SENDTO),
            hasData("mailto:")
        ))
    }
    
    @Test
    fun clickPhoneButton_shouldLaunchDialerIntent() {
        // Given
        val phoneNumber = "1234567890"
        
        // When
        onView(withId(R.id.btnCall))
            .perform(click())
        
        // Then
        intended(allOf(
            hasAction(Intent.ACTION_DIAL),
            hasData("tel:$phoneNumber")
        ))
    }
}
```

## 🤖 UI Automator 测试

### 跨应用测试

```kotlin
@RunWith(AndroidJUnit4::class)
class CrossAppTest {
    
    private lateinit var device: UiDevice
    
    @Before
    fun setup() {
        device = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation())
        
        // 启动应用
        val context = ApplicationProvider.getApplicationContext<Context>()
        val intent = context.packageManager.getLaunchIntentForPackage(PACKAGE_NAME)
        intent?.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)
        context.startActivity(intent)
        
        // 等待应用启动
        device.wait(Until.hasObject(By.pkg(PACKAGE_NAME).depth(0)), LAUNCH_TIMEOUT)
    }
    
    @Test
    fun shareToExternalApp_shouldOpenShareDialog() {
        // Given
        val shareButton = device.findObject(UiSelector().resourceId("$PACKAGE_NAME:id/btnShare"))
        
        // When
        shareButton.click()
        
        // Then
        val shareDialog = device.wait(
            Until.hasObject(By.text("Share via")), 
            TIMEOUT
        )
        assertThat(shareDialog).isTrue()
        
        // Select an app from share dialog
        val gmailOption = device.findObject(UiSelector().text("Gmail"))
        if (gmailOption.exists()) {
            gmailOption.click()
            
            // Verify Gmail opened
            val gmailPackage = device.wait(
                Until.hasObject(By.pkg("com.google.android.gm")), 
                TIMEOUT
            )
            assertThat(gmailPackage).isTrue()
        }
    }
    
    @Test
    fun handleSystemPermissionDialog_shouldGrantPermission() {
        // Given
        val cameraButton = device.findObject(UiSelector().resourceId("$PACKAGE_NAME:id/btnCamera"))
        
        // When
        cameraButton.click()
        
        // Then - Handle permission dialog
        val allowButton = device.wait(
            Until.findObject(By.text("ALLOW")), 
            TIMEOUT
        )
        
        if (allowButton != null) {
            allowButton.click()
        }
        
        // Verify camera opened
        val cameraView = device.wait(
            Until.hasObject(By.resourceId("$PACKAGE_NAME:id/cameraView")), 
            TIMEOUT
        )
        assertThat(cameraView).isTrue()
    }
    
    @Test
    fun handleNetworkConnectivityChange_shouldShowOfflineMessage() {
        // Given - Disable WiFi
        device.executeShellCommand("svc wifi disable")
        device.executeShellCommand("svc data disable")
        
        // When
        val refreshButton = device.findObject(UiSelector().resourceId("$PACKAGE_NAME:id/btnRefresh"))
        refreshButton.click()
        
        // Then
        val offlineMessage = device.wait(
            Until.hasObject(By.text("No internet connection")), 
            TIMEOUT
        )
        assertThat(offlineMessage).isTrue()
        
        // Cleanup - Re-enable connectivity
        device.executeShellCommand("svc wifi enable")
        device.executeShellCommand("svc data enable")
    }
    
    companion object {
        private const val PACKAGE_NAME = "com.yourapp.demoappnocompose"
        private const val LAUNCH_TIMEOUT = 5000L
        private const val TIMEOUT = 3000L
    }
}
```

### 设备交互测试

```kotlin
@RunWith(AndroidJUnit4::class)
class DeviceInteractionTest {
    
    private lateinit var device: UiDevice
    
    @Before
    fun setup() {
        device = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation())
    }
    
    @Test
    fun rotateDevice_shouldMaintainState() {
        // Given
        val inputField = device.findObject(UiSelector().resourceId("$PACKAGE_NAME:id/etInput"))
        val testText = "Test input text"
        
        inputField.setText(testText)
        
        // When
        device.setOrientationLandscape()
        device.waitForIdle()
        
        // Then
        val inputFieldAfterRotation = device.findObject(UiSelector().resourceId("$PACKAGE_NAME:id/etInput"))
        assertThat(inputFieldAfterRotation.text).isEqualTo(testText)
        
        // Cleanup
        device.setOrientationNatural()
    }
    
    @Test
    fun pressBackButton_shouldNavigateBack() {
        // Given
        val detailButton = device.findObject(UiSelector().resourceId("$PACKAGE_NAME:id/btnDetail"))
        detailButton.click()
        
        // Verify detail screen opened
        device.wait(Until.hasObject(By.resourceId("$PACKAGE_NAME:id/detailContainer")), TIMEOUT)
        
        // When
        device.pressBack()
        
        // Then
        val mainContainer = device.wait(
            Until.hasObject(By.resourceId("$PACKAGE_NAME:id/mainContainer")), 
            TIMEOUT
        )
        assertThat(mainContainer).isTrue()
    }
    
    @Test
    fun pressHomeAndReturn_shouldRestoreState() {
        // Given
        val inputField = device.findObject(UiSelector().resourceId("$PACKAGE_NAME:id/etInput"))
        val testText = "Test state"
        
        inputField.setText(testText)
        
        // When
        device.pressHome()
        device.waitForIdle()
        
        // Return to app
        val context = ApplicationProvider.getApplicationContext<Context>()
        val intent = context.packageManager.getLaunchIntentForPackage(PACKAGE_NAME)
        context.startActivity(intent)
        
        device.wait(Until.hasObject(By.pkg(PACKAGE_NAME)), TIMEOUT)
        
        // Then
        val inputFieldAfterReturn = device.findObject(UiSelector().resourceId("$PACKAGE_NAME:id/etInput"))
        assertThat(inputFieldAfterReturn.text).isEqualTo(testText)
    }
    
    companion object {
        private const val PACKAGE_NAME = "com.yourapp.demoappnocompose"
        private const val TIMEOUT = 3000L
    }
}
```

## 🎨 Compose 测试

### Compose UI 测试

```kotlin
@HiltAndroidTest
class ComposeUITest {
    
    @get:Rule
    val composeTestRule = createAndroidComposeRule<MainActivity>()
    
    @Test
    fun loginScreen_withValidInput_shouldEnableLoginButton() {
        composeTestRule.setContent {
            LoginScreen(
                onLoginClick = { _, _ -> },
                onNavigateToRegister = { }
            )
        }
        
        // When
        composeTestRule.onNodeWithTag("email_field")
            .performTextInput("test@example.com")
        
        composeTestRule.onNodeWithTag("password_field")
            .performTextInput("password123")
        
        // Then
        composeTestRule.onNodeWithTag("login_button")
            .assertIsEnabled()
    }
    
    @Test
    fun userList_shouldDisplayUsers() {
        val testUsers = listOf(
            User("1", "user1@example.com", "User 1"),
            User("2", "user2@example.com", "User 2")
        )
        
        composeTestRule.setContent {
            UserListScreen(
                users = testUsers,
                onUserClick = { }
            )
        }
        
        // Then
        composeTestRule.onNodeWithText("User 1")
            .assertIsDisplayed()
        
        composeTestRule.onNodeWithText("user1@example.com")
            .assertIsDisplayed()
        
        composeTestRule.onNodeWithText("User 2")
            .assertIsDisplayed()
    }
    
    @Test
    fun userList_clickItem_shouldTriggerCallback() {
        var clickedUser: User? = null
        val testUsers = listOf(
            User("1", "user1@example.com", "User 1")
        )
        
        composeTestRule.setContent {
            UserListScreen(
                users = testUsers,
                onUserClick = { user -> clickedUser = user }
            )
        }
        
        // When
        composeTestRule.onNodeWithText("User 1")
            .performClick()
        
        // Then
        assertThat(clickedUser).isEqualTo(testUsers[0])
    }
    
    @Test
    fun loadingState_shouldShowProgressIndicator() {
        composeTestRule.setContent {
            UserListScreen(
                users = emptyList(),
                isLoading = true,
                onUserClick = { }
            )
        }
        
        // Then
        composeTestRule.onNodeWithTag("loading_indicator")
            .assertIsDisplayed()
    }
}
```

### Compose 语义测试

```kotlin
@HiltAndroidTest
class ComposeSemanticTest {
    
    @get:Rule
    val composeTestRule = createAndroidComposeRule<MainActivity>()
    
    @Test
    fun loginForm_shouldHaveProperSemantics() {
        composeTestRule.setContent {
            LoginScreen(
                onLoginClick = { _, _ -> },
                onNavigateToRegister = { }
            )
        }
        
        // Check content descriptions
        composeTestRule.onNodeWithContentDescription("Email input field")
            .assertIsDisplayed()
        
        composeTestRule.onNodeWithContentDescription("Password input field")
            .assertIsDisplayed()
        
        composeTestRule.onNodeWithContentDescription("Login button")
            .assertIsDisplayed()
    }
    
    @Test
    fun userList_shouldSupportAccessibility() {
        val testUsers = listOf(
            User("1", "user1@example.com", "User 1")
        )
        
        composeTestRule.setContent {
            UserListScreen(
                users = testUsers,
                onUserClick = { }
            )
        }
        
        // Check semantic properties
        composeTestRule.onNodeWithText("User 1")
            .assert(hasClickAction())
            .assert(hasContentDescription())
    }
}
```

## 📄 页面对象模式

### 页面对象实现

```kotlin
class LoginPageObject {
    
    fun enterEmail(email: String): LoginPageObject {
        onView(withId(R.id.etEmail))
            .perform(typeText(email), closeSoftKeyboard())
        return this
    }
    
    fun enterPassword(password: String): LoginPageObject {
        onView(withId(R.id.etPassword))
            .perform(typeText(password), closeSoftKeyboard())
        return this
    }
    
    fun clickLoginButton(): LoginPageObject {
        onView(withId(R.id.btnLogin))
            .perform(click())
        return this
    }
    
    fun clickRegisterLink(): RegisterPageObject {
        onView(withId(R.id.tvRegister))
            .perform(click())
        return RegisterPageObject()
    }
    
    fun verifyEmailError(expectedError: String): LoginPageObject {
        onView(withId(R.id.tilEmail))
            .check(matches(hasTextInputLayoutErrorText(expectedError)))
        return this
    }
    
    fun verifyPasswordError(expectedError: String): LoginPageObject {
        onView(withId(R.id.tilPassword))
            .check(matches(hasTextInputLayoutErrorText(expectedError)))
        return this
    }
    
    fun verifyLoadingState(): LoginPageObject {
        onView(withId(R.id.progressBar))
            .check(matches(isDisplayed()))
        
        onView(withId(R.id.btnLogin))
            .check(matches(not(isEnabled())))
        return this
    }
    
    fun waitForMainScreen(): MainPageObject {
        onView(withId(R.id.mainContainer))
            .check(matches(isDisplayed()))
        return MainPageObject()
    }
}

class MainPageObject {
    
    fun clickUserProfile(): UserProfilePageObject {
        onView(withId(R.id.btnProfile))
            .perform(click())
        return UserProfilePageObject()
    }
    
    fun clickSettings(): SettingsPageObject {
        onView(withId(R.id.btnSettings))
            .perform(click())
        return SettingsPageObject()
    }
    
    fun verifyWelcomeMessage(userName: String): MainPageObject {
        onView(withText("Welcome, $userName!"))
            .check(matches(isDisplayed()))
        return this
    }
}

class UserListPageObject {
    
    fun verifyUserDisplayed(userName: String): UserListPageObject {
        onView(withText(userName))
            .check(matches(isDisplayed()))
        return this
    }
    
    fun clickUser(position: Int): UserDetailPageObject {
        onView(withRecyclerView(R.id.rvUsers).atPosition(position))
            .perform(click())
        return UserDetailPageObject()
    }
    
    fun swipeToRefresh(): UserListPageObject {
        onView(withId(R.id.swipeRefreshLayout))
            .perform(swipeDown())
        return this
    }
    
    fun verifyRefreshing(): UserListPageObject {
        onView(withId(R.id.swipeRefreshLayout))
            .check(matches(isRefreshing()))
        return this
    }
    
    fun scrollToPosition(position: Int): UserListPageObject {
        onView(withId(R.id.rvUsers))
            .perform(scrollToPosition(position))
        return this
    }
}
```

### 使用页面对象的测试

```kotlin
@HiltAndroidTest
class LoginFlowTest : BaseUITest() {
    
    @Test
    fun loginFlow_withValidCredentials_shouldSucceed() {
        LoginPageObject()
            .enterEmail("test@example.com")
            .enterPassword("password123")
            .clickLoginButton()
            .waitForMainScreen()
            .verifyWelcomeMessage("Test User")
    }
    
    @Test
    fun loginFlow_withInvalidCredentials_shouldShowErrors() {
        LoginPageObject()
            .enterEmail("invalid-email")
            .enterPassword("123")
            .clickLoginButton()
            .verifyEmailError("Invalid email format")
            .verifyPasswordError("Password too short")
    }
    
    @Test
    fun userListFlow_shouldDisplayAndNavigate() {
        LoginPageObject()
            .enterEmail("test@example.com")
            .enterPassword("password123")
            .clickLoginButton()
            .waitForMainScreen()
            .clickUserProfile()
            .verifyUserDisplayed("Test User 1")
            .clickUser(0)
            .verifyUserDetailDisplayed()
    }
}
```

## 📊 测试最佳实践

### 1. 测试稳定性

```kotlin
// ✅ 使用显式等待
fun waitForElementToAppear(matcher: Matcher<View>, timeout: Long = 5000) {
    onView(isRoot()).perform(waitForMatch(matcher, timeout))
}

// ✅ 处理动画
@Before
fun disableAnimations() {
    UiAutomation.executeShellCommand("settings put global animator_duration_scale 0")
}

// ✅ 使用稳定的定位器
onView(withId(R.id.specificId))  // 好
onView(withText("Specific Text"))  // 可以
onView(withClassName(containsString("Button")))  // 避免
```

### 2. 测试数据管理

```kotlin
// ✅ 使用测试数据
@Before
fun setupTestData() {
    TestDataManager.insertTestUsers()
    TestDataManager.setupMockResponses()
}

@After
fun cleanupTestData() {
    TestDataManager.clearTestData()
}

// ✅ 隔离测试环境
@TestInstallIn(
    components = [SingletonComponent::class],
    replaces = [NetworkModule::class]
)
object TestNetworkModule {
    @Provides
    fun provideMockApiService(): ApiService = MockApiService()
}
```

### 3. 错误处理

```kotlin
// ✅ 优雅的错误处理
@Test
fun testWithErrorHandling() {
    try {
        onView(withId(R.id.button))
            .perform(click())
        
        waitForView(withId(R.id.result))
        
        onView(withId(R.id.result))
            .check(matches(withText("Success")))
            
    } catch (e: NoMatchingViewException) {
        // 提供有用的错误信息
        fail("Expected view not found. Current screen state: ${getCurrentScreenState()}")
    }
}

private fun getCurrentScreenState(): String {
    // 返回当前屏幕状态的描述
    return "Current activity: ${getCurrentActivity()::class.simpleName}"
}
```

### 4. 测试组织

```kotlin
// ✅ 按功能组织测试
class LoginFeatureTest : BaseUITest() {
    
    @Nested
    @DisplayName("Valid login scenarios")
    inner class ValidLoginScenarios {
        
        @Test
        fun `login with email and password`() { /* ... */ }
        
        @Test
        fun `login with social media`() { /* ... */ }
    }
    
    @Nested
    @DisplayName("Invalid login scenarios")
    inner class InvalidLoginScenarios {
        
        @Test
        fun `login with invalid email`() { /* ... */ }
        
        @Test
        fun `login with wrong password`() { /* ... */ }
    }
}
```

### 5. 性能考虑

```kotlin
// ✅ 优化测试执行时间
@Test
fun fastUITest() {
    // 使用最小必要的等待时间
    waitForView(withId(R.id.element), timeout = 1000)
    
    // 避免不必要的 Thread.sleep()
    // Thread.sleep(5000) // ❌
    
    // 使用条件等待
    waitUntil { isElementDisplayed(R.id.element) } // ✅
}

// ✅ 并行执行独立测试
@Execution(ExecutionMode.CONCURRENT)
class ParallelUITests {
    // 独立的测试可以并行执行
}
```

---

*UI测试是验证用户体验的重要手段，应该覆盖关键的用户交互流程和边界情况。*