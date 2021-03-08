package com.wzh.fm_http

import com.wzh.fm_http.request.FmGetRequest
import com.wzh.fm_http.request.FmPostFormRequest
import com.wzh.fm_http.request.FmPostJsonRequest
import okhttp3.Call
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import java.util.concurrent.TimeUnit
import javax.net.ssl.X509TrustManager

/**
 * @Description:    Http
 * @Author:         Wzh
 * @CreateDate:     2021/2/24 11:19
 */
object FmHttp {
    private const val TIME_OUT = 10L

    val okHttpClient: OkHttpClient by lazy {
        val loggingInterceptor = HttpLoggingInterceptor()
        loggingInterceptor.setLevel(if (isOpenLog) HttpLoggingInterceptor.Level.BODY else HttpLoggingInterceptor.Level.NONE)
        OkHttpClient.Builder()
            .readTimeout(TIME_OUT, TimeUnit.SECONDS)
            .connectTimeout(TIME_OUT, TimeUnit.SECONDS)
            .writeTimeout(TIME_OUT, TimeUnit.SECONDS)
            .addInterceptor(loggingInterceptor)
            .sslSocketFactory(
                SSLSocketClient.getSSLSocketFactory(),
                SSLSocketClient.getTrustManager()[0] as X509TrustManager
            )
            .hostnameVerifier(SSLSocketClient.getHostnameVerifier())
            .build()
    }

    private val downloadClient by lazy {
        val loggingInterceptor = HttpLoggingInterceptor()
        loggingInterceptor.setLevel(if (isOpenLog) HttpLoggingInterceptor.Level.BASIC else HttpLoggingInterceptor.Level.NONE)
        OkHttpClient.Builder()
            .readTimeout(TIME_OUT, TimeUnit.SECONDS)
            .connectTimeout(TIME_OUT, TimeUnit.SECONDS)
            .writeTimeout(TIME_OUT, TimeUnit.SECONDS)
            .addInterceptor(loggingInterceptor)
            .sslSocketFactory(
                SSLSocketClient.getSSLSocketFactory(),
                SSLSocketClient.getTrustManager()[0] as X509TrustManager
            )
            .hostnameVerifier(SSLSocketClient.getHostnameVerifier())
            .build()
    }

    private var baseUrl: String = ""

    //是否打开日志
    private var isOpenLog = true

    /**
     * 初始化
     */
    fun init(isOpenLog: Boolean = true, baseUrl: String = "") {
        this.isOpenLog = isOpenLog
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
     * 下载
     */
    fun doDownload(url: String): FmGetRequest {
        return FmGetRequest(getHttpUrl(url), downloadClient)
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
        okHttpClient.dispatcher.cancelAll()
        downloadClient.dispatcher.cancelAll()
    }

    /**
     * 根据tag取消请求
     */
    fun cancelByTag(tag: String) {
        okHttpClient.dispatcher.queuedCalls().forEach { call ->
            cancel(call, tag)
        }

        okHttpClient.dispatcher.runningCalls().forEach { call ->
            cancel(call, tag)
        }

        downloadClient.dispatcher.queuedCalls().forEach { call ->
            cancel(call, tag)
        }

        downloadClient.dispatcher.runningCalls().forEach { call ->
            cancel(call, tag)
        }
    }

    private fun cancel(call: Call, tag: String) {
        val requestTag = (call.request().tag() as? String) ?: ""
        if (requestTag.isNotEmpty() && tag == requestTag) {
            call.cancel()
        }
    }
}