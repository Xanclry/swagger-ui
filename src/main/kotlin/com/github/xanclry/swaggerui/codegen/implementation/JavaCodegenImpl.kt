package com.github.xanclry.swaggerui.codegen.implementation

import com.github.xanclry.swaggerui.codegen.Codegen
import com.github.xanclry.swaggerui.codegen.CodegenAvailability
import com.github.xanclry.swaggerui.services.ConfigurationService
import com.github.xanclry.swaggerui.util.Notifier
import com.intellij.notification.NotificationType
import com.intellij.openapi.components.service
import com.intellij.openapi.editor.Document
import com.intellij.openapi.project.Project
import io.swagger.v3.oas.models.OpenAPI


class JavaCodegenImpl : Codegen {
    override fun isFileSuitable(document: Document): CodegenAvailability {
        val code = document.text
        return runChecks(code, ::isController, ::hasRequestMapping)
    }

    override fun generateCode(project: Project) {
        try {
            val configurationService = project.service<ConfigurationService>()
            val config: OpenAPI = configurationService.getConfiguration()

            println()
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

    }

    private fun isController(text: String): String? {
        return if (text.contains("@Controller") || text.contains("@RestController")) {
            null
        } else {
            "notification.codegen.error.java.noController"
        }
    }

    private fun hasRequestMapping(text: String): String? {
        return if (text.contains("@RequestMapping")) {
            null
        } else {
            "notification.codegen.error.java.noRequestMapping"
        }
    }
}