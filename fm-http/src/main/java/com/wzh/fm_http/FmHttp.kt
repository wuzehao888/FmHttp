package com.wzh.fm_http

import com.wzh.fm_http.request.FmGetRequest
import com.wzh.fm_http.request.FmPostFormRequest
import com.wzh.fm_http.request.FmPostJsonRequest
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import java.util.concurrent.TimeUnit

/**
 * @Description:    Http
 * @Author:         Wzh
 * @CreateDate:     2021/2/24 11:19
 */
object FmHttp {
    private const val TIME_OUT = 10L

    val okHttpClient: OkHttpClient

    private var baseUrl: String = ""

    init {
        val loggingInterceptor = HttpLoggingInterceptor()
        loggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY)
        okHttpClient = OkHttpClient.Builder()
            .readTimeout(TIME_OUT, TimeUnit.SECONDS)
            .connectTimeout(TIME_OUT, TimeUnit.SECONDS)
            .writeTimeout(TIME_OUT, TimeUnit.SECONDS)
            .addInterceptor(loggingInterceptor)
            .build()
    }

    /**
     * 初始化
     */
    fun init(baseUrl: String) {
        this.baseUrl = baseUrl
    }

    private fun getHttpUrl(url: String): String {
        if (baseUrl.isEmpty()) return url
        if (url.startsWith("http")) return url
        return "${baseUrl}${url}"
    }

    /**
     * get请求
     */
    fun doGet(url: String): FmGetRequest {
        return FmGetRequest(getHttpUrl(url))
    }

    /**
     * post提交表单
     */
    fun doPostForm(url: String): FmPostFormRequest {
        return FmPostFormRequest(getHttpUrl(url))
    }

    /**
     * post提交json
     */
    fun <T> doPostJson(url: String, t: T): FmPostJsonRequest<T> {
        return FmPostJsonRequest(url, t)
    }

    /**
     * 取消全部
     */
    fun cancelAll() {
        okHttpClient.dispatcher.queuedCalls().forEach { call ->
            call.cancel()
        }

        okHttpClient.dispatcher.runningCalls().forEach { call ->
            call.cancel()
        }
    }

    /**
     * 根据tag取消请求
     */
    fun cancelByTag(tag: String) {
        okHttpClient.dispatcher.queuedCalls().forEach { call ->
            val requestTag = (call.request().tag() as? String) ?: ""
            if (requestTag.isNotEmpty() && tag == requestTag) {
                call.cancel()
            }
        }

        okHttpClient.dispatcher.runningCalls().forEach { call ->
            val requestTag = (call.request().tag() as? String) ?: ""
            if (requestTag.isNotEmpty() && tag == requestTag) {
                call.cancel()
            }
        }
    }
}