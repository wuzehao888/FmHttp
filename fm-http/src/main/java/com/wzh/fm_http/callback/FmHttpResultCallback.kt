package com.wzh.fm_http.callback

import com.wzh.fm_http.domain.FmHttpResult

/**
 * @Description:    http请求回调
 * @Author:         Wzh
 * @CreateDate:     2021/2/24 17:01
 */
abstract class FmHttpResultCallback<T> {
    abstract fun response(result: FmHttpResult<T>)
}