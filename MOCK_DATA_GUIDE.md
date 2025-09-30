# Mock 数据使用指南

## 📝 说明

由于示例项目没有真实的后端服务器，我们在 `LoginRepository` 中添加了 Mock 数据功能，让你可以在本地测试登录功能。

## 🔧 配置说明

在 `LoginRepository.kt` 中有一个开关：

```kotlin
/**
 * 是否使用 Mock 数据（用于演示）
 * 正式环境请设置为 false
 */
private val useMockData = true
```

- **`true`** - 使用 Mock 数据（本地模拟）
- **`false`** - 使用真实网络请求

## 🧪 测试账号

### 账号 1（管理员）
- **用户名**：`admin`
- **密码**：`123456`
- **说明**：管理员账号

### 账号 2（测试用户）
- **用户名**：`test`
- **密码**：`123456`
- **说明**：普通测试账号

### 错误测试
- 输入其他用户名/密码组合会提示"用户名或密码错误"

## 🎯 使用步骤

### 1. 启动应用
```bash
./gradlew installDebug
```

### 2. 测试登录
1. 打开应用，进入登录页面
2. 输入用户名：`admin`
3. 输入密码：`123456`
4. 点击登录按钮
5. 等待 1.5 秒（模拟网络延迟）
6. 看到"登录成功"提示

### 3. 查看日志
在 Logcat 中可以看到完整的登录流程：

```
D/DemoApp: [Track] PageView: LoginActivity, params: null
D/DemoApp: [Track] Event: click_login, params: {username=admin}
D/DemoApp: Login success: userId=1001, username=admin
D/DemoApp: [Track] Event: login_success, params: {userId=1001, username=admin}
```

## 🔄 切换到真实网络请求

### 方式一：修改代码
在 `LoginRepository.kt` 中修改：

```kotlin
private val useMockData = false  // 改为 false
```

### 方式二：配置真实 API

1. **修改 BaseUrl**（在 `app/build.gradle.kts`）
   ```kotlin
   buildConfigField("String", "BASE_URL", "\"https://your-api.com/\"")
   ```

2. **确保后端接口格式匹配**
   ```kotlin
   // 请求格式
   POST /auth/login
   {
       "username": "admin",
       "password": "123456"
   }
   
   // 响应格式
   {
       "code": 200,
       "message": "success",
       "data": {
           "userId": "1001",
           "token": "xxx",
           "username": "admin",
           "avatar": "xxx",
           "email": "xxx"
       }
   }
   ```

## 💡 Mock 数据的优势

1. **无需后端** - 可以独立开发前端
2. **快速测试** - 不依赖网络环境
3. **可控场景** - 可以模拟各种情况（成功、失败、超时等）
4. **便于演示** - 随时随地展示功能

## 🎨 自定义 Mock 数据

你可以在 `loginWithMockData()` 方法中自定义更多测试场景：

```kotlin
private fun loginWithMockData(username: String, password: String): Flow<LoginResponse> = flow {
    delay(1500)  // 模拟网络延迟
    
    when {
        // 场景1：成功登录
        username == "admin" && password == "123456" -> {
            emit(LoginResponse(...))
        }
        
        // 场景2：账号被禁用
        username == "banned" -> {
            throw ApiException(403, "账号已被禁用")
        }
        
        // 场景3：需要验证码
        username == "needcode" -> {
            throw ApiException(1001, "需要验证码")
        }
        
        // 场景4：密码错误次数过多
        username == "locked" -> {
            throw ApiException(1002, "密码错误次数过多，请30分钟后再试")
        }
        
        // 默认：密码错误
        else -> {
            throw ApiException(ApiException.CODE_SERVER_ERROR, "用户名或密码错误")
        }
    }
}.flowOn(Dispatchers.IO)
```

## 🚀 使用 Mock 服务器

如果需要更真实的网络环境，可以使用以下 Mock 服务器：

### 1. JSON Placeholder
```kotlin
buildConfigField("String", "BASE_URL", "\"https://jsonplaceholder.typicode.com/\"")
```

### 2. MockAPI.io
1. 访问 [MockAPI.io](https://mockapi.io/)
2. 创建项目并设置接口
3. 获取 API URL
4. 配置到 `BASE_URL`

### 3. Postman Mock Server
1. 在 Postman 中创建 Collection
2. 创建 Mock Server
3. 配置响应数据
4. 使用 Mock Server URL

### 4. 本地 Node.js Mock Server

创建 `mock-server.js`：

```javascript
const express = require('express');
const app = express();

app.use(express.json());

app.post('/auth/login', (req, res) => {
    const { username, password } = req.body;
    
    if (username === 'admin' && password === '123456') {
        res.json({
            code: 200,
            message: 'success',
            data: {
                userId: '1001',
                token: 'mock_token_' + Date.now(),
                username: username,
                avatar: 'https://via.placeholder.com/100',
                email: 'admin@example.com'
            }
        });
    } else {
        res.json({
            code: 400,
            message: '用户名或密码错误',
            data: null
        });
    }
});

app.listen(3000, () => {
    console.log('Mock server running on http://localhost:3000');
});
```

运行：
```bash
node mock-server.js
```

然后配置：
```kotlin
buildConfigField("String", "BASE_URL", "\"http://10.0.2.2:3000/\"")
// 注意：Android 模拟器访问本机使用 10.0.2.2
```

## 📋 测试检查清单

- [ ] 使用 admin/123456 登录成功
- [ ] 使用 test/123456 登录成功
- [ ] 输入错误密码提示失败
- [ ] 用户名为空提示"请输入用户名"
- [ ] 密码为空提示"请输入密码"
- [ ] 密码少于6位提示"密码长度不能少于6位"
- [ ] 登录过程显示 Loading
- [ ] 登录成功后 Token 被保存
- [ ] 查看 Logcat 确认埋点正常

## ❓ 常见问题

### Q1: 为什么需要 Mock 数据？
A: 因为示例项目没有配套的后端服务器，使用 Mock 数据可以让你立即测试功能。

### Q2: Mock 数据会影响生产环境吗？
A: 不会。只要在发布前将 `useMockData` 设置为 `false` 并配置真实的 API 地址即可。

### Q3: 可以模拟网络超时吗？
A: 可以，在 `loginWithMockData()` 中增加更长的 `delay()` 时间即可。

### Q4: 如何模拟网络错误？
A: 直接抛出 `ApiException`：
```kotlin
throw ApiException(ApiException.CODE_NETWORK_ERROR, "网络连接失败")
```

## 🎯 生产环境部署

在发布正式版本前，请确保：

1. ✅ 将 `useMockData` 设置为 `false`
2. ✅ 配置正确的生产环境 `BASE_URL`
3. ✅ 确认后端接口格式匹配
4. ✅ 测试真实网络请求
5. ✅ 启用混淆配置

---

**Happy Testing!** 🎉
