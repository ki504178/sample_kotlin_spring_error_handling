package com.error.handling.spring.controller.error.mock

import com.error.handling.spring.controller.error.model.ValidList
import org.hibernate.validator.constraints.Length
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.*
import javax.validation.Valid
import javax.validation.constraints.Email
import javax.validation.constraints.Max
import javax.validation.constraints.NotBlank

@RestController
@RequestMapping("/test/param_error_test")
@Validated
internal class MockParameterErrorController {
    /** パス検証用 */
    @GetMapping
    @RequestMapping("/path/{id}")
    fun path(
        @PathVariable @Length(max = 1) id: String
    ) = println(id)

    /** クエリ検証用 */
    @GetMapping
    fun query(
        @RequestParam @Email mail: String
    ) = println(mail)

    /** パス・クエリ検証用 */
    @PutMapping
    @RequestMapping("/path_query/{id}")
    fun pathAndQuery(
        @PathVariable @Length(max = 1) id: String,
        @RequestParam @NotBlank hoge: String,
        @RequestParam @Max(3) fuga: Int
    ) = println(id)
}
