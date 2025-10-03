# 安全指南

本文档详细说明了 Android MVVM 框架中的安全最佳实践和防护措施。

## 📋 目录

- [安全概述](#安全概述)
- [网络安全](#网络安全)
- [数据安全](#数据安全)
- [代码安全](#代码安全)
- [权限管理](#权限管理)
- [安全检测](#安全检测)

## 🛡️ 安全概述

### 安全原则

- **最小权限原则**: 只申请必要的权限
- **数据加密**: 敏感数据必须加密存储和传输
- **输入验证**: 严格验证所有用户输入
- **安全通信**: 使用 HTTPS 和证书固定
- **代码混淆**: 保护代码不被逆向工程

### 安全威胁模型

```
┌─────────────────┐    ┌─────────────────┐    ┌─────────────────┐
│   网络攻击       │    │   本地攻击       │    │   代码攻击       │
│                 │    │                 │    │                 │
│ 中间人攻击       │    │ 数据窃取         │    │ 逆向工程         │
│ 网络窃听         │    │ Root 设备       │    │ 代码注入         │
│ 伪造服务器       │    │ 恶意应用         │    │ 动态调试         │
└─────────────────┘    └─────────────────┘    └─────────────────┘
```

## 🌐 网络安全

### HTTPS 强制使用

#### 网络安全配置

```xml
<!-- res/xml/network_security_config.xml -->
<?xml version="1.0" encoding="utf-8"?>
<network-security-config>
    <domain-config cleartextTrafficPermitted="false">
        <domain includeSubdomains="true">api.yourapp.com</domain>
        <pin-set expiration="2025-12-31">
            <pin digest="SHA-256">AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA=</pin>
            <pin digest="SHA-256">BBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBB=</pin>
        </pin-set>
    </domain-config>
    
    <!-- 开发环境配置 -->
    <domain-config cleartextTrafficPermitted="true">
        <domain includeSubdomains="true">localhost</domain>
        <domain includeSubdomains="true">10.0.2.2</domain>
    </domain-config>
    
    <!-- 禁用用户添加的 CA -->
    <base-config cleartextTrafficPermitted="false">
        <trust-anchors>
            <certificates src="system"/>
        </trust-anchors>
    </base-config>
</network-security-config>
```

```xml
<!-- AndroidManifest.xml -->
<application
    android:networkSecurityConfig="@xml/network_security_config"
    ... >
</application>
```

### 证书固定 (Certificate Pinning)

```kotlin
@Module
@InstallIn(SingletonComponent::class)
object NetworkSecurityModule {
    
    @Provides
    @Singleton
    fun provideCertificatePinner(): CertificatePinner {
        return CertificatePinner.Builder()
            .add("api.yourapp.com", "sha256/AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA=")
            .add("api.yourapp.com", "sha256/BBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBB=")
            .build()
    }
    
    @Provides
    @Singleton
    fun provideSecureOkHttpClient(
        certificatePinner: CertificatePinner
    ): OkHttpClient {
        return OkHttpClient.Builder()
            .certificatePinner(certificatePinner)
            .addInterceptor(SecurityInterceptor())
            .protocols(listOf(Protocol.HTTP_2, Protocol.HTTP_1_1))
            .build()
    }
}
```

### 请求安全拦截器

```kotlin
class SecurityInterceptor : Interceptor {
    
    override fun intercept(chain: Interceptor.Chain): Response {
        val originalRequest = chain.request()
        
        // 添加安全头
        val secureRequest = originalRequest.newBuilder()
            .addHeader("X-Requested-With", "XMLHttpRequest")
            .addHeader("X-App-Version", BuildConfig.VERSION_NAME)
            .addHeader("X-Platform", "Android")
            .addHeader("X-Device-ID", getDeviceId())
            .build()
        
        val response = chain.proceed(secureRequest)
        
        // 验证响应安全性
        validateResponse(response)
        
        return response
    }
    
    private fun validateResponse(response: Response) {
        // 检查响应头
        val contentType = response.header("Content-Type")
        if (contentType?.contains("application/json") != true) {
            Logger.w("Unexpected content type: $contentType")
        }
        
        // 检查安全头
        val xFrameOptions = response.header("X-Frame-Options")
        if (xFrameOptions.isNullOrEmpty()) {
            Logger.w("Missing X-Frame-Options header")
        }
    }
    
    private fun getDeviceId(): String {
        // 生成设备唯一标识（不使用敏感信息）
        return UUID.randomUUID().toString()
    }
}
```

### API 请求签名

```kotlin
class ApiSignatureInterceptor @Inject constructor(
    private val appConfig: AppConfig
) : Interceptor {
    
    override fun intercept(chain: Interceptor.Chain): Response {
        val originalRequest = chain.request()
        
        // 生成时间戳和随机数
        val timestamp = System.currentTimeMillis().toString()
        val nonce = UUID.randomUUID().toString()
        
        // 生成签名
        val signature = generateSignature(originalRequest, timestamp, nonce)
        
        // 添加签名头
        val signedRequest = originalRequest.newBuilder()
            .addHeader("X-Timestamp", timestamp)
            .addHeader("X-Nonce", nonce)
            .addHeader("X-Signature", signature)
            .build()
        
        return chain.proceed(signedRequest)
    }
    
    private fun generateSignature(
        request: Request, 
        timestamp: String, 
        nonce: String
    ): String {
        val method = request.method
        val url = request.url.toString()
        val body = request.body?.let { bodyToString(it) } ?: ""
        
        // 构建签名字符串
        val signString = "$method\n$url\n$body\n$timestamp\n$nonce"
        
        // 使用 HMAC-SHA256 生成签名
        return EncryptUtil.hmacSha256(signString, appConfig.apiSecret)
    }
    
    private fun bodyToString(body: RequestBody): String {
        return try {
            val buffer = Buffer()
            body.writeTo(buffer)
            buffer.readUtf8()
        } catch (e: Exception) {
            ""
        }
    }
}
```

## 🔐 数据安全

### 敏感数据加密存储

```kotlin
@Singleton
class SecureDataStore @Inject constructor(
    @ApplicationContext private val context: Context
) {
    
    private val masterKey = MasterKey.Builder(context)
        .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
        .build()
    
    private val encryptedPrefs = EncryptedSharedPreferences.create(
        context,
        "secure_prefs",
        masterKey,
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
    )
    
    fun saveToken(token: String) {
        encryptedPrefs.edit()
            .putString(KEY_TOKEN, token)
            .apply()
    }
    
    fun getToken(): String? {
        return encryptedPrefs.getString(KEY_TOKEN, null)
    }
    
    fun saveUserCredentials(username: String, password: String) {
        // 密码额外加密
        val encryptedPassword = EncryptUtil.aesEncrypt(password, getDeviceKey())
        
        encryptedPrefs.edit()
            .putString(KEY_USERNAME, username)
            .putString(KEY_PASSWORD, encryptedPassword)
            .apply()
    }
    
    fun getUserCredentials(): Pair<String?, String?> {
        val username = encryptedPrefs.getString(KEY_USERNAME, null)
        val encryptedPassword = encryptedPrefs.getString(KEY_PASSWORD, null)
        
        val password = encryptedPassword?.let { 
            EncryptUtil.aesDecrypt(it, getDeviceKey()) 
        }
        
        return Pair(username, password)
    }
    
    private fun getDeviceKey(): String {
        // 基于设备信息生成密钥
        val deviceInfo = "${Build.BRAND}_${Build.MODEL}_${Build.ID}"
        return EncryptUtil.sha256(deviceInfo).take(32)
    }
    
    fun clearAllData() {
        encryptedPrefs.edit().clear().apply()
    }
    
    companion object {
        private const val KEY_TOKEN = "auth_token"
        private const val KEY_USERNAME = "username"
        private const val KEY_PASSWORD = "password"
    }
}
```

### 数据库加密

```kotlin
@Module
@InstallIn(SingletonComponent::class)
object SecureDatabaseModule {
    
    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): AppDatabase {
        val passphrase = getDatabasePassphrase(context)
        
        return Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            "app_database"
        )
        .openHelperFactory(SupportFactory(passphrase))
        .build()
    }
    
    private fun getDatabasePassphrase(context: Context): ByteArray {
        val keyAlias = "database_key"
        
        return try {
            // 尝试从 Keystore 获取密钥
            getKeyFromKeystore(keyAlias)
        } catch (e: Exception) {
            // 生成新密钥并存储到 Keystore
            generateAndStoreKey(keyAlias)
        }
    }
    
    private fun getKeyFromKeystore(keyAlias: String): ByteArray {
        val keyStore = KeyStore.getInstance("AndroidKeyStore")
        keyStore.load(null)
        
        val secretKey = keyStore.getKey(keyAlias, null) as SecretKey
        return secretKey.encoded
    }
    
    private fun generateAndStoreKey(keyAlias: String): ByteArray {
        val keyGenerator = KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, "AndroidKeyStore")
        
        val keyGenParameterSpec = KeyGenParameterSpec.Builder(
            keyAlias,
            KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
        )
        .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
        .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
        .setRandomizedEncryptionRequired(false)
        .build()
        
        keyGenerator.init(keyGenParameterSpec)
        val secretKey = keyGenerator.generateKey()
        
        return secretKey.encoded
    }
}
```

### 敏感信息脱敏

```kotlin
object DataMasking {
    
    fun maskPhoneNumber(phone: String): String {
        return if (phone.length >= 11) {
            "${phone.take(3)}****${phone.takeLast(4)}"
        } else {
            phone
        }
    }
    
    fun maskEmail(email: String): String {
        val atIndex = email.indexOf('@')
        return if (atIndex > 0) {
            val username = email.substring(0, atIndex)
            val domain = email.substring(atIndex)
            
            when {
                username.length <= 2 -> "${username.first()}*$domain"
                username.length <= 4 -> "${username.take(2)}**$domain"
                else -> "${username.take(3)}****$domain"
            }
        } else {
            email
        }
    }
    
    fun maskIdCard(idCard: String): String {
        return if (idCard.length >= 18) {
            "${idCard.take(6)}********${idCard.takeLast(4)}"
        } else {
            idCard
        }
    }
    
    fun maskBankCard(bankCard: String): String {
        return if (bankCard.length >= 16) {
            "${bankCard.take(4)} **** **** ${bankCard.takeLast(4)}"
        } else {
            bankCard
        }
    }
}
```

## 🔒 代码安全

### 代码混淆配置

```kotlin
// proguard-rules.pro
-optimizationpasses 5
-dontusemixedcaseclassnames
-dontskipnonpubliclibraryclasses
-dontpreverify
-verbose

# 保留应用类
-keep public class * extends android.app.Application
-keep public class * extends android.app.Activity
-keep public class * extends android.app.Fragment

# 保留实体类
-keep class com.yourapp.data.model.** { *; }

# 保留 Retrofit 接口
-keep interface com.yourapp.data.remote.** { *; }

# 混淆日志输出
-assumenosideeffects class android.util.Log {
    public static *** d(...);
    public static *** v(...);
    public static *** i(...);
}

# 字符串加密
-adaptclassstrings
-adaptresourcefilenames
-adaptresourcefilecontents
```

### 反调试保护

```kotlin
class AntiDebugProtection {
    
    fun checkDebugging(): Boolean {
        return isDebuggingEnabled() || 
               isDebuggerConnected() || 
               isEmulator() ||
               isRooted()
    }
    
    private fun isDebuggingEnabled(): Boolean {
        return (ApplicationInfo.FLAG_DEBUGGABLE and BuildConfig.DEBUG) != 0
    }
    
    private fun isDebuggerConnected(): Boolean {
        return Debug.isDebuggerConnected() || Debug.waitingForDebugger()
    }
    
    private fun isEmulator(): Boolean {
        return Build.FINGERPRINT.startsWith("generic") ||
               Build.FINGERPRINT.startsWith("unknown") ||
               Build.MODEL.contains("google_sdk") ||
               Build.MODEL.contains("Emulator") ||
               Build.MODEL.contains("Android SDK built for x86") ||
               Build.MANUFACTURER.contains("Genymotion") ||
               Build.BRAND.startsWith("generic") && Build.DEVICE.startsWith("generic") ||
               "google_sdk" == Build.PRODUCT
    }
    
    private fun isRooted(): Boolean {
        return checkRootMethod1() || checkRootMethod2() || checkRootMethod3()
    }
    
    private fun checkRootMethod1(): Boolean {
        val buildTags = Build.TAGS
        return buildTags != null && buildTags.contains("test-keys")
    }
    
    private fun checkRootMethod2(): Boolean {
        val paths = arrayOf(
            "/system/app/Superuser.apk",
            "/sbin/su",
            "/system/bin/su",
            "/system/xbin/su",
            "/data/local/xbin/su",
            "/data/local/bin/su",
            "/system/sd/xbin/su",
            "/system/bin/failsafe/su",
            "/data/local/su"
        )
        
        return paths.any { File(it).exists() }
    }
    
    private fun checkRootMethod3(): Boolean {
        return try {
            Runtime.getRuntime().exec("su")
            true
        } catch (e: Exception) {
            false
        }
    }
    
    fun handleSecurityThreat() {
        Logger.w("Security threat detected!")
        
        // 清除敏感数据
        clearSensitiveData()
        
        // 退出应用
        exitProcess(0)
    }
    
    private fun clearSensitiveData() {
        // 清除缓存
        MemoryCache.clearAll()
        
        // 清除敏感的 SharedPreferences
        SecureDataStore.clearAllData()
        
        // 清除数据库敏感表
        DatabaseManager.clearSensitiveTables()
    }
}
```

### 代码完整性检查

```kotlin
class CodeIntegrityChecker {
    
    fun verifyAppSignature(context: Context): Boolean {
        return try {
            val packageInfo = context.packageManager.getPackageInfo(
                context.packageName,
                PackageManager.GET_SIGNATURES
            )
            
            val signatures = packageInfo.signatures
            val expectedSignature = getExpectedSignature()
            
            signatures.any { signature ->
                val signatureHash = EncryptUtil.sha256(signature.toByteArray())
                signatureHash == expectedSignature
            }
        } catch (e: Exception) {
            Logger.e("Failed to verify app signature", e)
            false
        }
    }
    
    private fun getExpectedSignature(): String {
        // 返回预期的签名哈希值
        return "your_expected_signature_hash"
    }
    
    fun verifyApkIntegrity(context: Context): Boolean {
        return try {
            val packageInfo = context.packageManager.getPackageInfo(
                context.packageName,
                0
            )
            
            val apkPath = packageInfo.applicationInfo.sourceDir
            val apkFile = File(apkPath)
            
            val apkHash = calculateFileHash(apkFile)
            val expectedHash = getExpectedApkHash()
            
            apkHash == expectedHash
        } catch (e: Exception) {
            Logger.e("Failed to verify APK integrity", e)
            false
        }
    }
    
    private fun calculateFileHash(file: File): String {
        return file.inputStream().use { input ->
            val digest = MessageDigest.getInstance("SHA-256")
            val buffer = ByteArray(8192)
            var bytesRead: Int
            
            while (input.read(buffer).also { bytesRead = it } != -1) {
                digest.update(buffer, 0, bytesRead)
            }
            
            digest.digest().joinToString("") { "%02x".format(it) }
        }
    }
    
    private fun getExpectedApkHash(): String {
        // 返回预期的 APK 哈希值
        return "your_expected_apk_hash"
    }
}
```

## 🔑 权限管理

### 运行时权限处理

```kotlin
class PermissionManager(private val activity: Activity) {
    
    private val permissionLauncher = (activity as ComponentActivity)
        .registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
            handlePermissionResult(permissions)
        }
    
    fun requestPermissions(
        permissions: Array<String>,
        callback: (granted: Boolean, deniedPermissions: List<String>) -> Unit
    ) {
        val deniedPermissions = permissions.filter { permission ->
            ContextCompat.checkSelfPermission(activity, permission) != PackageManager.PERMISSION_GRANTED
        }
        
        if (deniedPermissions.isEmpty()) {
            callback(true, emptyList())
            return
        }
        
        // 检查是否需要显示权限说明
        val shouldShowRationale = deniedPermissions.any { permission ->
            ActivityCompat.shouldShowRequestPermissionRationale(activity, permission)
        }
        
        if (shouldShowRationale) {
            showPermissionRationale(deniedPermissions) { granted ->
                if (granted) {
                    permissionLauncher.launch(deniedPermissions.toTypedArray())
                } else {
                    callback(false, deniedPermissions)
                }
            }
        } else {
            permissionLauncher.launch(deniedPermissions.toTypedArray())
        }
    }
    
    private fun showPermissionRationale(
        permissions: List<String>,
        callback: (Boolean) -> Unit
    ) {
        val permissionNames = permissions.map { getPermissionName(it) }
        val message = "应用需要以下权限才能正常工作：\n${permissionNames.joinToString("\n")}"
        
        AlertDialog.Builder(activity)
            .setTitle("权限申请")
            .setMessage(message)
            .setPositiveButton("授权") { _, _ -> callback(true) }
            .setNegativeButton("拒绝") { _, _ -> callback(false) }
            .setCancelable(false)
            .show()
    }
    
    private fun handlePermissionResult(permissions: Map<String, Boolean>) {
        val grantedPermissions = permissions.filter { it.value }.keys
        val deniedPermissions = permissions.filter { !it.value }.keys
        
        Logger.d("Granted permissions: $grantedPermissions")
        Logger.d("Denied permissions: $deniedPermissions")
        
        // 记录权限使用情况
        TrackManager.trackEvent("permission_result", mapOf(
            "granted" to grantedPermissions.joinToString(","),
            "denied" to deniedPermissions.joinToString(",")
        ))
    }
    
    private fun getPermissionName(permission: String): String {
        return when (permission) {
            Manifest.permission.CAMERA -> "相机"
            Manifest.permission.READ_EXTERNAL_STORAGE -> "存储读取"
            Manifest.permission.WRITE_EXTERNAL_STORAGE -> "存储写入"
            Manifest.permission.ACCESS_FINE_LOCATION -> "精确位置"
            Manifest.permission.ACCESS_COARSE_LOCATION -> "大致位置"
            Manifest.permission.RECORD_AUDIO -> "录音"
            else -> permission
        }
    }
}
```

### 权限最小化原则

```xml
<!-- AndroidManifest.xml -->
<manifest>
    <!-- 只申请必要的权限 -->
    <uses-permission android:name="android.permission.INTERNET" />
    <uses-permission android:name="android.permission.ACCESS_NETWORK_STATE" />
    
    <!-- 条件权限申请 -->
    <uses-permission 
        android:name="android.permission.CAMERA"
        android:required="false" />
    
    <!-- 限制权限使用范围 -->
    <uses-permission 
        android:name="android.permission.READ_EXTERNAL_STORAGE"
        android:maxSdkVersion="32" />
    
    <!-- 权限组合使用 -->
    <uses-permission-sdk-23 android:name="android.permission.ACCESS_FINE_LOCATION" />
</manifest>
```

## 🔍 安全检测

### 安全扫描工具集成

```kotlin
class SecurityScanner {
    
    fun performSecurityScan(): SecurityScanResult {
        val results = mutableListOf<SecurityIssue>()
        
        // 1. 检查调试状态
        if (BuildConfig.DEBUG) {
            results.add(SecurityIssue(
                type = SecurityIssueType.DEBUG_ENABLED,
                severity = SecuritySeverity.HIGH,
                description = "应用处于调试模式"
            ))
        }
        
        // 2. 检查网络安全
        if (!isNetworkSecure()) {
            results.add(SecurityIssue(
                type = SecurityIssueType.INSECURE_NETWORK,
                severity = SecuritySeverity.CRITICAL,
                description = "网络通信不安全"
            ))
        }
        
        // 3. 检查存储安全
        if (!isStorageSecure()) {
            results.add(SecurityIssue(
                type = SecurityIssueType.INSECURE_STORAGE,
                severity = SecuritySeverity.HIGH,
                description = "数据存储不安全"
            ))
        }
        
        // 4. 检查权限使用
        val unnecessaryPermissions = checkUnnecessaryPermissions()
        if (unnecessaryPermissions.isNotEmpty()) {
            results.add(SecurityIssue(
                type = SecurityIssueType.EXCESSIVE_PERMISSIONS,
                severity = SecuritySeverity.MEDIUM,
                description = "申请了不必要的权限: ${unnecessaryPermissions.joinToString()}"
            ))
        }
        
        return SecurityScanResult(
            issues = results,
            overallScore = calculateSecurityScore(results),
            scanTime = System.currentTimeMillis()
        )
    }
    
    private fun isNetworkSecure(): Boolean {
        // 检查是否强制使用 HTTPS
        // 检查是否启用证书固定
        return true // 简化实现
    }
    
    private fun isStorageSecure(): Boolean {
        // 检查敏感数据是否加密存储
        // 检查是否使用了安全的存储方式
        return true // 简化实现
    }
    
    private fun checkUnnecessaryPermissions(): List<String> {
        // 分析权限使用情况，识别不必要的权限
        return emptyList() // 简化实现
    }
    
    private fun calculateSecurityScore(issues: List<SecurityIssue>): Int {
        var score = 100
        
        issues.forEach { issue ->
            score -= when (issue.severity) {
                SecuritySeverity.CRITICAL -> 30
                SecuritySeverity.HIGH -> 20
                SecuritySeverity.MEDIUM -> 10
                SecuritySeverity.LOW -> 5
            }
        }
        
        return maxOf(0, score)
    }
}

data class SecurityScanResult(
    val issues: List<SecurityIssue>,
    val overallScore: Int,
    val scanTime: Long
)

data class SecurityIssue(
    val type: SecurityIssueType,
    val severity: SecuritySeverity,
    val description: String
)

enum class SecurityIssueType {
    DEBUG_ENABLED,
    INSECURE_NETWORK,
    INSECURE_STORAGE,
    EXCESSIVE_PERMISSIONS,
    CODE_VULNERABILITY
}

enum class SecuritySeverity {
    LOW, MEDIUM, HIGH, CRITICAL
}
```

## 🎯 安全最佳实践

### 1. 开发阶段
- **安全编码**: 遵循安全编码规范
- **代码审查**: 重点关注安全相关代码
- **依赖检查**: 定期检查第三方库的安全漏洞
- **静态分析**: 使用安全扫描工具

### 2. 测试阶段
- **渗透测试**: 进行安全渗透测试
- **漏洞扫描**: 使用自动化工具扫描漏洞
- **权限测试**: 验证权限申请和使用的合理性
- **数据安全测试**: 验证敏感数据的保护措施

### 3. 发布阶段
- **代码混淆**: 启用代码混淆和优化
- **签名验证**: 使用正式签名发布
- **安全配置**: 确保生产环境的安全配置
- **监控部署**: 部署安全监控机制

### 4. 运维阶段
- **安全监控**: 持续监控安全威胁
- **漏洞响应**: 建立安全漏洞响应机制
- **更新维护**: 及时修复安全漏洞
- **用户教育**: 教育用户安全使用应用

---

*安全是一个持续的过程，需要在应用的整个生命周期中持续关注和改进。*