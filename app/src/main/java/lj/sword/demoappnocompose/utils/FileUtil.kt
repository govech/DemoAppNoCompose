package lj.sword.demoappnocompose.utils

import android.content.Context
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.text.DecimalFormat

/**
 * 文件工具类
 * 提供文件读写、缓存管理等功能
 * 
 * @author Sword
 * @since 1.0.0
 */
object FileUtil {

    /**
     * 获取缓存目录
     */
    @JvmStatic
    fun getCacheDir(context: Context): File {
        return context.cacheDir
    }

    /**
     * 获取外部缓存目录
     */
    @JvmStatic
    fun getExternalCacheDir(context: Context): File? {
        return context.externalCacheDir
    }

    /**
     * 获取应用文件目录
     */
    @JvmStatic
    fun getFilesDir(context: Context): File {
        return context.filesDir
    }

    /**
     * 创建文件
     */
    @JvmStatic
    fun createFile(path: String): File? {
        return try {
            val file = File(path)
            if (!file.exists()) {
                file.parentFile?.mkdirs()
                file.createNewFile()
            }
            file
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    /**
     * 删除文件
     */
    @JvmStatic
    fun deleteFile(path: String): Boolean {
        return try {
            val file = File(path)
            file.delete()
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    /**
     * 删除目录（包括子文件）
     */
    @JvmStatic
    fun deleteDirectory(directory: File): Boolean {
        return try {
            if (directory.isDirectory) {
                directory.listFiles()?.forEach { file ->
                    if (file.isDirectory) {
                        deleteDirectory(file)
                    } else {
                        file.delete()
                    }
                }
            }
            directory.delete()
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    /**
     * 复制文件
     */
    @JvmStatic
    fun copyFile(sourcePath: String, destPath: String): Boolean {
        return try {
            val sourceFile = File(sourcePath)
            val destFile = File(destPath)
            
            destFile.parentFile?.mkdirs()
            
            FileInputStream(sourceFile).use { input ->
                FileOutputStream(destFile).use { output ->
                    input.copyTo(output)
                }
            }
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    /**
     * 读取文件内容
     */
    @JvmStatic
    fun readFile(path: String): String? {
        return try {
            File(path).readText()
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    /**
     * 写入文件内容
     */
    @JvmStatic
    fun writeFile(path: String, content: String): Boolean {
        return try {
            val file = createFile(path) ?: return false
            file.writeText(content)
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    /**
     * 获取文件大小
     */
    @JvmStatic
    fun getFileSize(path: String): Long {
        return try {
            File(path).length()
        } catch (e: Exception) {
            0L
        }
    }

    /**
     * 格式化文件大小
     */
    @JvmStatic
    fun formatFileSize(size: Long): String {
        val df = DecimalFormat("#.##")
        return when {
            size < 1024 -> "$size B"
            size < 1024 * 1024 -> "${df.format(size / 1024.0)} KB"
            size < 1024 * 1024 * 1024 -> "${df.format(size / (1024.0 * 1024))} MB"
            else -> "${df.format(size / (1024.0 * 1024 * 1024))} GB"
        }
    }

    /**
     * 获取目录大小
     */
    @JvmStatic
    fun getDirectorySize(directory: File): Long {
        var size = 0L
        try {
            directory.listFiles()?.forEach { file ->
                size += if (file.isDirectory) {
                    getDirectorySize(file)
                } else {
                    file.length()
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return size
    }

    /**
     * 清空缓存目录
     */
    @JvmStatic
    fun clearCache(context: Context): Boolean {
        var result = true
        context.cacheDir?.let { result = result && deleteDirectory(it) }
        context.externalCacheDir?.let { result = result && deleteDirectory(it) }
        return result
    }

    /**
     * 获取缓存大小
     */
    @JvmStatic
    fun getCacheSize(context: Context): Long {
        var size = 0L
        context.cacheDir?.let { size += getDirectorySize(it) }
        context.externalCacheDir?.let { size += getDirectorySize(it) }
        return size
    }
}
