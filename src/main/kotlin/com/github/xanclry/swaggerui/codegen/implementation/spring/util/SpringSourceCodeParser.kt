package com.github.xanclry.swaggerui.codegen.implementation.spring.util

import com.github.xanclry.swaggerui.model.SwaggerMethodDto
import io.swagger.models.HttpMethod

class SpringSourceCodeParser {
    // todo put verifications in separate class
    fun isController(text: String): String? {
        return if (text.contains("@Controller") || text.contains("@RestController")) null
        else "notification.codegen.error.java.noController"
    }

    fun hasRequestMapping(text: String): String? {
        return if (text.contains("@RequestMapping")) null else "notification.codegen.error.java.noRequestMapping"
    }

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
}