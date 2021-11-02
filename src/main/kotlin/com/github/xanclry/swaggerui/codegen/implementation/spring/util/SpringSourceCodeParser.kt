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

    fun getEndpointsMappings(text: String, fullPath: Boolean): List<SwaggerMethodDto> {
        var controllerPath = ""
        if (fullPath) {
            controllerPath = getControllerPath(text)
        }
        val resultList: MutableList<SwaggerMethodDto> = ArrayList()

        val pathSpecifiedRegex = "@([^R]{3,7})Mapping.*?\"(.+?)\"".toRegex()
        val pathSpecifiedSearchResult = pathSpecifiedRegex.findAll(text)

        handleSearchResult(pathSpecifiedSearchResult, resultList, controllerPath, false)

        val pathNotSpecifiedRegex = "@([^R]{3,7})Mapping(\n|\\Q()\\E\n|\\Q(\"\")\\E)".toRegex()
        val pathNotSpecifiedSearchResult = pathNotSpecifiedRegex.findAll(text)

        handleSearchResult(pathNotSpecifiedSearchResult, resultList, controllerPath, true)

        return resultList
    }

    private fun getPathFromMatchResult(matchResult: MatchResult, pathIsEmpty: Boolean): String {
        return if (pathIsEmpty) {
            ""
        } else {
            matchResult.groupValues[2]
        }
    }

    private fun handleSearchResult(
        searchResult: Sequence<MatchResult>,
        resultList: MutableList<SwaggerMethodDto>,
        controllerPath: String,
        pathIsEmpty: Boolean
    ) {
        searchResult.forEach { matchResult ->
            val method = HttpMethod.valueOf(matchResult.groupValues[1].toUpperCase())
            val path = getPathFromMatchResult(matchResult, pathIsEmpty)
            // todo convert to map
            val existingDto: SwaggerMethodDto? = resultList.find { dto ->
                dto.path == path
            }
            if (existingDto != null) {
                existingDto.methodSet.add(method)
            } else {
                val newDto = SwaggerMethodDto(HashSet(), uniteControllerAndMethodPath(controllerPath, path))
                newDto.methodSet.add(method)
                resultList.add(newDto)
            }
        }
    }

    private fun uniteControllerAndMethodPath(controllerPath: String, methodPath: String): String {
        val controllerPathWithSlash = if (!controllerPath.endsWith("/")) {
            controllerPath.plus("/")
        } else {
            controllerPath
        }
        val methodPathWithSlash = if (!methodPath.startsWith("/")) {
            "/".plus(methodPath)
        } else {
            methodPath
        }
        return controllerPathWithSlash.plus(methodPathWithSlash).replace("//", "/")
    }
}