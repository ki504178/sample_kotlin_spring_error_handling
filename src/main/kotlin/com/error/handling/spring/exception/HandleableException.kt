package com.error.handling.spring.exception

import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.ResponseStatus

/**
 * システムでハンドリング可能な例外基底クラス
 */
@ResponseStatus(value = HttpStatus.BAD_REQUEST)
abstract class HandleableException(
    override val message: String,
    override val cause: Throwable? = null
) : RuntimeException(message, cause)

/** 検証用 */
class SampleException(
    override val message: String
) : HandleableException(message = message)

/** 404 */
@ResponseStatus(value = HttpStatus.NOT_FOUND)
class ResourceNotFoundException(
    override val message: String
) : HandleableException(message = message)
