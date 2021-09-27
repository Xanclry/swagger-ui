package com.github.xanclry.swaggerui.codegen.implementation.spring.util

import com.github.xanclry.swaggerui.model.OperationWithMethodDto
import io.swagger.models.HttpMethod
import io.swagger.v3.oas.models.Operation
import io.swagger.v3.oas.models.media.Schema
import io.swagger.v3.oas.models.parameters.Parameter
import io.swagger.v3.oas.models.parameters.RequestBody
import io.swagger.v3.oas.models.responses.ApiResponse

class SpringEndpointsSyntaxUtil {

    private val typesUtil = SpringTypesUtil()

    fun generateEndpointCode(
        operationWithMethodDto: OperationWithMethodDto,
        controllerPath: String,
        models: Map<String, Schema<Any>>
    ): String {
        val pathForEndpoint = operationWithMethodDto.path.removePrefix(controllerPath)

        // @Get/Post/etc. Mapping
        val bindAnnotationCode = generateBindAnnotationCode(operationWithMethodDto.method, pathForEndpoint)
        val apiOperationCode = generateApiOperationCode(operationWithMethodDto.operation)
        val apiResponsesCode = generateApiResponsesCode(operationWithMethodDto.operation)
        val methodCode = generateMethodCode(operationWithMethodDto, models)
        return "$bindAnnotationCode\n$apiOperationCode\n$apiResponsesCode\n$methodCode"
    }

    private fun generateMethodCode(
        operationWithMethodDto: OperationWithMethodDto,
        models: Map<String, Schema<Any>>
    ): String {
        val returnType = generateMethodReturnType(operationWithMethodDto.operation, models)
        val parametersString = generateMethodParameters(operationWithMethodDto.operation, models)
        val methodName = generateMethodName(operationWithMethodDto)
        val returnTypeInMethod = if (returnType == "void") "" else "null"
        return """
            |public $returnType $methodName($parametersString) {
            |// todo implement this method
            |return $returnTypeInMethod;
            |}
        """.trimMargin()
    }

    private fun generateMethodParameters(operation: Operation, models: Map<String, Schema<Any>>): String {
        var accumulator = ""
        if (operation.parameters != null) {
            operation.parameters.forEach { parameter ->
                accumulator += "\n".plus(generateSingleMethodParameter(parameter, models)).plus(",")
            }
        }

        if (operation.requestBody != null) {
            val generatedRequestBody: String? = generateRequestBody(operation.requestBody, models)
            if (generatedRequestBody != null) {
                accumulator += "\n".plus(generatedRequestBody).plus(",")
            }
        }

        val lastComma = accumulator.lastIndexOf(",")
        if (lastComma > 0) {
            accumulator = accumulator.replaceRange(lastComma, lastComma + 1, "")
        }

        return accumulator
    }

    private fun generateRequestBody(requestBody: RequestBody, models: Map<String, Schema<Any>>): String? {
        val schema: Schema<Any>? = requestBody.content["application/json"]?.schema
        return if (schema != null) {
            "@org.springframework.web.bind.annotation.RequestBody ${
                typesUtil.getType(
                    schema,
                    models
                )
            } ${requestBody.description}"
        } else {
            null
        }
    }

    private fun generateParameterHttpType(parameter: Parameter): String {
        return when (parameter.`in`) {
            "path" -> """@org.springframework.web.bind.annotation.PathVariable(name = "${parameter.name}")"""

            "query", "formData" -> {
                val default =
                    if (parameter.schema.default != null) """, defaultValue = "${parameter.schema.default}"""" else ""
                """@org.springframework.web.bind.annotation.RequestParam(required = ${parameter.required ?: "false"}, name = "${parameter.name}"$default)"""
            }
            "body" -> "@org.springframework.web.bind.annotation.RequestBody"
            else -> ""
        }
    }

    private fun generateSingleMethodParameter(parameter: Parameter, models: Map<String, Schema<Any>>): String {

        return """@io.swagger.annotations.ApiParam(value = "${parameter.description}") ${
            generateParameterHttpType(
                parameter
            )
        } ${
            typesUtil.getType(
                parameter.schema,
                models
            )
        } ${getParameterName(parameter)}"""
    }

    private fun getParameterName(parameter: Parameter): String {
        return parameter.name
    }

    private fun generateMethodReturnType(operation: Operation, models: Map<String, Schema<Any>>): String {
        val schema: Schema<Any>? = operation.responses["200"]?.content?.get("*/*")?.schema
        return if (schema != null) {
            typesUtil.getType(schema, models)
        } else {
            "void"
        }
    }

    private fun generateMethodName(operationWithMethodDto: OperationWithMethodDto): String {
        val operationId = operationWithMethodDto.operation.operationId
        val usingIndex = operationId.lastIndexOf("Using${operationWithMethodDto.method.toString().toUpperCase()}")

        return operationId.replaceRange(usingIndex, operationId.length, "")
    }

    private fun generateApiResponsesCode(operation: Operation): String {
        return if (operation.responses.isNotEmpty()) {
            var responseAccumulator = ""
            operation.responses.forEach { response ->
                responseAccumulator =
                    responseAccumulator.plus(generateSingleApiResponseCode(response.key, response.value)).plus(",\n")
            }
            """@io.swagger.annotations.ApiResponses(value = {$responseAccumulator})"""
        } else {
            ""
        }
    }

    private fun generateSingleApiResponseCode(code: String, apiResponse: ApiResponse): String {
        val message: String =
            if (apiResponse.description != null) """, message = "${apiResponse.description}"""" else ""
        return """@io.swagger.annotations.ApiResponse(code = ${code}$message)"""
    }

    private fun generateApiOperationCode(operation: Operation): String {
        val description = operation.summary
        return if (description != null) {
            """@io.swagger.annotations.ApiOperation(value = "$description")"""
        } else {
            ""
        }
    }

    private fun generateBindAnnotationCode(method: HttpMethod, path: String): String {
        val methodNameInLowerCase = method.toString().toLowerCase()
        val methodName = methodNameInLowerCase.replaceRange(0, 1, methodNameInLowerCase[0].toString().toUpperCase())
        return if (path == "") {
            """@org.springframework.web.bind.annotation.${methodName}Mapping"""
        } else {
            """@org.springframework.web.bind.annotation.${methodName}Mapping("$path")"""
        }
    }
}
