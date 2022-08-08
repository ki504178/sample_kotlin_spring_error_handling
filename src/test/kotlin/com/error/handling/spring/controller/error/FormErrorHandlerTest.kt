package com.error.handling.spring.controller.error

import com.error.handling.spring.controller.error.mock.MockFormErrorForm
import com.error.handling.spring.controller.error.mock.MockFormErrorNestedOverForm
import com.error.handling.spring.controller.error.mock.Nest
import com.error.handling.spring.controller.error.mock.NestedOver
import com.error.handling.spring.controller.error.model.ErrorResponse
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import io.kotest.core.spec.style.FunSpec
import io.kotest.core.spec.style.StringSpec
import io.kotest.inspectors.forExactly
import io.kotest.matchers.should
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertAll
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.web.client.TestRestTemplate
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpMethod
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.test.context.junit.jupiter.SpringExtension
import java.net.URI

@Suppress("NonAsciiCharacters")
@ExtendWith(SpringExtension::class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
internal class FormErrorHandlerTest(
    @Autowired
    val restTemplate: TestRestTemplate
) : FunSpec() {
    companion object {
        private const val INVALID_ERROR = "InvalidError"
        private const val ROOT_URI = "/test/form_error_test"
    }

    // data class でJackson使うための設定
    val mapper = ObjectMapper().registerKotlinModule()

    init {
        test("正常なレスポンスの場合はエラーレスポンスが返らないこと") {
            val nest = Nest(3, "1", listOf("a", "1"))
            val form = MockFormErrorForm(
                "鈴木　一郎", "example@example.com", listOf(1), nest
            )
            val ret = restRequest(ROOT_URI, HttpMethod.POST, mapper.writeValueAsString(form))

            ret.statusCode shouldBe HttpStatus.CREATED
            ret.body shouldBe null
        }

        test("RequestBodyのフィールドネスト数上限を超えていた場合、ネスト数オーバーとしてバリデーションエラーとなること") {
            val nest = Nest(5, "", listOf("a", "1", "3"))
            val nestedOver = NestedOver(nest)
            val form = MockFormErrorNestedOverForm(nestedOver)
            val ret = restRequest(ROOT_URI, HttpMethod.PUT, mapper.writeValueAsString(form))

            val body = commonAssertion(ret)
            body.invalidErrors should {
                it shouldNotBe null
                it!!.size shouldBe 1
            }

            val invalidError = body.invalidErrors!!.first()
            invalidError.index shouldBe 0

            val itemErrors = invalidError.itemErrors
            itemErrors.size shouldBe 3

            itemErrors.forExactly(3) {
                it.type shouldBe "ReqBodyNestedOver"
            }
        }

        // TODO: なぜかList<T>のT要素のバリデーションが効かないため一旦保留して落ちるテストケースのままとする
        test("単一オブジェクトでエラーとなった場合、InvalidError 1件でエラー内容がパラメータに指定した通りである") {
            val nest = Nest(5, "", listOf("a", ""))
            val form = MockFormErrorForm(
                "  ", "not_mail_address", listOf(1, 4, 4, 3), nest
            )
            val ret = restRequest(ROOT_URI, HttpMethod.POST, mapper.writeValueAsString(form))

            val body = commonAssertion(ret)
            body.invalidErrors should {
                it shouldNotBe null
                it!!.size shouldBe 1
            }

            val invalidError = body.invalidErrors!!.first()
            invalidError.index shouldBe 0

            val itemErrors = invalidError.itemErrors
            itemErrors.size shouldBe 6

            itemErrors.filter { err -> err.name == "name" } should {
                it.size shouldBe 1
                it.firstOrNull { err -> err.type == "NotBlank" } shouldNotBe null
            }

            itemErrors.filter { err -> err.name == "email" } should {
                it.size shouldBe 1
                it.firstOrNull { err -> err.type == "Email" } shouldNotBe null
            }

            itemErrors.filter { err -> err.name == "hogeList" } should {
                it shouldNotBe null
                it.size shouldBe 1
                it.first().type shouldBe null
                it.first().name shouldBe null
                it.firstOrNull { err -> err.type == "Size" } shouldNotBe null
                it.first().listErrors!! should { listErrors ->
                    listErrors.size shouldBe 2
                    listErrors.firstOrNull { err ->
                        err.index == 1 && err.type == "Max"
                    } shouldNotBe null
                    listErrors.firstOrNull { err ->
                        err.index == 2 && err.type == "Max"
                    } shouldNotBe null
                }
            }

            itemErrors.filter { err -> err.name.startsWith("nest") } should {
                it shouldNotBe null
                it.size shouldBe 3
                it.firstOrNull { err -> err.name == "nest.id" && err.type == "Max" } shouldNotBe null
                it.firstOrNull { err -> err.name == "nest.value" && err.type == "NotBlank" } shouldNotBe null
                it.filter { err -> err.name == "nest.list" } should { nestList ->
                    nestList shouldNotBe null
                    nestList.size shouldBe 1
                    nestList.first().listErrors shouldNotBe null
                    nestList.first().listErrors!! should { listErrors ->
                        listErrors.size shouldBe 1
                        listErrors.firstOrNull { err ->
                            err.index == 1 && err.type == "NotBlank"
                        } shouldNotBe null
                    }
                }
            }
        }

        // TODO: ↑のTODO解消後に配列オブジェクトのテストケースを実装する
    }

    /** APIリクエスト */
    private fun restRequest(uri: String, method: HttpMethod, formJson: String?): ResponseEntity<String> {
        val url = URI(uri)
        val headers = HttpHeaders()
        headers.contentType = MediaType.APPLICATION_JSON
        val entity = HttpEntity<String>(formJson, headers)
        println(entity.body)

        return restTemplate.exchange(url, method, entity, String::class.java)
    }

    /** バリデーションエラーの共通チェック */
    private fun commonAssertion(ret: ResponseEntity<String>): ErrorResponse {
        Assertions.assertTrue(ret.hasBody())

        val body = mapper.readValue(ret.body, ErrorResponse::class.java)
        println(body)
        Assertions.assertNotNull(body.responseId)
        Assertions.assertEquals(INVALID_ERROR, body.errorCause)
        Assertions.assertNull(body.message)

        return body
    }
}
