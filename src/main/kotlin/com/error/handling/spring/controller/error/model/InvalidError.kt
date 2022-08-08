package com.error.handling.spring.controller.error.model

import com.fasterxml.jackson.annotation.JsonCreator

/** バリデーションエラーの内容 */
data class InvalidError @JsonCreator constructor(
    /** リクエストボディの配列順序。配列オブジェクトとしてパラメータが設定されていない場合は 0 固定 */
    val index: Int,
    /** 各項目のエラー内容リスト */
    val itemErrors: MutableList<ItemError>
)
