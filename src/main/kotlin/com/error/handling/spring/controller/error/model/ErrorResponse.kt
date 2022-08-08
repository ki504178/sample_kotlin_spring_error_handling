package com.error.handling.spring.controller.error.model

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonInclude
import java.util.UUID

/** エラーの共通レスポンス */
@JsonInclude(JsonInclude.Include.NON_NULL)
data class ErrorResponse @JsonCreator constructor(
    /** レスポンスごとに一意な文字列が設定される。 */
    val responseId: String = UUID.randomUUID().toString(),

    /** エラー名称。バリデーションエラー時は「InvalidError」が設定される */
    val errorCause: String,

    /** エラーメッセージ。バリデーションエラー時は設定されない */
    val message: String? = null,

    /** パス・クエリパラメータ、リクエストボディでバリデーションエラーが発生した場合に設定される、レコードごとのエラー詳細リスト。
     * リクエストボディが優先され、左記でエラーの場合はパス・クエリパラメータはチェックされない */
    val invalidErrors: MutableList<InvalidError>? = null
)
