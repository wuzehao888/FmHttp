package com.wzh.fm_http.request

import com.wzh.fm_http.FmHttp
import com.wzh.fm_http.FmHttpRequest
import okhttp3.OkHttpClient
import okhttp3.Request
import java.net.URLEncoder

/**
 * @Description:    GET请求
 * @Author:         Wzh
 * @CreateDate:     2021/2/24 11:43
 */
class FmGetRequest(url: String, client: OkHttpClient = FmHttp.okHttpClient) :
    FmHttpRequest<FmGetRequest>(url, client) {

    override fun createOkHttpRequest(builder: Request.Builder): Request {
        return builder.get().url(createGetUrl(url)).build()
    }

    private fun createGetUrl(url: String): String {
        val sb = StringBuilder()
        sb.append(url)
        if (url.indexOf("?") > 0 || url.indexOf("&") > 0) {
            sb.append("&");
        } else {
            sb.append("?");
        }
        params.forEach { entry ->
            sb.append("${entry.key}=${URLEncoder.encode(entry.value.toString(), "UTF-8")}")
                .append("&")
        }
        sb.deleteCharAt(sb.length - 1)
        return sb.toString()
    }
}