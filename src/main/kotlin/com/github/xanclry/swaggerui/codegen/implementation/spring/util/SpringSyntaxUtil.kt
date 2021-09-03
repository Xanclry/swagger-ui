package com.github.xanclry.swaggerui.codegen.implementation.spring.util

import com.github.xanclry.swaggerui.model.OperationWithMethodDto
import com.github.xanclry.swaggerui.model.SwaggerMethodDto
import io.swagger.models.HttpMethod
import io.swagger.v3.oas.models.Operation
import io.swagger.v3.oas.models.parameters.Parameter
import io.swagger.v3.oas.models.responses.ApiResponse

class SpringSyntaxUtil {
    fun getControllerPath(text: String): String {
        val regex = "@RequestMapping.*?\"(.+?)\"".toRegex()
        val find = regex.find(text)?.groupValues?.get(1)
        return find ?: ""
    }

    fun getEndpointsMappings(text: String): List<SwaggerMethodDto> {
        val regex = "@([^R]{3,7})Mapping.*?\"(.+?)\"".toRegex()
        val searchResult = regex.findAll(text)

        val resultList: MutableList<SwaggerMethodDto> = ArrayList()

        searchResult.forEach { matchResult ->
            val method = HttpMethod.valueOf(matchResult.groupValues[1].toUpperCase())
            val path = matchResult.groupValues[2]
            val existingDto: SwaggerMethodDto? = resultList.find { dto ->
                dto.path == path
            }
            if (existingDto != null) {
                existingDto.methodSet.add(method)
            } else {
                val newDto = SwaggerMethodDto(HashSet(), path)
                newDto.methodSet.add(method)
                resultList.add(newDto)
            }
        }

        return resultList
    }

    fun generateEndpointCode(operationWithMethodDto: OperationWithMethodDto, controllerPath: String): String {
        val pathForEndpoint = operationWithMethodDto.path.removePrefix(controllerPath)
        val bindAnnotationCode = generateBindAnnotationCode(operationWithMethodDto.method, pathForEndpoint)
        val apiOperationCode = generateApiOperationCode(operationWithMethodDto.operation)
        val apiResponsesCode = generateApiResponsesCode(operationWithMethodDto.operation)
        val methodCode = generateMethodCode(operationWithMethodDto)
        return "$bindAnnotationCode\n$apiOperationCode\n$apiResponsesCode\n$methodCode"
    }

    private fun generateMethodCode(operationWithMethodDto: OperationWithMethodDto): String {
        val returnType = generateMethodReturnType(operationWithMethodDto.operation)
        val parametersString = generateMethodParameters(operationWithMethodDto.operation)
        val methodName = generateMethodName(operationWithMethodDto)
        val returnTypeInMethod = if (returnType == "void") "" else "null"
        return """
            |public $returnType $methodName($parametersString) {
            |// todo implement this method
            |return $returnTypeInMethod;
            |}
        """.trimMargin()
    }

    private fun generateMethodParameters(operation: Operation): String {
        var accumulator = ""
        if (operation.parameters != null) {
            operation.parameters.forEach { parameter ->
                accumulator += "\n".plus(generateSingleMethodParameter(parameter)).plus(",")
            }

            val lastComma = accumulator.lastIndexOf(",")
            if (lastComma > 0) {
                accumulator = accumulator.replaceRange(lastComma, lastComma + 1, "")
            }
        }
        return accumulator
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

    private fun generateSingleMethodParameter(parameter: Parameter): String {

        return """@io.swagger.annotations.ApiParam(value = "${parameter.description}") ${
            generateParameterHttpType(
                parameter
            )
        } ${
            getParameterType(
                parameter.schema.type,
                parameter.schema.format,
                parameter
            )
        } ${getParameterName(parameter)}"""
    }

    private fun getParameterName(parameter: Parameter): String {
        return parameter.name
    }

    private fun getParameterType(type: String, format: String?, parameter: Parameter): String {
        when (type) {
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
                if (parameter.schema.enum != null) return "Enum"
                return when (format) {
                    "date" -> "java.time.LocalDate"
                    else -> "String"
                }
            }
            "array" -> {
                return "java.util.List<>"
            }
        }
        return "***"
    }

    private fun generateMethodReturnType(operation: Operation): String {
        val ref: String? = operation.responses["200"]?.content?.get("*/*")?.schema?.`$ref`
        return if (ref != null) {
            val returnTypeWithQuotes = ref.replaceBeforeLast("/", "").replaceRange(0, 1, "")
            returnTypeWithQuotes.replace("»", ">").replace("«", "<")
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
