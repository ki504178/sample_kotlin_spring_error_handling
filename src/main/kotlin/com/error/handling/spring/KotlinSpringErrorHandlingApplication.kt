package com.error.handling.spring

import org.springframework.boot.autoconfigure.EnableAutoConfiguration
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.context.annotation.Configuration

@SpringBootApplication
class KotlinSpringErrorHandlingApplication

fun main(args: Array<String>) {
	runApplication<KotlinSpringErrorHandlingApplication>(*args)
}
