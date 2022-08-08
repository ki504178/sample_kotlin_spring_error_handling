package com.error.handling.spring.controller.error

import com.error.handling.spring.controller.error.model.*
import org.springframework.context.MessageSource
import org.springframework.validation.BindException
import org.springframework.validation.FieldError
import java.util.Locale

/** リクエストボディでバリデーションエラーが発生した場合に利用するハンドラ */
class FormErrorHandler {
    companion object {
        private val MESSAGE_LOCALE = Locale.getDefault()

        /** リクエストボディのフィールドネスト数上限 */
        private const val REQ_BODY_NESTED_SIZE_OVER = 2

        // FieldErrorパース用
        private const val LEFT_BRACKET = '['
        private const val RIGHT_BRACKET = ']'
        private const val DOT = '.'
    }

    /**
     * リクエストボディのエラーハンドラ
     * BindExceptionはFormバリデーションエラーとし、FieldErrorを各項目のエラーとして抽出する
     */
    fun handleFormException(ex: BindException, messageSource: MessageSource): MutableList<InvalidError> {
        val nestedOverError = nestedOverErrors(ex.fieldErrors)
        if (nestedOverError != null) return mutableListOf(nestedOverError)

        val invalidErrors: MutableList<InvalidError> = mutableListOf()
        ex.fieldErrors
            .sortedBy { error -> error.field }
            .forEach { error ->
                val fieldFullName = fieldFullName(error.field)
                val fieldSplitList = fieldSplitList(fieldFullName)

                val field = field(fieldSplitList)
                val type = InvalidErrorTypeEnum.type(error.code ?: "")
                val message = messageSource.getMessage(error, MESSAGE_LOCALE)

                val invalidError = invalidError(fieldFullName, invalidErrors)
                val listError = listError(field, type, message)

                val fieldExcludedIndex = fieldExcludeIndex(field, listError)
                val addedItemError =
                    invalidError.itemErrors.firstOrNull { err -> err.name == fieldExcludedIndex }

                if (null == addedItemError) {
                    val itemError = itemError(fieldExcludedIndex, type, message, listError)
                    invalidError.itemErrors.add(itemError)
                } else {
                    if (null == listError) {
                        invalidError.itemErrors.add(ItemError(fieldExcludedIndex, type, message))
                    } else {
                        if (null == addedItemError.listErrors) {
                            addedItemError.listErrors = mutableListOf(listError)
                        } else {
                            addedItemError.listErrors!!.add(listError)
                        }
                    }
                }

                if (!invalidErrors.any { err -> err.index == invalidError.index }) {
                    invalidErrors.add(invalidError)
                }
            }

        return invalidErrors
    }

    /** リクエストボディのネスト数上限をオーバーしているフィールドをバリデーションエラーとして返す */
    private fun nestedOverErrors(fieldErrors: List<FieldError>): InvalidError? {
        val nestedOverItemErrors = fieldErrors.map { err -> fieldFullName((err.field)) }
            .filter { fieldFullName ->
                fieldSplitList(fieldFullName).size > REQ_BODY_NESTED_SIZE_OVER
            }.map { fieldFullName ->
                ItemError(fieldFullName, InvalidErrorTypeEnum.ReqBodyNestedOver.name)
            }
        if (nestedOverItemErrors.isEmpty()) return null

        return InvalidError(0, nestedOverItemErrors.toMutableList())
    }

    /** FieldError.fieldからValidListを利用した場合に設定されるフィールド名を除外する　*/
    private fun fieldFullName(field: String) = if (field.startsWith(ValidList.WRAP_LIST_FIELDS_NAME)) {
        field.substringAfter(ValidList.WRAP_LIST_FIELDS_NAME)
    } else field

    /** フルフィールド名から先頭の []. を除き、 . で分割したリストを取得 */
    private fun fieldSplitList(fieldFullName: String): List<String> {
        val fullNameExcludedIndex = if (fieldFullName.startsWith(LEFT_BRACKET)) {
            fieldFullName.substringAfter(DOT)
        } else fieldFullName

        return fullNameExcludedIndex.split(DOT)
    }

    /** 分割したフィールド名をフォーマット */
    private fun field(fieldSplitList: List<String>) = if (fieldSplitList.size == REQ_BODY_NESTED_SIZE_OVER) {
        "${fieldSplitList.first()}.${fieldSplitList.last()}"
    } else fieldSplitList.first()

    /** ValidList<T>のTでエラーの場合は、フィールド名から [] を除外 */
    private fun fieldExcludeIndex(field: String, listError: ListError?) = if (null == listError) field
    else field.substringBefore(LEFT_BRACKET)

    /** ItemErrorを返す */
    private fun itemError(field: String, type: String, message: String, listError: ListError?) =
        if (null == listError) ItemError(field, type, message)
        else ItemError(field, listErrors = mutableListOf(listError))

    /** List<T>のTに対するエラーの場合はListErrorとして返す。違うならnull */
    private fun listError(field: String, type: String, message: String): ListError? {
        if (!(field.contains(LEFT_BRACKET) && field.contains(RIGHT_BRACKET))) return null

        val listFieldIndex = field.substringAfter(LEFT_BRACKET).substringBefore(RIGHT_BRACKET)
            .toIntOrNull() ?: return null

        return ListError(listFieldIndex, type, message)
    }

    /** フィールドごとのInvalidErrorを返す */
    private fun invalidError(fieldFullName: String, invalidErrors: MutableList<InvalidError>): InvalidError {
        // ValidList<Form>をパラメータとしている場合、[n].フィールド名 となるため、n をindexとして設定する
        val indexStr = if (fieldFullName.startsWith(LEFT_BRACKET)) fieldFullName.substring(1, 2)
        else ""
        val index = indexStr.toIntOrNull() ?: 0

        return invalidErrors.firstOrNull { err -> err.index == index }
            ?: InvalidError(index, mutableListOf())
    }
}
