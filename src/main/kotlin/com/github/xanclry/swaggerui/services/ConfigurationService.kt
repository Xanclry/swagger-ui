package com.github.xanclry.swaggerui.services

import com.github.xanclry.swaggerui.state.settings.SettingsPathType
import com.github.xanclry.swaggerui.state.settings.SettingsState
import com.intellij.openapi.components.Service
import io.swagger.parser.OpenAPIParser
import io.swagger.v3.oas.models.OpenAPI

@Service
class ConfigurationService {

    fun getConfiguration(): OpenAPI {
        return loadConfig()
    }

    private fun loadConfig(): OpenAPI {
        val url = SettingsState.instance.configUrl
        return when (SettingsState.instance.configType) {
            SettingsPathType.URL -> loadUrlConfiguration(url)
            SettingsPathType.FILE -> loadFileConfiguration()
        }
    }

    private fun loadUrlConfiguration(url: String): OpenAPI {
        val parsedConfig = OpenAPIParser().readLocation(url, null, null)
        if (parsedConfig.openAPI != null) {
            return parsedConfig.openAPI
        } else {
            throw IllegalArgumentException()
        }
    }

    private fun loadFileConfiguration(): OpenAPI {
        TODO("Not yet implemented")
    }

}
