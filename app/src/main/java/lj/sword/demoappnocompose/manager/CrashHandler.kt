package lj.sword.demoappnocompose.manager

import android.content.Context
import lj.sword.demoappnocompose.config.AppConfig
import lj.sword.demoappnocompose.utils.DateUtil
import lj.sword.demoappnocompose.utils.FileUtil
import java.io.File
import kotlin.system.exitProcess

/**
 * 全局异常捕获处理器
 * 捕获未处理的异常并保存日志
 * 
 * @author Sword
 * @since 1.0.0
 */
class CrashHandler private constructor() : Thread.UncaughtExceptionHandler {

    private var context: Context? = null
    private var defaultHandler: Thread.UncaughtExceptionHandler? = null

    companion object {
        @Volatile
        private var instance: CrashHandler? = null

        @JvmStatic
        fun getInstance(): CrashHandler {
            return instance ?: synchronized(this) {
                instance ?: CrashHandler().also { instance = it }
            }
        }
    }

    /**
     * 初始化
     */
    fun init(context: Context) {
        this.context = context.applicationContext
        defaultHandler = Thread.getDefaultUncaughtExceptionHandler()
        Thread.setDefaultUncaughtExceptionHandler(this)
    }

    override fun uncaughtException(thread: Thread, throwable: Throwable) {
        try {
            val config = AppConfig.getInstance()
            
            // 根据配置决定是否收集崩溃信息
            if (config.enableCrashCollection) {
                // 保存崩溃日志
                saveCrashLog(throwable)
                
                // 记录日志
                Logger.e("App Crashed", throwable)
                
                // 这里可以上报崩溃信息到服务器
            }
            // uploadCrashLog(throwable)
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            // 调用系统默认的异常处理器
            defaultHandler?.uncaughtException(thread, throwable)
            
            // 退出应用
            android.os.Process.killProcess(android.os.Process.myPid())
            exitProcess(1)
        }
    }

    /**
     * 保存崩溃日志到文件
     */
    private fun saveCrashLog(throwable: Throwable) {
        try {
            val ctx = context ?: return
            
            val crashDir = File(ctx.cacheDir, "crash")
            if (!crashDir.exists()) {
                crashDir.mkdirs()
            }
            
            val timestamp = System.currentTimeMillis()
            val fileName = "crash_${DateUtil.formatTimestamp(timestamp, "yyyyMMdd_HHmmss")}.log"
            val crashFile = File(crashDir, fileName)
            
            val logContent = buildString {
                appendLine("==================== Crash Log ====================")
                appendLine("Time: ${DateUtil.formatTimestamp(timestamp)}")
                appendLine("Thread: ${Thread.currentThread().name}")
                appendLine("Exception: ${throwable.javaClass.name}")
                appendLine("Message: ${throwable.message}")
                appendLine("\nStack Trace:")
                appendLine(throwable.stackTraceToString())
                appendLine("==================================================")
            }
            
            FileUtil.writeFile(crashFile.absolutePath, logContent)
            
            Logger.d("Crash log saved: ${crashFile.absolutePath}")
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    /**
     * 获取崩溃日志文件列表
     */
    fun getCrashLogFiles(): List<File> {
        val ctx = context ?: return emptyList()
        val crashDir = File(ctx.cacheDir, "crash")
        return crashDir.listFiles()?.toList() ?: emptyList()
    }

    /**
     * 清除崩溃日志
     */
    fun clearCrashLogs() {
        val ctx = context ?: return
        val crashDir = File(ctx.cacheDir, "crash")
        FileUtil.deleteDirectory(crashDir)
    }
}
