package com.github.xanclry.swaggerui.services.facade

import com.github.xanclry.swaggerui.codegen.ModelGenerator
import com.github.xanclry.swaggerui.services.ConfigurationService
import com.intellij.openapi.components.service
import com.intellij.openapi.project.Project
import io.swagger.v3.oas.models.OpenAPI
import io.swagger.v3.oas.models.media.Schema

class ModelConfigurationFacade(project: Project) {
    private val configurationService = project.service<ConfigurationService>()


    companion object {
        fun findModelByName(models: Map<String, Schema<Any>>, name: String): Map.Entry<String, Schema<Any>>? {
            return models.entries.find { it.key == name }
        }
    }

    fun getFilteredModelsFromConfig(modelGenerator: ModelGenerator): Map<String, Schema<Any>> {
        val allModels: MutableMap<String, Schema<Any>>? = parseModels()
        return if (allModels != null) {
            modelGenerator.filterModels(allModels)
        } else {
            emptyMap()
        }
    }

    private fun parseModels(): MutableMap<String, Schema<Any>>? {
        val configuration: OpenAPI = getConfiguration()
        return configuration.components.schemas
    }

    private fun getConfiguration(): OpenAPI {
        return configurationService.getConfiguration()
    }

}