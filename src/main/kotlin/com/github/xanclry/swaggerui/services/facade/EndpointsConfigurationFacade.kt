package com.github.xanclry.swaggerui.services.facade

import com.github.xanclry.swaggerui.model.OperationWithMethodDto
import com.github.xanclry.swaggerui.model.SwaggerMethodDto
import com.github.xanclry.swaggerui.model.file.FileMetadataDto
import com.github.xanclry.swaggerui.services.ConfigurationService
import com.intellij.openapi.components.service
import com.intellij.openapi.project.Project
import io.swagger.models.HttpMethod
import io.swagger.v3.oas.models.OpenAPI
import io.swagger.v3.oas.models.PathItem
import org.apache.commons.lang.StringUtils

class EndpointsConfigurationFacade(project: Project) {
    private val configurationService = project.service<ConfigurationService>()

    fun identifyMissingEndpoints(
        controllerPath: String,
        existingMappings: List<SwaggerMethodDto>
    ): List<OperationWithMethodDto> {

        val fullPathMappings: List<SwaggerMethodDto> = existingMappings.map { mapping ->
            SwaggerMethodDto(mapping.methodSet, controllerPath.plus(mapping.path))
        }

        val openApiConfig: OpenAPI = getConfiguration()
        val endpointsForCurrentControllerFromConfig: List<MutableMap.MutableEntry<String, PathItem>> =
            openApiConfig.paths.entries.filter { entry ->
                entry.key.startsWith(controllerPath)
            }
        return differenceBetweenAllAndExisting(endpointsForCurrentControllerFromConfig, fullPathMappings)
    }

    fun computeFileOperationsMap(
        missingEndpoints: List<OperationWithMethodDto>,
        parsingFunction: (operationWithMethod: OperationWithMethodDto) -> FileMetadataDto
    ): Map<FileMetadataDto, List<OperationWithMethodDto>> {
        val fileOperationMap: MutableMap<FileMetadataDto, MutableList<OperationWithMethodDto>> = HashMap()
        missingEndpoints.forEach {
            val fileMetadataDto = parsingFunction.invoke(it)
            fileOperationMap.computeIfAbsent(fileMetadataDto) { ArrayList() }
            fileOperationMap[fileMetadataDto]?.add(it)
        }
        return fileOperationMap
    }

    fun findEndpointsCommonPrefix(endpointsPaths: List<String>): String {
        val commonPrefix = StringUtils.getCommonPrefix(endpointsPaths.toTypedArray())
        return if (commonPrefix == "") {
            "/"
        } else {
            commonPrefix
        }
    }

    private fun differenceBetweenAllAndExisting(
        all: Collection<MutableMap.MutableEntry<String, PathItem>>,
        existing: Collection<SwaggerMethodDto>
    ): List<OperationWithMethodDto> {
        val accumulator: MutableList<OperationWithMethodDto> = ArrayList()
        all.forEach { entryFromConfig ->
            handleOpenApiPath(entryFromConfig, existing, accumulator)
        }
        return accumulator
    }

    fun identifyMissingEndpointsInProject(existingMappings: Collection<SwaggerMethodDto>): List<OperationWithMethodDto> {
        val openApiConfig: OpenAPI = getConfiguration()
        val allEndpointsFromConfig: List<MutableMap.MutableEntry<String, PathItem>> =
            openApiConfig.paths.entries.toList()

        return differenceBetweenAllAndExisting(allEndpointsFromConfig, existingMappings)
    }

    private fun getConfiguration(): OpenAPI {
        return configurationService.getConfiguration()
    }

    private fun handleOpenApiPath(
        entryFromConfig: MutableMap.MutableEntry<String, PathItem>,
        existingMappings: Collection<SwaggerMethodDto>,
        result: MutableList<OperationWithMethodDto>
    ) {
        val path = entryFromConfig.key
        var existingMappingForCurrentPath: SwaggerMethodDto? =
            existingMappings.find { mapping -> mapping.path == path }
        if (existingMappingForCurrentPath == null) {
            existingMappingForCurrentPath = SwaggerMethodDto(HashSet(), path)
        }
        if (entryFromConfig.value.get != null && !existingMappingForCurrentPath.methodSet.contains(HttpMethod.GET)) result.add(
            OperationWithMethodDto(HttpMethod.GET, path, entryFromConfig.value.get)
        )
        if (entryFromConfig.value.put != null && !existingMappingForCurrentPath.methodSet.contains(HttpMethod.PUT)) result.add(
            OperationWithMethodDto(HttpMethod.PUT, path, entryFromConfig.value.put)
        )
        if (entryFromConfig.value.post != null && !existingMappingForCurrentPath.methodSet.contains(HttpMethod.POST)) result.add(
            OperationWithMethodDto(HttpMethod.POST, path, entryFromConfig.value.post)
        )
        if (entryFromConfig.value.delete != null && !existingMappingForCurrentPath.methodSet.contains(HttpMethod.DELETE)) result.add(
            OperationWithMethodDto(HttpMethod.DELETE, path, entryFromConfig.value.delete)
        )
        if (entryFromConfig.value.options != null && !existingMappingForCurrentPath.methodSet.contains(HttpMethod.OPTIONS)) result.add(
            OperationWithMethodDto(HttpMethod.OPTIONS, path, entryFromConfig.value.options)
        )
        if (entryFromConfig.value.head != null && !existingMappingForCurrentPath.methodSet.contains(HttpMethod.HEAD)) result.add(
            OperationWithMethodDto(HttpMethod.HEAD, path, entryFromConfig.value.head)
        )
        if (entryFromConfig.value.patch != null && !existingMappingForCurrentPath.methodSet.contains(HttpMethod.PATCH)) result.add(
            OperationWithMethodDto(HttpMethod.PATCH, path, entryFromConfig.value.patch)
        )
    }
}
