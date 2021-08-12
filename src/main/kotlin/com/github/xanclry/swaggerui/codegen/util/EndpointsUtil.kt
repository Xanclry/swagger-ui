package com.github.xanclry.swaggerui.codegen.util

import com.github.xanclry.swaggerui.model.OperationWithMethodDto
import com.github.xanclry.swaggerui.model.SwaggerMethodDto
import com.github.xanclry.swaggerui.services.ConfigurationService
import com.intellij.openapi.components.service
import com.intellij.openapi.project.Project
import io.swagger.models.HttpMethod
import io.swagger.v3.oas.models.OpenAPI
import io.swagger.v3.oas.models.PathItem

class EndpointsUtil(project: Project) {
    private val configurationService = project.service<ConfigurationService>()

    fun getEndpointsToCreate(
        controllerPath: String,
        existingMappings: List<SwaggerMethodDto>
    ): List<OperationWithMethodDto> {

        val fullPathMappings: List<SwaggerMethodDto> = existingMappings.map { mapping ->
            SwaggerMethodDto(mapping.methodSet, controllerPath.plus(mapping.path))
        }

        val openApiConfig: OpenAPI = configurationService.getConfiguration()
        val endpointsForCurrentControllerFromConfig: List<MutableMap.MutableEntry<String, PathItem>> =
            openApiConfig.paths.entries.filter { entry ->
                entry.key.startsWith(controllerPath)
            }
        val endpointsToCreate: MutableList<OperationWithMethodDto> = ArrayList()
        endpointsForCurrentControllerFromConfig.forEach { entryFromConfig ->
            handleOpenApiPath(entryFromConfig, fullPathMappings, endpointsToCreate)
        }
        return endpointsToCreate
    }

    private fun handleOpenApiPath(
        entryFromConfig: MutableMap.MutableEntry<String, PathItem>,
        existingMappings: List<SwaggerMethodDto>,
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