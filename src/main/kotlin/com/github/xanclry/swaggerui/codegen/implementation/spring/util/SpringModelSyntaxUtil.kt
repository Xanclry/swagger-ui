package com.github.xanclry.swaggerui.codegen.implementation.spring.util

import io.swagger.v3.oas.models.media.Schema

class SpringModelSyntaxUtil {

    fun generateModelCode(modelName: String, models: Map<String, Schema<Any>>): String {
        val currentSchema = models[modelName]

        return ""
    }
}