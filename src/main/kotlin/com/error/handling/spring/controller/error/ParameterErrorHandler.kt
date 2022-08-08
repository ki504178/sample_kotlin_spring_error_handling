package com.error.handling.spring.controller.error

import com.error.handling.spring.controller.error.model.InvalidError
import com.error.handling.spring.controller.error.model.InvalidErrorTypeEnum
import com.error.handling.spring.controller.error.model.ItemError
import org.springframework.web.bind.MissingServletRequestParameterException
import javax.validation.ConstraintViolationException

/** パス・クエリパラメータのエラーハンドラ */
class ParameterErrorHandler {
    /**
     * パス・クエリパラメータでバリデーションエラーとなった場合に利用するハンドラ
     * 前提として、パラメータはプリミティブ型であること
     */
    fun handleParameterException(ex: ConstraintViolationException) = InvalidError(
        0,
        ex.constraintViolations.map { err ->
            ItemError(
                lastDotAfter(err.propertyPath.toString()),
                InvalidErrorTypeEnum.type(err.constraintDescriptor.annotation.annotationClass.simpleName ?: "Unknown"),
                err.message
            )
        }.toMutableList()
    )

    /**
     * 必須のクエリパラメータが存在しない場合に利用するハンドラ
     * 前提として、パラメータはプリミティブ型であること
     */
    fun handleParameterException(ex: MissingServletRequestParameterException) = InvalidError(
        0,
        mutableListOf(
            ItemError(
                ex.parameterName,
                InvalidErrorTypeEnum.Required.name,
                ex.localizedMessage
            )
        )
    )

    /** 最後の . から先の文字列を取得 */
    private fun lastDotAfter(value: String) = value.substring(value.lastIndexOf(".") + 1)
}
