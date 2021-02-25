package com.wzh.fm_http.request

import com.blankj.utilcode.util.GsonUtils
import com.wzh.fm_http.FmHttpRequest
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody

/**
 * @Description:    POST JSON请求
 * @Author:         Wzh
 * @CreateDate:     2021/2/24 14:07
 */
class FmPostJsonRequest<T>(url: String, private val t: T) :
    FmHttpRequest<FmPostJsonRequest<T>>(url) {
    override fun createOkHttpRequest(builder: Request.Builder): Request {
        val json = GsonUtils.toJson(t)
        val jsonRequestBody =
            json.toRequestBody("application/json; charset=utf-8".toMediaTypeOrNull())
        return builder.url(url).post(jsonRequestBody).build()
    }
}