package com.github.xanclry.swaggerui.codegen.implementation.java

import com.github.xanclry.swaggerui.codegen.Codegen
import com.github.xanclry.swaggerui.codegen.CodegenAvailability
import com.github.xanclry.swaggerui.codegen.implementation.java.util.JavaSyntaxUtil
import com.github.xanclry.swaggerui.model.OperationWithMethodDto
import com.github.xanclry.swaggerui.model.SwaggerMethodDto
import com.github.xanclry.swaggerui.services.ConfigurationService
import com.github.xanclry.swaggerui.util.Notifier
import com.intellij.notification.NotificationType
import com.intellij.openapi.components.service
import com.intellij.openapi.editor.Document
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.project.Project
import io.swagger.models.HttpMethod
import io.swagger.v3.oas.models.OpenAPI
import io.swagger.v3.oas.models.PathItem


class JavaCodegenImpl : Codegen {

    private val syntaxUtil = JavaSyntaxUtil()

    override fun isFileSuitable(document: Document): CodegenAvailability {
        val code = document.text
        return runChecks(code, ::isController, ::hasRequestMapping)
    }

    override fun generateCode(project: Project, editor: Editor): String {
        try {
            val configurationService = project.service<ConfigurationService>()
            val controllerPath = syntaxUtil.getControllerPath(editor.document.text)
            val existingMappings: List<SwaggerMethodDto> = syntaxUtil.getEndpointsMappings(editor.document.text)
            existingMappings.forEach { mapping ->
                mapping.path = controllerPath.plus(mapping.path)
            }

            val openApiConfig: OpenAPI = configurationService.getConfiguration()
            val endpointsForCurrentControllerFromConfig: List<MutableMap.MutableEntry<String, PathItem>> =
                openApiConfig.paths.entries.filter { entry ->
                    entry.key.startsWith(controllerPath)
                }
            val endpointsToCreate: MutableList<OperationWithMethodDto> = ArrayList()
            endpointsForCurrentControllerFromConfig.forEach { entryFromConfig ->
                handleOpenApiPath(entryFromConfig, existingMappings, endpointsToCreate)
            }

            var accumulator = ""

            endpointsToCreate.forEach { endpoint ->
                accumulator += syntaxUtil.generateEndpointCode(endpoint, controllerPath).plus("\n\n")
            }
            return accumulator

        } catch (e: Exception) {
            if (e.message == null) {
                Notifier.notifyProjectWithMessageFromBundle(
                    project,
                    "notification.config.error",
                    NotificationType.ERROR
                )
                throw e
            } else {
                Notifier.notifyProjectWithMessageFromBundle(project, e.message!!, NotificationType.ERROR)
            }
        }
        return ""
    }

    override fun offsetForNewCode(document: Document): Int {
        return document.text.lastIndexOf("}") - 1
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
        if (entryFromConfig.value.get != null && !existingMappingForCurrentPath.method.contains(HttpMethod.GET)) result.add(
            OperationWithMethodDto(HttpMethod.GET, path, entryFromConfig.value.get)
        )
        if (entryFromConfig.value.put != null && !existingMappingForCurrentPath.method.contains(HttpMethod.PUT)) result.add(
            OperationWithMethodDto(HttpMethod.PUT, path, entryFromConfig.value.put)
        )
        if (entryFromConfig.value.post != null && !existingMappingForCurrentPath.method.contains(HttpMethod.POST)) result.add(
            OperationWithMethodDto(HttpMethod.POST, path, entryFromConfig.value.post)
        )
        if (entryFromConfig.value.delete != null && !existingMappingForCurrentPath.method.contains(HttpMethod.DELETE)) result.add(
            OperationWithMethodDto(HttpMethod.DELETE, path, entryFromConfig.value.delete)
        )
        if (entryFromConfig.value.options != null && !existingMappingForCurrentPath.method.contains(HttpMethod.OPTIONS)) result.add(
            OperationWithMethodDto(HttpMethod.OPTIONS, path, entryFromConfig.value.options)
        )
        if (entryFromConfig.value.head != null && !existingMappingForCurrentPath.method.contains(HttpMethod.HEAD)) result.add(
            OperationWithMethodDto(HttpMethod.HEAD, path, entryFromConfig.value.head)
        )
        if (entryFromConfig.value.patch != null && !existingMappingForCurrentPath.method.contains(HttpMethod.PATCH)) result.add(
            OperationWithMethodDto(HttpMethod.PATCH, path, entryFromConfig.value.patch)
        )
    }

    private fun isController(text: String): String? {
        return if (text.contains("@Controller") || text.contains("@RestController")) null else "notification.codegen.error.java.noController"
    }

    private fun hasRequestMapping(text: String): String? {
        return if (text.contains("@RequestMapping")) null else "notification.codegen.error.java.noRequestMapping"
    }
}