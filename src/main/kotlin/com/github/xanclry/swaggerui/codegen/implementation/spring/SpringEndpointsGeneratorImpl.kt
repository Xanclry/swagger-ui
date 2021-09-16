package com.github.xanclry.swaggerui.codegen.implementation.spring

import com.github.xanclry.swaggerui.codegen.EndpointsGenerator
import com.github.xanclry.swaggerui.codegen.GeneratedMethodsAdapter
import com.github.xanclry.swaggerui.codegen.exception.FileIsNotControllerException
import com.github.xanclry.swaggerui.codegen.exception.PathDontMatchException
import com.github.xanclry.swaggerui.codegen.implementation.spring.util.SpringGeneratedMethodsAdapter
import com.github.xanclry.swaggerui.codegen.implementation.spring.util.SpringSourceCodeParser
import com.github.xanclry.swaggerui.codegen.implementation.spring.util.SpringSyntaxUtil
import com.github.xanclry.swaggerui.model.ControllerFileMetadata
import com.github.xanclry.swaggerui.model.OperationWithMethodDto
import com.github.xanclry.swaggerui.model.SwaggerMethodDto
import com.github.xanclry.swaggerui.model.file.FileMetadataDto
import com.github.xanclry.swaggerui.services.facade.EndpointsConfigurationFacade
import com.github.xanclry.swaggerui.util.DocumentUtil
import com.github.xanclry.swaggerui.util.Notifier
import com.intellij.lang.Language
import com.intellij.notification.NotificationType
import com.intellij.openapi.editor.Document
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.*
import com.intellij.psi.codeStyle.CodeStyleManager
import com.intellij.psi.codeStyle.JavaCodeStyleManager

class SpringEndpointsGeneratorImpl(project: Project) : EndpointsGenerator {

    private val syntaxUtil = SpringSyntaxUtil()
    private val endpointsConfigurationFacade = EndpointsConfigurationFacade(project)
    private val language = Language.findLanguageByID("JAVA")!!
    private val codeParser = SpringSourceCodeParser()
    private val documentUtil = DocumentUtil()

    override fun parseExistingMappings(code: String, fullPath: Boolean): List<SwaggerMethodDto> {
        return codeParser.getEndpointsMappings(code, fullPath)
    }

    override fun parsePathAndFilename(operationWithMethod: OperationWithMethodDto): FileMetadataDto {
        val tagWithDot = operationWithMethod.operation.tags.first { it.contains('.') }
        val path = tagWithDot.substringBeforeLast(".")
        val fileName = tagWithDot.substringAfterLast('.').plus(".").plus(getExtension())
        return FileMetadataDto(path, fileName)
    }

    override fun isController(document: Document): ControllerFileMetadata {
        val code = document.text
        return runChecks(code, codeParser::isController, codeParser::hasRequestMapping)
    }

    override fun isController(text: String): ControllerFileMetadata {
        return runChecks(text, codeParser::isController, codeParser::hasRequestMapping)
    }

    override fun createOrFindControllerAndGenerateMethods(
        project: Project,
        scope: VirtualFile,
        fileMetadataDto: FileMetadataDto,
        controllerPath: String,
        operations: List<OperationWithMethodDto>
    ): PsiFile {
        val directory = documentUtil.createOrFindDirectory(project, scope, fileMetadataDto.packagePath)
        val controllerFile = directory.findChild(fileMetadataDto.filename)
        controllerFile?.refresh(false, false)
        return if (controllerFile != null) {
            validateControllerAndAddMethods(controllerFile, controllerPath, operations, project)
        } else {
            generateMethodsAndController(operations, controllerPath, project, fileMetadataDto, directory)
        }
    }

    private fun generateMethodsAndController(
        operations: List<OperationWithMethodDto>,
        controllerPath: String,
        project: Project,
        fileMetadataDto: FileMetadataDto,
        directory: VirtualFile
    ): PsiFile {
        val content = generateMethods(operations, controllerPath, project)
        val generatedController: PsiFile =
            generateController(controllerPath, project, code = content.asString(), filename = fileMetadataDto.filename)
        documentUtil.createFileInDirectory(project, generatedController, directory)
        return generatedController
    }

    private fun validateControllerAndAddMethods(
        controllerFile: VirtualFile,
        controllerPath: String,
        operations: List<OperationWithMethodDto>,
        project: Project
    ): PsiFile {
        val controllerFileContent = documentUtil.loadText(controllerFile)
        if (isController(controllerFileContent).isController) {
            val controllerPathFromFile = parseControllerPath((controllerFileContent))
            if (controllerPath.startsWith(controllerPathFromFile)) {
                val content = generateMethods(operations, controllerPathFromFile, project)
                val foundControllerFile: PsiFile = PsiManager.getInstance(project).findFile(controllerFile)!!
                addMethodsToPsiFile(content, foundControllerFile, project)
                return foundControllerFile
            } else {
                throw PathDontMatchException(controllerPathFromFile, controllerPath)
            }
        } else {
            throw FileIsNotControllerException(controllerFile.name)
        }
    }

    override fun addMethodsToPsiFile(psiMethods: GeneratedMethodsAdapter?, psiFile: PsiFile, project: Project) {
        if (psiMethods != null) {
            val classes: Array<PsiClass> = (psiFile as PsiJavaFile).classes
            psiMethods.asPsiList(psiFile).forEach { classes[0].add(it) }
            reformatAndOptimizeImports(psiFile, project)
        }
    }

    override fun generateMethods(
        endpointsToCreate: List<OperationWithMethodDto>,
        controllerPath: String,
        project: Project
    ): GeneratedMethodsAdapter {
        val accumulator = ArrayList<String>()
        endpointsToCreate.forEach { endpoint ->
            accumulator += syntaxUtil.generateEndpointCode(endpoint, controllerPath).plus("\n\n")
        }
        return SpringGeneratedMethodsAdapter(accumulator, project)
    }

    override fun generateEndpointsCodeWithPath(
        project: Project,
        existingCode: String,
        path: String
    ): GeneratedMethodsAdapter {
        try {
            val existingMappings: List<SwaggerMethodDto> = parseExistingMappings(existingCode, false)
            val endpointsToCreate: List<OperationWithMethodDto> =
                endpointsConfigurationFacade.identifyMissingEndpoints(path, existingMappings)

            return generateMethods(endpointsToCreate, path, project)
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
        return SpringGeneratedMethodsAdapter(ArrayList(), project)
    }

    override fun parseControllerPath(existingCode: String): String {
        return codeParser.getControllerPath(existingCode)
    }

    override fun generateEndpointsCodePathUnknown(project: Project, existingCode: String): GeneratedMethodsAdapter {
        val controllerPath = parseControllerPath(existingCode)
        return generateEndpointsCodeWithPath(project, existingCode, controllerPath)
    }

    private fun generateControllerCode(path: String, classname: String, code: String = ""): String {
        return """
            |@org.springframework.web.bind.annotation.RestController
            |@org.springframework.web.bind.annotation.RequestMapping("$path")
            |public class $classname {$code}
        """.trimMargin()
    }

    override fun generateController(
        path: String,
        project: Project,
        shouldOptimizeCode: Boolean,
        code: String,
        filename: String?
    ): PsiFile {
        val newPsiFile =
            createPsiFileWithController(project, path, code, filename)
        if (shouldOptimizeCode) reformatAndOptimizeImports(newPsiFile, project)
        return newPsiFile
    }

    private fun createPsiFileWithController(
        project: Project,
        path: String,
        code: String,
        filename: String? = null
    ): PsiFile {
        val newFilename = filename ?: generateFilename(path)
        val classname = newFilename.replace(".".plus(getExtension()), "")
        return PsiFileFactory.getInstance(project)
            .createFileFromText(newFilename, language, generateControllerCode(path, classname, code))
    }


    override fun reformatAndOptimizeImports(psiElement: PsiElement, project: Project) {
        JavaCodeStyleManager.getInstance(project).shortenClassReferences(psiElement)
        CodeStyleManager.getInstance(project).reformat(psiElement)
    }

    private fun generateClassnameFromControllerPath(path: String): String {
        val reg = Regex("([/].)")
        return path
            .replace(reg) { matchResult: MatchResult -> matchResult.value.toUpperCase().substring(1) }
            .replace("Api", "", true)
            .replace("/", "")
            .replace("Rest", "", true)
            .plus("RestController")
    }

    override fun getExtension(): String {
        return "java"
    }

    override fun generateFilename(path: String): String {
        return generateClassnameFromControllerPath(path)
            .plus(".")
            .plus(getExtension())
    }
}
