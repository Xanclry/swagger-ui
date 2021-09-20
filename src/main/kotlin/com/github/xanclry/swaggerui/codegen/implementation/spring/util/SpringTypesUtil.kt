package com.github.xanclry.swaggerui.codegen.implementation.spring.util

import io.swagger.v3.oas.models.media.ArraySchema
import io.swagger.v3.oas.models.media.Schema

class SpringTypesUtil {
    fun getParameterType(schema: Schema<Any>, models: Map<String, Schema<Any>>? = null): String {
        val format = schema.format
        when (schema.type) {
            "integer" -> {
                if (format == "int64") {
                    return "Long"
                }
                return "Integer"
            }
            "number" -> {
                if (format == "float") {
                    return "Float"
                }
                return "Double"
            }
            "boolean" -> return "Boolean"
            "string" -> {
                if (schema.enum != null) return "Enum"
                return when (format) {
                    "date" -> "java.time.LocalDate"
                    else -> "String"
                }
            }
            "array" -> {
                val arraySchema = schema as ArraySchema
                val ref = arraySchema.items.`$ref`
                val type = resolveReferenceType(ref, models)
                return handleTemplateType("java.util.List", type)
            }
        }
        return "***"
    }

    private fun resolveReferenceType(referenceLink: String?, models: Map<String, Schema<Any>>?): String? {
        if (models == null || referenceLink == null) return null
        val objectType = referenceLink.substringAfterLast("/")
        val entryByReference: Map.Entry<String, Schema<Any>>? = models.entries.find { it.key == objectType }
        return if (entryByReference != null) {
            "${entryByReference.value.description}.${entryByReference.key}"
        } else {
            null
        }

    }

    private fun handleTemplateType(rawTemplateType: String, diamondType: String?): String {
        return if (diamondType == null) {
            rawTemplateType
        } else {
            rawTemplateType.plus("<").plus(diamondType).plus(">")
        }
    }
}