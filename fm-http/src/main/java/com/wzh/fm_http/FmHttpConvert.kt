package com.wzh.fm_http

import java.lang.reflect.Type

/**
 * @Description:    http数据转换器
 * @Author:         Wzh
 * @CreateDate:     2021/2/24 16:29
 */
interface FmHttpConvert {
    fun <T> convert(content: String, type: Type): T?
}