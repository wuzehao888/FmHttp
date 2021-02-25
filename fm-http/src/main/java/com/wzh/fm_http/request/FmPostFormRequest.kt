package com.wzh.fm_http.request

import com.wzh.fm_http.FmHttpRequest
import okhttp3.FormBody
import okhttp3.Request
import java.net.URLEncoder

/**
 * @Description:    POST FORM请求
 * @Author:         Wzh
 * @CreateDate:     2021/2/24 14:01
 */
class FmPostFormRequest(url: String) : FmHttpRequest<FmPostFormRequest>(url) {
    override fun createOkHttpRequest(builder: Request.Builder): Request {
        val formBuilder = FormBody.Builder()
        params.forEach { entry ->
            formBuilder.addEncoded(entry.key, URLEncoder.encode(entry.value.toString(), "UTF-8"))
        }
        val formBody = formBuilder.build()
        return builder.url(url).post(formBody).build()
    }
}