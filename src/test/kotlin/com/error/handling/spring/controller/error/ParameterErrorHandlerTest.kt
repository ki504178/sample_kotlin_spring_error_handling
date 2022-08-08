package com.error.handling.spring.controller.error

import com.error.handling.spring.controller.error.model.ErrorResponse
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.should
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.web.client.TestRestTemplate
import org.springframework.http.*
import org.springframework.test.context.junit.jupiter.SpringExtension
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import java.net.URI


@Suppress("NonAsciiCharacters")
@ExtendWith(SpringExtension::class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
internal class ParameterErrorHandlerTest(
    @Autowired
    val restTemplate: TestRestTemplate
) : FunSpec() {
    companion object {
        private const val VALIDATION_ERROR = "ValidationError"
        private const val ROOT_URI = "/test/param_error_test"
    }

    // data class でJackson使うための設定
    val mapper = ObjectMapper().registerKotlinModule()

    init {
        test("正常なパラメータ指定はOKとなること") {
            val ret = restRequest("$ROOT_URI/path/i", HttpMethod.GET)

            ret.statusCode shouldBe HttpStatus.OK
        }
        test("必須のパスパラメータを指定していない場合、ResourceNotFoundとなること") {
            val ret = restRequest("${ROOT_URI}/path", HttpMethod.GET)

            ret.statusCode shouldBe HttpStatus.NOT_FOUND
        }

        test("必須のクエリパラメータを指定していない場合、バリデーションエラーとなること") {
            val ret = restRequest(ROOT_URI, HttpMethod.GET)

            val body = commonAssertion(ret)
            body.invalidErrors should {
                it shouldNotBe null
                it!!.size shouldBe 1
            }

            body.invalidErrors!!.first() should {
                it.index shouldBe 0
                it.itemErrors.size shouldBe 1
                it.itemErrors.first() should { itemError ->
                    itemError.name shouldBe "mail"
                    itemError.type shouldBe "Required"
                    itemError.listErrors shouldBe null
                }
            }
        }

        test("クエリパラメータが正常でない場合、バリデーションエラーとなること") {
            val ret = restRequest("$ROOT_URI?mail=invalid_query", HttpMethod.GET)

            val body = commonAssertion(ret)
            body.invalidErrors should {
                it shouldNotBe null
                it!!.size shouldBe 1
            }

            body.invalidErrors!!.first() should {
                it.index shouldBe 0
                it.itemErrors.size shouldBe 1
                it.itemErrors.first() should { itemError ->
                    itemError.name shouldBe "mail"
                    itemError.type shouldBe "Email"
                    itemError.listErrors shouldBe null
                }
            }
        }

        test("パス・クエリパラメータが正常でない場合、バリデーションエラーとなりそれぞれの項目でエラー内容が返されること") {
            val ret = restRequest("$ROOT_URI/path_query/id?hoge=&fuga=4", HttpMethod.GET)

            val body = commonAssertion(ret)
            body.invalidErrors should {
                it shouldNotBe null
                it!!.size shouldBe 1
            }

            body.invalidErrors!!.first() should {
                it.index shouldBe 0
                it.itemErrors.size shouldBe 3
                it.itemErrors should { itemErrors ->
                    itemErrors.firstOrNull() { err ->
                        err.name == "id" && err.type == "Length"
                    } shouldNotBe null
                    itemErrors.firstOrNull() { err ->
                        err.name == "hoge" && err.type == "NotBlank"
                    } shouldNotBe null
                    itemErrors.firstOrNull() { err ->
                        err.name == "fuga" && err.type == "Max"
                    } shouldNotBe null
                }
            }
        }
    }

    /** APIリクエスト */
    private fun restRequest(uri: String, method: HttpMethod): ResponseEntity<String> {
        val url = URI(uri)
        val headers = HttpHeaders()
        headers.contentType = MediaType.APPLICATION_JSON

        return restTemplate.exchange(url, method, null, String::class.java)
    }

    /** バリデーションエラーの共通チェック */
    private fun commonAssertion(ret: ResponseEntity<String>): ErrorResponse {
        Assertions.assertTrue(ret.hasBody())

        val body = mapper.readValue(ret.body, ErrorResponse::class.java)
        println(body)
        Assertions.assertNotNull(body.responseId)
        Assertions.assertEquals(VALIDATION_ERROR, body.errorCause)
        Assertions.assertNull(body.message)

        return body
    }
}
