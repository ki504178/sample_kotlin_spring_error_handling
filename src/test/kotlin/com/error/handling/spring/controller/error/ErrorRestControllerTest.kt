package com.error.handling.spring.controller.error

import com.error.handling.spring.controller.error.mock.MockErrorRestControllerForm
import com.error.handling.spring.controller.error.model.ErrorResponse
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.should
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.web.client.TestRestTemplate
import org.springframework.http.*
import org.springframework.test.context.junit.jupiter.SpringExtension
import java.net.URI

@Suppress("NonAsciiCharacters")
@ExtendWith(SpringExtension::class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
internal class ErrorRestControllerTest(
    @Autowired
    val restTemplate: TestRestTemplate
) : FunSpec() {
    companion object {
        private const val ROOT_URI = "/test"
    }

    // data class でJackson使うための設定
    val mapper = ObjectMapper().registerKotlinModule()

    init {
        test("HandleableExceptionを継承したExceptionがthrowされた場合、HandleableErrorとしてレスポンスされること") {
            val ret = restRequest(ROOT_URI, HttpMethod.GET)
            val body = mapper.readValue(ret.body, ErrorResponse::class.java)

            body.responseId shouldNotBe null
            body.errorCause shouldBe "HandleableError"
            body.message shouldBe "検証用"
        }
        test("想定外のExceptionがthrowされた場合、SystemExceptionとしてレスポンスされること") {
            val ret = restRequest(ROOT_URI, HttpMethod.POST)
            val body = mapper.readValue(ret.body, ErrorResponse::class.java)

            body.responseId shouldNotBe null
            body.errorCause shouldBe "SystemException"
            body.message shouldBe "検証用"
        }
        test("Path/Query/RequestBodyすべてでバリデーションエラーとなる場合、RequestBodyが優先されてPath/Queryのバリデーションエラーは発生しないこと") {
            val form = MockErrorRestControllerForm("")
            val ret = restRequest("$ROOT_URI/12?num=2", HttpMethod.PUT, mapper.writeValueAsString(form))
            val body = mapper.readValue(ret.body, ErrorResponse::class.java)

            body.responseId shouldNotBe null
            body.errorCause shouldBe "ValidationError"
            body.invalidErrors shouldNotBe null
            body.invalidErrors?.size shouldBe 1

            body.invalidErrors!!.first() should {
                it.itemErrors.size shouldBe 1
                it.itemErrors.first() should { itemError ->
                    itemError.name shouldBe "mockId"
                    itemError.type shouldBe "NotBlank"
                }
            }
        }
        test("未定義のURIでは、ResourceNotFoundとなること") {
            val ret = restRequest("${ROOT_URI}/not/found", HttpMethod.GET)

            ret.statusCode shouldBe HttpStatus.NOT_FOUND

            val body = mapper.readValue(ret.body, ErrorResponse::class.java)

            body.responseId shouldNotBe null
            body.errorCause shouldBe "ResourceNotFound"
            body.message shouldBe "Not Found"
        }
    }

    /** APIリクエスト */
    private fun restRequest(uri: String, method: HttpMethod, formJson: String? = null): ResponseEntity<String> {
        val url = URI(uri)
        val headers = HttpHeaders()
        headers.contentType = MediaType.APPLICATION_JSON
        val entity = if (formJson == null) null else HttpEntity<String>(formJson, headers)
        println(entity?.body)

        return restTemplate.exchange(url, method, entity, String::class.java)
    }
}
