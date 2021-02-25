package com.wzh.fm_http.domain

/**
 * @Description:    Http请求结果
 * @Author:         Wzh
 * @CreateDate:     2021/2/24 16:13
 */
data class FmHttpResult<T>(val success: Boolean, val code: Int, val msg: String, val body: T?){
    companion object{
        const val ERROR_CODE = 500
    }
}