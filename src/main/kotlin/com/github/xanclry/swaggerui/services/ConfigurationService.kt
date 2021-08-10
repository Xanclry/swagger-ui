package com.github.xanclry.swaggerui.services

import com.github.xanclry.swaggerui.MyBundle
import com.github.xanclry.swaggerui.state.settings.SettingsPathType
import com.github.xanclry.swaggerui.state.settings.SettingsState
import com.intellij.openapi.components.Service
import com.intellij.openapi.project.Project
import io.swagger.parser.OpenAPIParser
import io.swagger.v3.oas.models.OpenAPI

@Service
class ConfigurationService(project: Project) {

    init {
        println(MyBundle.message("projectService", project.name))
    }

    fun getConfiguration(): OpenAPI {
        val url = SettingsState.instance.configUrl
        return when (SettingsState.instance.configType) {
            SettingsPathType.URL -> getUrlConfiguration(url)
            SettingsPathType.FILE -> getFileConfiguration(url)
        }
    }

    private fun getUrlConfiguration(url: String): OpenAPI {
        // todo add wrong url exception handling
        return OpenAPIParser().readLocation(url, null, null).openAPI

    }

    private fun getFileConfiguration(url: String): OpenAPI {
        TODO("Not yet implemented")

    }


}
