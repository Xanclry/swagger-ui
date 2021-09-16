package com.github.xanclry.swaggerui.services.facade

import com.github.xanclry.swaggerui.services.ConfigurationService
import com.intellij.openapi.components.service
import com.intellij.openapi.project.Project
import io.swagger.v3.oas.models.OpenAPI
import io.swagger.v3.oas.models.media.Schema

class ModelConfigurationFacade(project: Project) {
    private val configurationService = project.service<ConfigurationService>()

    fun parseModels(): MutableMap<String, Schema<Any>>? {
        val configuration: OpenAPI = getConfiguration()
        return configuration.components.schemas
    }

    private fun getConfiguration(): OpenAPI {
        return configurationService.getConfiguration()
    }

}