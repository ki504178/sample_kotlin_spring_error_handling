package com.error.handling.spring.controller.error.model

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonInclude

/** 項目ごとのバリデーションエラーの内容 */
@JsonInclude(JsonInclude.Include.NON_NULL)
data class ItemError @JsonCreator constructor(
    /** 名称 */
    val name: String,
    /** エラータイプ */
    val type: String? = null,
    /** エラーメッセージ */
    val message: String? = null,
    /** List<T>の場合の T 要素のバリデーションエラー内容 */
    var listErrors: MutableList<ListError>? = null
)
