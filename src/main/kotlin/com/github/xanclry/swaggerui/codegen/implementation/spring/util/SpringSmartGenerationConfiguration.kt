package com.github.xanclry.swaggerui.codegen.implementation.spring.util

import io.swagger.v3.oas.models.media.Schema

class SpringSmartGenerationConfiguration {
    fun getControllerDefaultPath(): String {
        return "generated.controller"
    }

    fun getControllerDefaultName(): String {
        return "DefaultGeneratedController"
    }

    private fun getModelDefaultPath(): String {
        return "generated.model"
    }

    fun getModelPath(schema: Schema<Any>): String {
        return schema.description ?: getModelDefaultPath()
    }
}