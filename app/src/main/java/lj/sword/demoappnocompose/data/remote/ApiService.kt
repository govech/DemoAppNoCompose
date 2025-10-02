package lj.sword.demoappnocompose.data.remote

import lj.sword.demoappnocompose.data.model.BaseResponse
import lj.sword.demoappnocompose.data.model.LoginRequest
import lj.sword.demoappnocompose.data.model.LoginResponse
import retrofit2.http.Body
import retrofit2.http.POST

/**
 * API 接口定义
 * 定义所有的网络请求接口
 * 
 * @author Sword
 * @since 1.0.0
 */
interface ApiService {
    
    /**
     * 用户登录
     * @param request 登录请求参数
     * @return 登录响应数据
     */
    @POST("auth/login")
    suspend fun login(@Body request: LoginRequest): BaseResponse<LoginResponse>
    
    /**
     * 更新用户信息
     * @param user 用户信息
     * @return 更新后的用户信息
     */
    @POST("user/update")
    suspend fun updateUser(@Body user: LoginResponse): BaseResponse<LoginResponse>
    
    /**
     * 获取用户信息
     * @return 用户信息
     */
    @POST("user/info")
    suspend fun getUserInfo(): BaseResponse<LoginResponse>
    
    // 更多 API 接口方法示例（将根据实际业务添加）
    // @GET("news/list")
    // suspend fun getNewsList(
    //     @Query("page") page: Int,
    //     @Query("size") size: Int
    // ): BaseResponse<List<News>>
}
