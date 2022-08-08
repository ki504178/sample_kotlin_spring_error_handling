package com.error.handling.spring.controller.error.model

/**
 * Spring, または独自定義で、Controllerでバリデーションとして利用しているアノテーションの名前（エラータイプ）一覧
 */
enum class InvalidErrorTypeEnum {
    ReqBodyNestedOver,
    Required,
    NotBlank,
    Length,
    Size,
    Max,
    Email
    ;

    companion object {
        fun type(fieldErrorCode: String) = values().firstOrNull { err -> err.name == fieldErrorCode }
            ?.name ?: throw java.lang.IllegalArgumentException("バリデーションエラータイプとして未定義: $fieldErrorCode")
    }
}
