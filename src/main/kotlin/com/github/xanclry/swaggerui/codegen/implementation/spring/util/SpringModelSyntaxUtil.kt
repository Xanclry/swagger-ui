package com.github.xanclry.swaggerui.codegen.implementation.spring.util

import io.swagger.v3.oas.models.media.Schema

class SpringModelSyntaxUtil {

    private val typesUtil = SpringTypesUtil()

    fun generateModelCode(modelName: String, packagePath: String, models: Map<String, Schema<Any>>): String {
        val currentSchema: Schema<Any>? = models[modelName]
        val properties: MutableMap<String, Schema<Any>>? = currentSchema?.properties
        return """
            @lombok.Data
            @lombok.NoArgsConstructor
            @io.swagger.annotations.ApiModel(description = "$packagePath")
            public class $modelName {
            ${
            properties?.entries?.stream()
                ?.map { generateFieldCode(it, models) }
                ?.reduce { acc, item -> acc.plus("\n").plus(item).plus("\n") }?.get()
        }
            }
        """.trimIndent()
    }

    private fun generateFieldCode(entry: Map.Entry<String, Schema<Any>>, models: Map<String, Schema<Any>>): String {
        val parameterType = typesUtil.getParameterType(entry.value, models)
        val name = generateFieldName(parameterType, entry.key)
        return "private $parameterType $name;"
    }

    private fun generateFieldName(type: String, nameFromConfig: String): String {
        return when (type) {
            "Boolean", "boolean" -> typesUtil.generateBooleanFieldNameWithIs(nameFromConfig)
            else -> nameFromConfig
        }
    }

}