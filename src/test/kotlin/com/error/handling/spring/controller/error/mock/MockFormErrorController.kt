package com.error.handling.spring.controller.error.mock

import com.error.handling.spring.controller.error.model.ValidList
import org.hibernate.validator.constraints.Length
import org.springframework.http.HttpStatus
import org.springframework.lang.NonNull
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController
import javax.validation.Valid
import javax.validation.constraints.Email
import javax.validation.constraints.Max
import javax.validation.constraints.NotBlank
import javax.validation.constraints.NotNull
import javax.validation.constraints.Size

@RestController
@RequestMapping("/test/form_error_test")
internal class MockFormErrorController {
    /** 単一オブジェクトのリクエストボディ検証用 */
    @PostMapping
    @ResponseStatus(value = HttpStatus.CREATED)
    fun register(
        @Validated
        @RequestBody
        form: MockFormErrorForm
    ) = println(form)

    /** 複数オブジェクトのリクエストボディ検証用 */
    @PostMapping("/registers")
    @ResponseStatus(value = HttpStatus.CREATED)
    fun registers(
        @Validated
        @RequestBody
        forms: ValidList<@Valid MockFormErrorForm>
    ) = println(forms)

    /** フィールドのネスト数上限をオーバーしたリクエストボディ検証用 */
    @PutMapping
    @ResponseStatus(value = HttpStatus.CREATED)
    fun nestedOver(
        @Validated
        @RequestBody
        form: MockFormErrorNestedOverForm
    ) = println(form)
}

data class MockFormErrorForm(
    @field: NotBlank
    @field: Length(min = 1, max = 10)
    val name: String,

    @field: Email
    val email: String,

    @field: Valid // TODO: なぜか List 内部要素のバリデーションが効かない
    @field: Size(min = 1, max = 3)
    val hogeList: List<@Max(3) Int>,

    @field: Valid
    @field: NonNull
    val nest: Nest
)

data class Nest(
    @field: NotNull
    @field: Max(4)
    val id: Number,

    @field: NotBlank
    val value: String,

    @field: Valid // TODO: なぜか List 内部要素のバリデーションが効かない
    @field: Size(max = 2)
    val list: List<@NotBlank String>
)

@Validated
data class MockFormErrorNestedOverForm(
    @field: Valid
    @field: NotNull
    val nestedOver: NestedOver
)

data class NestedOver(
    @field: Valid
    @field: NotNull
    val nest: Nest
)
