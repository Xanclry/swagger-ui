package com.github.xanclry.swaggerui.codegen.implementation.spring

import com.github.xanclry.swaggerui.codegen.Codegen
import com.github.xanclry.swaggerui.codegen.CodegenAvailability
import com.github.xanclry.swaggerui.codegen.implementation.spring.util.SpringSyntaxUtil
import com.github.xanclry.swaggerui.codegen.util.EndpointsUtil
import com.github.xanclry.swaggerui.model.OperationWithMethodDto
import com.github.xanclry.swaggerui.model.SwaggerMethodDto
import com.github.xanclry.swaggerui.util.Notifier
import com.intellij.lang.Language
import com.intellij.notification.NotificationType
import com.intellij.openapi.editor.Document
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiFile
import com.intellij.psi.PsiFileFactory
import com.intellij.psi.codeStyle.CodeStyleManager
import com.intellij.psi.codeStyle.JavaCodeStyleManager

class SpringCodegenImpl(project: Project) : Codegen {

    private val syntaxUtil = SpringSyntaxUtil()
    private val endpointsUtil = EndpointsUtil(project)
    private val language = Language.findLanguageByID("JAVA")!!

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

    private fun generateEmptyControllerCode(path: String): String {
        val className = getClassname(path)
        return """
            |@org.springframework.web.bind.annotation.RestController
            |@org.springframework.web.bind.annotation.RequestMapping("$path")
            |public class $className {}
        """.trimMargin()
    }

    override fun generateEmptyController(path: String, project: Project): PsiFile {
        val newPsiFile =
            PsiFileFactory.getInstance(project).createFileFromText(getFilename(path), language, generateEmptyControllerCode(path))
        JavaCodeStyleManager.getInstance(project).shortenClassReferences(newPsiFile)
        CodeStyleManager.getInstance(project).reformat(newPsiFile)
        return newPsiFile
    }

    override fun offsetForNewCode(document: Document): Int {
        return document.text.lastIndexOf("}") - 1
    }

    private fun isController(text: String): String? {
        return if (text.contains("@Controller") || text.contains("@RestController")) null
        else "notification.codegen.error.java.noController"
    }

    private fun hasRequestMapping(text: String): String? {
        return if (text.contains("@RequestMapping")) null else "notification.codegen.error.java.noRequestMapping"
    }

    private fun getClassname(path: String): String {
        val reg = Regex("([/].)")
        return path
            .replace(reg) { matchResult: MatchResult -> matchResult.value.toUpperCase().substring(1) }
            .replace("Api", "", true)
            .replace("/", "")
            .replace("Rest", "", true)
            .plus("RestController")
    }

    private fun getExtension(): String {
        return ".java"
    }

    override fun getFilename(path: String): String {
        return getClassname(path)
            .plus(getExtension())
    }
}
