# 数据加密指南

本文档详细说明了 Android MVVM 框架中的数据加密策略和实施方案。

## 📋 目录

- [加密概述](#加密概述)
- [对称加密](#对称加密)
- [非对称加密](#非对称加密)
- [哈希算法](#哈希算法)
- [密钥管理](#密钥管理)
- [实际应用](#实际应用)

## 🔐 加密概述

### 加密策略

- **传输加密**: 网络传输数据使用 HTTPS/TLS
- **存储加密**: 本地敏感数据加密存储
- **内存保护**: 运行时敏感数据保护
- **密钥安全**: 使用 Android Keystore 保护密钥

### 加密架构

```
┌─────────────────┐    ┌─────────────────┐    ┌─────────────────┐
│   应用层加密     │    │   系统层加密     │    │   硬件层加密     │
│                 │    │                 │    │                 │
│ AES/RSA 加密    │    │ Android Keystore│    │ TEE/SE 安全芯片 │
│ 自定义加密算法   │    │ 文件系统加密     │    │ 硬件密钥存储     │
│ 数据脱敏        │    │ 数据库加密       │    │ 生物识别加密     │
└─────────────────┘    └─────────────────┘    └─────────────────┘
```

## 🔑 对称加密

### AES 加密工具类

```kotlin
object AESUtil {
    
    private const val TRANSFORMATION = "AES/GCM/NoPadding"
    private const val ANDROID_KEYSTORE = "AndroidKeyStore"
    private const val KEY_ALIAS = "AESKey"
    
    fun encrypt(data: String, keyAlias: String = KEY_ALIAS): EncryptedData {
        val secretKey = getOrCreateSecretKey(keyAlias)
        val cipher = Cipher.getInstance(TRANSFORMATION)
        cipher.init(Cipher.ENCRYPT_MODE, secretKey)
        
        val iv = cipher.iv
        val encryptedBytes = cipher.doFinal(data.toByteArray(Charsets.UTF_8))
        
        return EncryptedData(
            encryptedData = Base64.encodeToString(encryptedBytes, Base64.DEFAULT),
            iv = Base64.encodeToString(iv, Base64.DEFAULT)
        )
    }
    
    fun decrypt(encryptedData: EncryptedData, keyAlias: String = KEY_ALIAS): String {
        val secretKey = getOrCreateSecretKey(keyAlias)
        val cipher = Cipher.getInstance(TRANSFORMATION)
        
        val iv = Base64.decode(encryptedData.iv, Base64.DEFAULT)
        val spec = GCMParameterSpec(128, iv)
        cipher.init(Cipher.DECRYPT_MODE, secretKey, spec)
        
        val encryptedBytes = Base64.decode(encryptedData.encryptedData, Base64.DEFAULT)
        val decryptedBytes = cipher.doFinal(encryptedBytes)
        
        return String(decryptedBytes, Charsets.UTF_8)
    }
    
    private fun getOrCreateSecretKey(keyAlias: String): SecretKey {
        val keyStore = KeyStore.getInstance(ANDROID_KEYSTORE)
        keyStore.load(null)
        
        return if (keyStore.containsAlias(keyAlias)) {
            keyStore.getKey(keyAlias, null) as SecretKey
        } else {
            generateSecretKey(keyAlias)
        }
    }
    
    private fun generateSecretKey(keyAlias: String): SecretKey {
        val keyGenerator = KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, ANDROID_KEYSTORE)
        
        val keyGenParameterSpec = KeyGenParameterSpec.Builder(
            keyAlias,
            KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
        )
        .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
        .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
        .setUserAuthenticationRequired(false)
        .setRandomizedEncryptionRequired(false)
        .build()
        
        keyGenerator.init(keyGenParameterSpec)
        return keyGenerator.generateKey()
    }
    
    fun deleteKey(keyAlias: String) {
        val keyStore = KeyStore.getInstance(ANDROID_KEYSTORE)
        keyStore.load(null)
        keyStore.deleteEntry(keyAlias)
    }
}

data class EncryptedData(
    val encryptedData: String,
    val iv: String
)
```

### 文件加密

```kotlin
class FileEncryption {
    
    fun encryptFile(inputFile: File, outputFile: File, keyAlias: String) {
        val secretKey = AESUtil.getOrCreateSecretKey(keyAlias)
        val cipher = Cipher.getInstance("AES/CBC/PKCS7Padding")
        cipher.init(Cipher.ENCRYPT_MODE, secretKey)
        
        val iv = cipher.iv
        
        FileInputStream(inputFile).use { fis ->
            FileOutputStream(outputFile).use { fos ->
                // 写入 IV
                fos.write(iv.size)
                fos.write(iv)
                
                // 加密文件内容
                val cipherOutputStream = CipherOutputStream(fos, cipher)
                val buffer = ByteArray(8192)
                var bytesRead: Int
                
                while (fis.read(buffer).also { bytesRead = it } != -1) {
                    cipherOutputStream.write(buffer, 0, bytesRead)
                }
                
                cipherOutputStream.close()
            }
        }
    }
    
    fun decryptFile(inputFile: File, outputFile: File, keyAlias: String) {
        val secretKey = AESUtil.getOrCreateSecretKey(keyAlias)
        
        FileInputStream(inputFile).use { fis ->
            // 读取 IV
            val ivSize = fis.read()
            val iv = ByteArray(ivSize)
            fis.read(iv)
            
            val cipher = Cipher.getInstance("AES/CBC/PKCS7Padding")
            val spec = IvParameterSpec(iv)
            cipher.init(Cipher.DECRYPT_MODE, secretKey, spec)
            
            FileOutputStream(outputFile).use { fos ->
                val cipherInputStream = CipherInputStream(fis, cipher)
                val buffer = ByteArray(8192)
                var bytesRead: Int
                
                while (cipherInputStream.read(buffer).also { bytesRead = it } != -1) {
                    fos.write(buffer, 0, bytesRead)
                }
                
                cipherInputStream.close()
            }
        }
    }
}
```

## 🔐 非对称加密

### RSA 加密工具类

```kotlin
object RSAUtil {
    
    private const val TRANSFORMATION = "RSA/ECB/PKCS1Padding"
    private const val ANDROID_KEYSTORE = "AndroidKeyStore"
    private const val KEY_ALIAS = "RSAKey"
    
    fun generateKeyPair(keyAlias: String = KEY_ALIAS): KeyPair {
        val keyPairGenerator = KeyPairGenerator.getInstance(KeyProperties.KEY_ALGORITHM_RSA, ANDROID_KEYSTORE)
        
        val keyGenParameterSpec = KeyGenParameterSpec.Builder(
            keyAlias,
            KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
        )
        .setDigests(KeyProperties.DIGEST_SHA256, KeyProperties.DIGEST_SHA512)
        .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_RSA_PKCS1)
        .setKeySize(2048)
        .setUserAuthenticationRequired(false)
        .build()
        
        keyPairGenerator.initialize(keyGenParameterSpec)
        return keyPairGenerator.generateKeyPair()
    }
    
    fun encrypt(data: String, keyAlias: String = KEY_ALIAS): String {
        val keyPair = getOrCreateKeyPair(keyAlias)
        val cipher = Cipher.getInstance(TRANSFORMATION)
        cipher.init(Cipher.ENCRYPT_MODE, keyPair.public)
        
        val encryptedBytes = cipher.doFinal(data.toByteArray(Charsets.UTF_8))
        return Base64.encodeToString(encryptedBytes, Base64.DEFAULT)
    }
    
    fun decrypt(encryptedData: String, keyAlias: String = KEY_ALIAS): String {
        val keyPair = getOrCreateKeyPair(keyAlias)
        val cipher = Cipher.getInstance(TRANSFORMATION)
        cipher.init(Cipher.DECRYPT_MODE, keyPair.private)
        
        val encryptedBytes = Base64.decode(encryptedData, Base64.DEFAULT)
        val decryptedBytes = cipher.doFinal(encryptedBytes)
        
        return String(decryptedBytes, Charsets.UTF_8)
    }
    
    private fun getOrCreateKeyPair(keyAlias: String): KeyPair {
        val keyStore = KeyStore.getInstance(ANDROID_KEYSTORE)
        keyStore.load(null)
        
        return if (keyStore.containsAlias(keyAlias)) {
            val privateKey = keyStore.getKey(keyAlias, null) as PrivateKey
            val publicKey = keyStore.getCertificate(keyAlias).publicKey
            KeyPair(publicKey, privateKey)
        } else {
            generateKeyPair(keyAlias)
        }
    }
    
    fun sign(data: String, keyAlias: String = KEY_ALIAS): String {
        val keyPair = getOrCreateKeyPair(keyAlias)
        val signature = Signature.getInstance("SHA256withRSA")
        signature.initSign(keyPair.private)
        signature.update(data.toByteArray(Charsets.UTF_8))
        
        val signatureBytes = signature.sign()
        return Base64.encodeToString(signatureBytes, Base64.DEFAULT)
    }
    
    fun verify(data: String, signatureString: String, keyAlias: String = KEY_ALIAS): Boolean {
        val keyPair = getOrCreateKeyPair(keyAlias)
        val signature = Signature.getInstance("SHA256withRSA")
        signature.initVerify(keyPair.public)
        signature.update(data.toByteArray(Charsets.UTF_8))
        
        val signatureBytes = Base64.decode(signatureString, Base64.DEFAULT)
        return signature.verify(signatureBytes)
    }
}
```

### 混合加密方案

```kotlin
class HybridEncryption {
    
    fun encrypt(data: String, rsaKeyAlias: String, aesKeyAlias: String): HybridEncryptedData {
        // 1. 生成随机 AES 密钥
        val aesKey = generateRandomAESKey()
        
        // 2. 使用 AES 密钥加密数据
        val encryptedData = encryptWithAES(data, aesKey)
        
        // 3. 使用 RSA 公钥加密 AES 密钥
        val encryptedAESKey = RSAUtil.encrypt(
            Base64.encodeToString(aesKey.encoded, Base64.DEFAULT),
            rsaKeyAlias
        )
        
        return HybridEncryptedData(
            encryptedData = encryptedData,
            encryptedKey = encryptedAESKey
        )
    }
    
    fun decrypt(hybridData: HybridEncryptedData, rsaKeyAlias: String): String {
        // 1. 使用 RSA 私钥解密 AES 密钥
        val aesKeyString = RSAUtil.decrypt(hybridData.encryptedKey, rsaKeyAlias)
        val aesKeyBytes = Base64.decode(aesKeyString, Base64.DEFAULT)
        val aesKey = SecretKeySpec(aesKeyBytes, "AES")
        
        // 2. 使用 AES 密钥解密数据
        return decryptWithAES(hybridData.encryptedData, aesKey)
    }
    
    private fun generateRandomAESKey(): SecretKey {
        val keyGenerator = KeyGenerator.getInstance("AES")
        keyGenerator.init(256)
        return keyGenerator.generateKey()
    }
    
    private fun encryptWithAES(data: String, key: SecretKey): EncryptedData {
        val cipher = Cipher.getInstance("AES/GCM/NoPadding")
        cipher.init(Cipher.ENCRYPT_MODE, key)
        
        val iv = cipher.iv
        val encryptedBytes = cipher.doFinal(data.toByteArray(Charsets.UTF_8))
        
        return EncryptedData(
            encryptedData = Base64.encodeToString(encryptedBytes, Base64.DEFAULT),
            iv = Base64.encodeToString(iv, Base64.DEFAULT)
        )
    }
    
    private fun decryptWithAES(encryptedData: EncryptedData, key: SecretKey): String {
        val cipher = Cipher.getInstance("AES/GCM/NoPadding")
        
        val iv = Base64.decode(encryptedData.iv, Base64.DEFAULT)
        val spec = GCMParameterSpec(128, iv)
        cipher.init(Cipher.DECRYPT_MODE, key, spec)
        
        val encryptedBytes = Base64.decode(encryptedData.encryptedData, Base64.DEFAULT)
        val decryptedBytes = cipher.doFinal(encryptedBytes)
        
        return String(decryptedBytes, Charsets.UTF_8)
    }
}

data class HybridEncryptedData(
    val encryptedData: EncryptedData,
    val encryptedKey: String
)
```

## 🔢 哈希算法

### 哈希工具类

```kotlin
object HashUtil {
    
    fun sha256(data: String): String {
        val digest = MessageDigest.getInstance("SHA-256")
        val hashBytes = digest.digest(data.toByteArray(Charsets.UTF_8))
        return hashBytes.joinToString("") { "%02x".format(it) }
    }
    
    fun sha512(data: String): String {
        val digest = MessageDigest.getInstance("SHA-512")
        val hashBytes = digest.digest(data.toByteArray(Charsets.UTF_8))
        return hashBytes.joinToString("") { "%02x".format(it) }
    }
    
    fun md5(data: String): String {
        val digest = MessageDigest.getInstance("MD5")
        val hashBytes = digest.digest(data.toByteArray(Charsets.UTF_8))
        return hashBytes.joinToString("") { "%02x".format(it) }
    }
    
    fun hmacSha256(data: String, key: String): String {
        val mac = Mac.getInstance("HmacSHA256")
        val secretKey = SecretKeySpec(key.toByteArray(Charsets.UTF_8), "HmacSHA256")
        mac.init(secretKey)
        
        val hashBytes = mac.doFinal(data.toByteArray(Charsets.UTF_8))
        return Base64.encodeToString(hashBytes, Base64.DEFAULT)
    }
    
    fun pbkdf2(password: String, salt: String, iterations: Int = 10000): String {
        val spec = PBEKeySpec(password.toCharArray(), salt.toByteArray(), iterations, 256)
        val factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256")
        val hash = factory.generateSecret(spec).encoded
        return Base64.encodeToString(hash, Base64.DEFAULT)
    }
    
    fun generateSalt(): String {
        val random = SecureRandom()
        val salt = ByteArray(16)
        random.nextBytes(salt)
        return Base64.encodeToString(salt, Base64.DEFAULT)
    }
    
    fun verifyPassword(password: String, salt: String, hash: String): Boolean {
        val computedHash = pbkdf2(password, salt)
        return computedHash == hash
    }
}
```

### 文件完整性校验

```kotlin
class FileIntegrityChecker {
    
    fun calculateFileHash(file: File, algorithm: String = "SHA-256"): String {
        val digest = MessageDigest.getInstance(algorithm)
        
        file.inputStream().use { input ->
            val buffer = ByteArray(8192)
            var bytesRead: Int
            
            while (input.read(buffer).also { bytesRead = it } != -1) {
                digest.update(buffer, 0, bytesRead)
            }
        }
        
        return digest.digest().joinToString("") { "%02x".format(it) }
    }
    
    fun verifyFileIntegrity(file: File, expectedHash: String, algorithm: String = "SHA-256"): Boolean {
        val actualHash = calculateFileHash(file, algorithm)
        return actualHash.equals(expectedHash, ignoreCase = true)
    }
    
    fun generateChecksumFile(file: File, checksumFile: File, algorithm: String = "SHA-256") {
        val hash = calculateFileHash(file, algorithm)
        val checksum = "${file.name} $hash"
        checksumFile.writeText(checksum)
    }
    
    fun verifyChecksumFile(file: File, checksumFile: File, algorithm: String = "SHA-256"): Boolean {
        val checksumContent = checksumFile.readText().trim()
        val parts = checksumContent.split(" ")
        
        if (parts.size != 2) {
            return false
        }
        
        val expectedHash = parts[1]
        return verifyFileIntegrity(file, expectedHash, algorithm)
    }
}
```

## 🗝️ 密钥管理

### Android Keystore 管理

```kotlin
@Singleton
class KeystoreManager @Inject constructor() {
    
    private val keyStore: KeyStore by lazy {
        KeyStore.getInstance("AndroidKeyStore").apply {
            load(null)
        }
    }
    
    fun generateAESKey(
        alias: String,
        requireAuthentication: Boolean = false,
        validityDuration: Int = 30
    ): SecretKey {
        val keyGenerator = KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, "AndroidKeyStore")
        
        val builder = KeyGenParameterSpec.Builder(
            alias,
            KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
        )
        .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
        .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
        .setRandomizedEncryptionRequired(false)
        
        if (requireAuthentication) {
            builder.setUserAuthenticationRequired(true)
            builder.setUserAuthenticationValidityDurationSeconds(validityDuration)
        }
        
        keyGenerator.init(builder.build())
        return keyGenerator.generateKey()
    }
    
    fun generateRSAKeyPair(
        alias: String,
        keySize: Int = 2048,
        requireAuthentication: Boolean = false
    ): KeyPair {
        val keyPairGenerator = KeyPairGenerator.getInstance(KeyProperties.KEY_ALGORITHM_RSA, "AndroidKeyStore")
        
        val builder = KeyGenParameterSpec.Builder(
            alias,
            KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT or
            KeyProperties.PURPOSE_SIGN or KeyProperties.PURPOSE_VERIFY
        )
        .setDigests(KeyProperties.DIGEST_SHA256, KeyProperties.DIGEST_SHA512)
        .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_RSA_PKCS1)
        .setSignaturePaddings(KeyProperties.SIGNATURE_PADDING_RSA_PKCS1)
        .setKeySize(keySize)
        
        if (requireAuthentication) {
            builder.setUserAuthenticationRequired(true)
        }
        
        keyPairGenerator.initialize(builder.build())
        return keyPairGenerator.generateKeyPair()
    }
    
    fun getSecretKey(alias: String): SecretKey? {
        return try {
            if (keyStore.containsAlias(alias)) {
                keyStore.getKey(alias, null) as SecretKey
            } else {
                null
            }
        } catch (e: Exception) {
            Logger.e("Failed to get secret key: $alias", e)
            null
        }
    }
    
    fun getKeyPair(alias: String): KeyPair? {
        return try {
            if (keyStore.containsAlias(alias)) {
                val privateKey = keyStore.getKey(alias, null) as PrivateKey
                val publicKey = keyStore.getCertificate(alias).publicKey
                KeyPair(publicKey, privateKey)
            } else {
                null
            }
        } catch (e: Exception) {
            Logger.e("Failed to get key pair: $alias", e)
            null
        }
    }
    
    fun deleteKey(alias: String): Boolean {
        return try {
            if (keyStore.containsAlias(alias)) {
                keyStore.deleteEntry(alias)
                true
            } else {
                false
            }
        } catch (e: Exception) {
            Logger.e("Failed to delete key: $alias", e)
            false
        }
    }
    
    fun listKeys(): List<String> {
        return try {
            keyStore.aliases().toList()
        } catch (e: Exception) {
            Logger.e("Failed to list keys", e)
            emptyList()
        }
    }
    
    fun keyExists(alias: String): Boolean {
        return try {
            keyStore.containsAlias(alias)
        } catch (e: Exception) {
            Logger.e("Failed to check key existence: $alias", e)
            false
        }
    }
}
```

### 密钥轮换策略

```kotlin
class KeyRotationManager @Inject constructor(
    private val keystoreManager: KeystoreManager,
    private val secureDataStore: SecureDataStore
) {
    
    private val keyRotationInterval = TimeUnit.DAYS.toMillis(90) // 90天轮换一次
    
    fun checkAndRotateKeys() {
        val keys = keystoreManager.listKeys()
        
        keys.forEach { keyAlias ->
            if (shouldRotateKey(keyAlias)) {
                rotateKey(keyAlias)
            }
        }
    }
    
    private fun shouldRotateKey(keyAlias: String): Boolean {
        val lastRotationTime = secureDataStore.getKeyRotationTime(keyAlias)
        val currentTime = System.currentTimeMillis()
        
        return currentTime - lastRotationTime > keyRotationInterval
    }
    
    private fun rotateKey(keyAlias: String) {
        try {
            Logger.d("Rotating key: $keyAlias")
            
            // 1. 生成新密钥
            val newKeyAlias = "${keyAlias}_new"
            keystoreManager.generateAESKey(newKeyAlias)
            
            // 2. 重新加密数据
            reencryptDataWithNewKey(keyAlias, newKeyAlias)
            
            // 3. 删除旧密钥
            keystoreManager.deleteKey(keyAlias)
            
            // 4. 重命名新密钥
            renameKey(newKeyAlias, keyAlias)
            
            // 5. 更新轮换时间
            secureDataStore.setKeyRotationTime(keyAlias, System.currentTimeMillis())
            
            Logger.d("Key rotation completed: $keyAlias")
            
        } catch (e: Exception) {
            Logger.e("Key rotation failed: $keyAlias", e)
        }
    }
    
    private fun reencryptDataWithNewKey(oldKeyAlias: String, newKeyAlias: String) {
        // 获取需要重新加密的数据
        val encryptedData = secureDataStore.getAllEncryptedData(oldKeyAlias)
        
        encryptedData.forEach { (dataKey, encryptedValue) ->
            try {
                // 使用旧密钥解密
                val decryptedData = AESUtil.decrypt(encryptedValue, oldKeyAlias)
                
                // 使用新密钥加密
                val newEncryptedData = AESUtil.encrypt(decryptedData, newKeyAlias)
                
                // 保存新的加密数据
                secureDataStore.saveEncryptedData(dataKey, newEncryptedData, newKeyAlias)
                
            } catch (e: Exception) {
                Logger.e("Failed to reencrypt data: $dataKey", e)
            }
        }
    }
    
    private fun renameKey(oldAlias: String, newAlias: String) {
        // Android Keystore 不支持直接重命名
        // 这里需要根据具体实现来处理
        // 可能需要重新生成密钥并更新所有引用
    }
    
    fun scheduleKeyRotation() {
        // 定期检查密钥轮换
        GlobalScope.launch {
            while (true) {
                delay(TimeUnit.HOURS.toMillis(24)) // 每天检查一次
                checkAndRotateKeys()
            }
        }
    }
}
```

## 💼 实际应用

### 用户密码加密

```kotlin
class PasswordManager @Inject constructor(
    private val secureDataStore: SecureDataStore
) {
    
    fun hashPassword(password: String): HashedPassword {
        val salt = HashUtil.generateSalt()
        val hash = HashUtil.pbkdf2(password, salt, 10000)
        
        return HashedPassword(
            hash = hash,
            salt = salt,
            algorithm = "PBKDF2WithHmacSHA256",
            iterations = 10000
        )
    }
    
    fun verifyPassword(password: String, hashedPassword: HashedPassword): Boolean {
        return HashUtil.verifyPassword(password, hashedPassword.salt, hashedPassword.hash)
    }
    
    fun saveUserPassword(userId: String, password: String) {
        val hashedPassword = hashPassword(password)
        secureDataStore.saveHashedPassword(userId, hashedPassword)
    }
    
    fun changeUserPassword(userId: String, oldPassword: String, newPassword: String): Boolean {
        val storedPassword = secureDataStore.getHashedPassword(userId)
        
        if (storedPassword != null && verifyPassword(oldPassword, storedPassword)) {
            saveUserPassword(userId, newPassword)
            return true
        }
        
        return false
    }
}

data class HashedPassword(
    val hash: String,
    val salt: String,
    val algorithm: String,
    val iterations: Int
)
```

### API 通信加密

```kotlin
class EncryptedApiService @Inject constructor(
    private val apiService: ApiService,
    private val keystoreManager: KeystoreManager
) {
    
    private val apiKeyAlias = "api_encryption_key"
    
    suspend fun sendEncryptedRequest(request: ApiRequest): ApiResponse {
        // 1. 序列化请求数据
        val requestJson = Json.encodeToString(request)
        
        // 2. 加密请求数据
        val encryptedRequest = AESUtil.encrypt(requestJson, apiKeyAlias)
        
        // 3. 发送加密请求
        val encryptedResponse = apiService.sendEncryptedData(
            EncryptedApiRequest(
                data = encryptedRequest.encryptedData,
                iv = encryptedRequest.iv
            )
        )
        
        // 4. 解密响应数据
        val decryptedResponse = AESUtil.decrypt(
            EncryptedData(
                encryptedData = encryptedResponse.data,
                iv = encryptedResponse.iv
            ),
            apiKeyAlias
        )
        
        // 5. 反序列化响应数据
        return Json.decodeFromString(decryptedResponse)
    }
    
    fun initializeApiEncryption() {
        if (!keystoreManager.keyExists(apiKeyAlias)) {
            keystoreManager.generateAESKey(apiKeyAlias)
        }
    }
}

@Serializable
data class EncryptedApiRequest(
    val data: String,
    val iv: String
)

@Serializable
data class EncryptedApiResponse(
    val data: String,
    val iv: String
)
```

### 文件存储加密

```kotlin
class EncryptedFileManager @Inject constructor(
    private val context: Context,
    private val keystoreManager: KeystoreManager
) {
    
    private val fileKeyAlias = "file_encryption_key"
    
    fun saveEncryptedFile(fileName: String, data: String) {
        val file = File(context.filesDir, "$fileName.enc")
        val encryptedData = AESUtil.encrypt(data, fileKeyAlias)
        
        val fileData = EncryptedFileData(
            data = encryptedData.encryptedData,
            iv = encryptedData.iv,
            timestamp = System.currentTimeMillis()
        )
        
        file.writeText(Json.encodeToString(fileData))
    }
    
    fun loadEncryptedFile(fileName: String): String? {
        val file = File(context.filesDir, "$fileName.enc")
        
        if (!file.exists()) {
            return null
        }
        
        return try {
            val fileContent = file.readText()
            val fileData = Json.decodeFromString<EncryptedFileData>(fileContent)
            
            AESUtil.decrypt(
                EncryptedData(
                    encryptedData = fileData.data,
                    iv = fileData.iv
                ),
                fileKeyAlias
            )
        } catch (e: Exception) {
            Logger.e("Failed to load encrypted file: $fileName", e)
            null
        }
    }
    
    fun deleteEncryptedFile(fileName: String): Boolean {
        val file = File(context.filesDir, "$fileName.enc")
        return file.delete()
    }
    
    fun initializeFileEncryption() {
        if (!keystoreManager.keyExists(fileKeyAlias)) {
            keystoreManager.generateAESKey(fileKeyAlias)
        }
    }
}

@Serializable
data class EncryptedFileData(
    val data: String,
    val iv: String,
    val timestamp: Long
)
```

## 🎯 加密最佳实践

### 1. 密钥管理
- **使用 Android Keystore**: 将密钥存储在硬件安全模块中
- **定期轮换密钥**: 建立密钥轮换机制
- **最小权限原则**: 只授予必要的密钥使用权限
- **密钥备份**: 建立安全的密钥备份和恢复机制

### 2. 算法选择
- **对称加密**: 使用 AES-256-GCM
- **非对称加密**: 使用 RSA-2048 或 ECC-P256
- **哈希算法**: 使用 SHA-256 或 SHA-3
- **密钥派生**: 使用 PBKDF2 或 Argon2

### 3. 实施原则
- **加密传输**: 所有网络通信使用 TLS
- **加密存储**: 敏感数据加密存储
- **安全随机数**: 使用 SecureRandom 生成随机数
- **完整性校验**: 使用 HMAC 或数字签名验证数据完整性

### 4. 性能优化
- **硬件加速**: 利用硬件加密加速
- **缓存机制**: 合理缓存加密结果
- **异步处理**: 在后台线程执行加密操作
- **批量处理**: 批量处理多个加密操作

---

*数据加密是保护用户隐私和应用安全的重要手段，需要根据具体场景选择合适的加密方案。*