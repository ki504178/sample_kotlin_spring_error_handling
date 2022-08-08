package com.error.handling.spring.controller.error.mock

import com.error.handling.spring.exception.SampleException
import org.hibernate.validator.constraints.Length
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import javax.validation.constraints.Max
import javax.validation.constraints.NotBlank
import kotlin.math.max

@RestController
@RequestMapping("/test")
@Validated
class MockErrorRestController {
    /** HandleableError検証用 */
    @GetMapping
    fun handleable(): Nothing = throw SampleException("検証用")

    /** SystemException検証用 */
    @PostMapping
    fun system(): Nothing = throw IllegalArgumentException("検証用")

    /** Path/Query/RequestBody同時にバリデーションエラーした場合の検証用 */
    @PutMapping
    @RequestMapping("/{id}")
    fun pathQueryBody(
        @PathVariable
        @Length(max = 1)
        id: String,
        @RequestParam
        @Max(1)
        num: Int,
        @Validated
        @RequestBody
        form: MockErrorRestControllerForm
    ) = println()
}

data class MockErrorRestControllerForm(
    @field: NotBlank
    val mockId: String
)
