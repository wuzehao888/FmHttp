package com.wzh.fm_http

import com.blankj.utilcode.util.LogUtils
import com.wzh.fm_http.callback.FmHttpResultCallback
import com.wzh.fm_http.domain.FmHttpResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.*
import java.io.IOException
import java.lang.reflect.ParameterizedType
import java.lang.reflect.Type

/**
 * @Description:    http request
 * @Author:         Wzh
 * @CreateDate:     2021/2/24 11:30
 */
abstract class FmHttpRequest<FR : FmHttpRequest<FR>>(
    protected val url: String,
    private val client: OkHttpClient = FmHttp.okHttpClient
) {
    //头信息
    private val headers by lazy { mutableMapOf<String, String>() }

    //参数
    protected val params by lazy { mutableMapOf<String, Any>() }

    //转换器
    private var convert: FmHttpConvert? = null

    //标记
    private var tag: String = ""

    /**
     * 添加标记
     */
    fun tag(tag: String): FR {
        this.tag = tag
        return this as FR
    }

    /**
     * 添加头信息
     */
    fun addHeader(name: String, value: String): FR {
        headers[name] = value
        return this as FR
    }

    /**
     * 添加参数
     */
    fun addParams(name: String, value: Any): FR {
        //允许传入的是String类型和基本数据类型
        if (value is String) {
            params[name] = value
        } else {
            try {
                val typeField = value.javaClass.getDeclaredField("TYPE")
                val clazz = typeField.get(null) as Class<*>
                if (clazz.isPrimitive) {
                    params[name] = value
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        return this as FR
    }

    /**
     * 设置转换器
     */
    fun convert(convert: FmHttpConvert): FR {
        this.convert = convert
        return this as FR
    }

    private fun okCall(): Call {
        val builder = Request.Builder()
        //添加头信息
        headers.forEach { entry ->
            builder.addHeader(entry.key, entry.value)
        }
        //添加标记
        if (tag.isNotEmpty()) {
            builder.tag(tag)
        }
        val request = createOkHttpRequest(builder)
        return client.newCall(request)
    }

    /**
     * 同步执行
     */
    suspend fun execute(): Response? = withContext(Dispatchers.IO) {
        try {
            okCall().execute()
        } catch (e: IOException) {
            e.printStackTrace()
            null
        }
    }

    /**
     * 异步执行
     */
    fun enqueue(callback: Callback) {
        try {
            okCall().enqueue(callback)
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    /**
     * 同步执行
     */
    suspend fun <T> execute(type: Type): FmHttpResult<T> = withContext(Dispatchers.IO) {
        try {
            val response = execute()
                ?: return@withContext FmHttpResult<T>(
                    false,
                    FmHttpResult.ERROR_CODE,
                    "网络异常",
                    null
                )
            return@withContext parseResponse<T>(response, type)
        } catch (e: IOException) {
            e.printStackTrace()
            return@withContext FmHttpResult<T>(
                false,
                FmHttpResult.ERROR_CODE,
                e.message ?: "",
                null
            )
        }
    }

    /**
     * 异步执行
     */
    fun <T> enqueue(callback: FmHttpResultCallback<T>) {
        enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                if (!call.isCanceled()) {
                    callback.response(
                        FmHttpResult(
                            false,
                            FmHttpResult.ERROR_CODE,
                            e.message ?: "",
                            null
                        )
                    )
                }
            }

            override fun onResponse(call: Call, response: Response) {
                try {
                    val pt = callback::class.java.genericSuperclass as ParameterizedType
                    val type = pt.actualTypeArguments[0]
                    val result = parseResponse<T>(response, type)
                    callback.response(result)
                } catch (e: Exception) {
                    e.printStackTrace()
                    callback.response(
                        FmHttpResult(
                            false,
                            FmHttpResult.ERROR_CODE,
                            e.message ?: "",
                            null
                        )
                    )
                }
            }
        })
    }

    /**
     * 解析响应
     */
    private fun <T> parseResponse(response: Response, type: Type): FmHttpResult<T> {
        var success = response.isSuccessful
        val code = response.code
        var msg = response.message
        var body: T? = null
        if (success) {
            response.body?.let { responseBody ->
                val content = responseBody.string()
                if (content.isNotEmpty()) {
                    //转换器
                    if (convert == null) {
                        success = false
                        msg = "需要设置转换器convert()"
                    } else {
                        body = convert!!.convert(content, type)
                    }
                }
            }
        }
        response.close()
        val fmHttpResult = FmHttpResult(success, code, msg, body)
        LogUtils.iTag("okhttp", fmHttpResult)
        return fmHttpResult
    }

    /**
     * 创建请求
     */
    protected abstract fun createOkHttpRequest(builder: Request.Builder): Request
}