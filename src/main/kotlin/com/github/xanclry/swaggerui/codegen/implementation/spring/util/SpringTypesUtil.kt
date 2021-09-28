package com.github.xanclry.swaggerui.codegen.implementation.spring.util

import com.github.xanclry.swaggerui.services.facade.ModelConfigurationFacade
import io.swagger.v3.oas.models.media.ArraySchema
import io.swagger.v3.oas.models.media.MapSchema
import io.swagger.v3.oas.models.media.Schema

class SpringTypesUtil {

    private val smartGenerationConfig = SpringSmartGenerationConfiguration()

    fun getType(schema: Schema<Any>, models: Map<String, Schema<Any>>): String {
        val simpleType = getPrimitiveType(schema, models)
        if (simpleType == null) {
            val collectionType = getCollectionType(schema, models)
            if (collectionType == null) {
                val ref = schema.`$ref` ?: return "Object"
                val referenceType = ref.substringAfterLast("/")
                return generateTypeWithFullNames(referenceType, models)
            }
            return generateTypeWithFullNames(collectionType, models)
        }
        return generateTypeWithFullNames(simpleType, models)

    }

    private fun getCollectionType(schema: Schema<Any>, models: Map<String, Schema<Any>>): String? {
        if (schema is MapSchema) {
            return handleMapSchema(schema, models)
        }
        if (schema is ArraySchema) {
            return handleArraySchema(schema, models)
        }
        return null
    }

    fun getType(shortName: String, models: Map<String, Schema<Any>>): String {
        return generateTypeWithFullNames(shortName, models)
    }

    private fun handleMapSchema(schema: MapSchema, models: Map<String, Schema<Any>>): String {
        val additionalProperty = schema.additionalProperties
        try {
            if (additionalProperty != null && additionalProperty is Schema<*>) {
                val castedSchema = additionalProperty as Schema<Any>
                val mapValueType = getType(castedSchema, models)
                return "java.util.Map<Object, $mapValueType>"
            }
        } catch (e: Exception) {
            // ignore
        }
        return "java.util.Map"
    }

    private fun handleArraySchema(schema: ArraySchema, models: Map<String, Schema<Any>>): String {
        val ref = schema.items.`$ref`
        val type = resolveReferenceType(ref, models)
        return handleTemplateType("java.util.List", type)
    }

    private fun getPrimitiveType(schema: Schema<Any>, models: Map<String, Schema<Any>>): String? {
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
                return handleArraySchema(schema as ArraySchema, models)
            }
        }
        return null
    }

    private fun resolveReferenceType(referenceLink: String?, models: Map<String, Schema<Any>>?): String? {
        if (models == null || referenceLink == null) return null
        val objectType = referenceLink.substringAfterLast("/")
        val entryByReference: Map.Entry<String, Schema<Any>>? = models.entries.find { it.key == objectType }
        return if (entryByReference != null) {
            val packagePath = smartGenerationConfig.getModelPath(entryByReference.value)
            generateFullTypeName(packagePath, entryByReference.key)
        } else {
            null
        }

    }

    private fun generateTypeWithFullNames(
        typeWithShortNames: String,
        models: Map<String, Schema<Any>>
    ): String {
        // now with short names
        var typeName = typeWithShortNames.replace("»", ">").replace("«", "<")

        // searching for model names in complete templated type
        val modelsNameInTypeList: List<String> =
            Regex("(?<=^|[><])(\\w+)(?=[><$]|$)").findAll(typeName).map { it.value }.toList()

        val modelsInTypeList: List<Map.Entry<String, Schema<Any>>> =
            modelsNameInTypeList.mapNotNull { ModelConfigurationFacade.findModelByName(models, it) }

        modelsInTypeList.forEach { entry: Map.Entry<String, Schema<Any>> ->
            typeName = typeName.replace(
                Regex("(?<=^|[<>])(${entry.key})(?=[><$]|$)"),
                generateFullTypeName(smartGenerationConfig.getModelPath(entry.value), entry.key)
            )
        }
        // now with full names
        return typeName
    }

    fun generateBooleanFieldNameWithIs(originalName: String): String {
        return if (!originalName.startsWith("is")) {
            "is" + originalName[0].toUpperCase() + originalName.substring(1)
        } else {
            originalName
        }
    }

    private fun handleTemplateType(rawTemplateType: String, diamondType: String?): String {
        return if (diamondType == null) {
            rawTemplateType
        } else {
            rawTemplateType.plus("<").plus(diamondType).plus(">")
        }
    }

    companion object {
        fun generateFullTypeName(packagePath: String, className: String): String {
            return "${packagePath}.${className}"
        }
    }
}