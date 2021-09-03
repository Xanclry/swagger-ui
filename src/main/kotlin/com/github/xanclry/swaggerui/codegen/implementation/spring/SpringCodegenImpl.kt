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

    override fun generateEndpointsCodeWithPath(project: Project, existingCode: String, path: String): String {
        try {
            val existingMappings: List<SwaggerMethodDto> = syntaxUtil.getEndpointsMappings(existingCode)
            val endpointsToCreate: List<OperationWithMethodDto> =
                endpointsUtil.getEndpointsToCreate(path, existingMappings)

            var accumulator = ""

            endpointsToCreate.forEach { endpoint ->
                accumulator += syntaxUtil.generateEndpointCode(endpoint, path).plus("\n\n")
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

    override fun generateEndpointsCodePathUnknown(project: Project, existingCode: String): String {
        val controllerPath = syntaxUtil.getControllerPath(existingCode)
        return generateEndpointsCodeWithPath(project, existingCode, controllerPath)
    }

    private fun generateControllerCode(path: String, code: String): String {
        val className = getClassname(path)
        return """
            |@org.springframework.web.bind.annotation.RestController
            |@org.springframework.web.bind.annotation.RequestMapping("$path")
            |public class $className {$code}
        """.trimMargin()
    }

    override fun generateController(
        path: String,
        project: Project,
        shouldOptimizeCode: Boolean,
        code: String
    ): PsiFile {
        val newPsiFile =
            createPsiFileWithController(project, path, code)
        if (shouldOptimizeCode) reformatAndOptimizeImports(newPsiFile, project)
        return newPsiFile
    }

    private fun createPsiFileWithController(project: Project, path: String, code: String): PsiFile {
        return PsiFileFactory.getInstance(project)
            .createFileFromText(getFilename(path), language, generateControllerCode(path, code))
    }

    private fun reformatAndOptimizeImports(psiFile: PsiFile, project: Project) {
        JavaCodeStyleManager.getInstance(project).shortenClassReferences(psiFile)
        CodeStyleManager.getInstance(project).reformat(psiFile)
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
