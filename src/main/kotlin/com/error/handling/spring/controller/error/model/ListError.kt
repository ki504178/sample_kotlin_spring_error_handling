package com.error.handling.spring.controller.error.model

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonInclude

/** List<T> の T のバリデーションエラーの内容 */
@JsonInclude(JsonInclude.Include.NON_NULL)
data class ListError @JsonCreator constructor(
    /** List<T> の T の順序 */
    val index: Int,
    /** ItemErrorと同じ */
    val type: String,
    /** ItemErrorと同じ */
    val message: String
)
