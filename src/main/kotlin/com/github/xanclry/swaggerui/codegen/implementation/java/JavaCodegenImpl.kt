package com.github.xanclry.swaggerui.codegen.implementation.java

import com.github.xanclry.swaggerui.codegen.Codegen
import com.github.xanclry.swaggerui.codegen.CodegenAvailability
import com.github.xanclry.swaggerui.codegen.implementation.java.util.JavaSyntaxUtil
import com.github.xanclry.swaggerui.codegen.util.EndpointsUtil
import com.github.xanclry.swaggerui.model.OperationWithMethodDto
import com.github.xanclry.swaggerui.model.SwaggerMethodDto
import com.github.xanclry.swaggerui.util.Notifier
import com.intellij.notification.NotificationType
import com.intellij.openapi.editor.Document
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.project.Project

class JavaCodegenImpl(project: Project) : Codegen {

    private val syntaxUtil = JavaSyntaxUtil()
    private val endpointsUtil = EndpointsUtil(project)

    override fun isFileSuitable(document: Document): CodegenAvailability {
        val code = document.text
        return runChecks(code, ::isController, ::hasRequestMapping)
    }

    override fun generateCode(project: Project, editor: Editor): String {
        try {
            val controllerPath = syntaxUtil.getControllerPath(editor.document.text)
            val existingMappings: List<SwaggerMethodDto> = syntaxUtil.getEndpointsMappings(editor.document.text)
            val endpointsToCreate: List<OperationWithMethodDto> =
                endpointsUtil.getEndpointsToCreate(controllerPath, existingMappings)

            var accumulator = ""

            endpointsToCreate.forEach { endpoint ->
                accumulator += syntaxUtil.generateEndpointCode(endpoint, controllerPath).plus("\n\n")
            }
            return accumulator
        } catch (e: IllegalArgumentException) {
            Notifier.notifyProjectWithMessageFromBundle(
                project,
                "notification.config.error.wrongUrl",
                NotificationType.ERROR
            )
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

    private fun isController(text: String): String? {
        return if (text.contains("@Controller") || text.contains("@RestController")) null else "notification.codegen.error.java.noController"
    }

    private fun hasRequestMapping(text: String): String? {
        return if (text.contains("@RequestMapping")) null else "notification.codegen.error.java.noRequestMapping"
    }
}
