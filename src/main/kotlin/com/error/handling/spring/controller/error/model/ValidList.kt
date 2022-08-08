package com.error.handling.spring.controller.error.model

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonValue
import javax.validation.Valid

/**
 * Controllerのメソッド引数でList<Form>として受け取るためのラッパー
 * List<Form>で受け取るとバリデーションが効かないため、左記の形式で受け取る場合はこれを利用する
 */
class ValidList<E> @JsonCreator constructor(vararg args: E) {
    companion object {
        const val WRAP_LIST_FIELDS_NAME = "wrapListFields"
    }

    @JsonValue
    @Valid
    var wrapListFields: List<E>? = null
    val getWrapListFields: List<E>?
        get() = wrapListFields

    init {
        this.wrapListFields = args.asList()
    }
}
