package com.github.xanclry.swaggerui.codegen.implementation.spring

import com.github.xanclry.swaggerui.codegen.EndpointsGenerator
import com.github.xanclry.swaggerui.codegen.GeneratedMethodsAdapter
import com.github.xanclry.swaggerui.codegen.exception.FileIsNotControllerException
import com.github.xanclry.swaggerui.codegen.exception.PathDontMatchException
import com.github.xanclry.swaggerui.codegen.implementation.spring.util.*
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
import io.swagger.v3.oas.models.media.Schema

class SpringEndpointsGeneratorImpl(project: Project) : EndpointsGenerator {

    private val syntaxUtil = SpringEndpointsSyntaxUtil()
    private val endpointsConfigurationFacade = EndpointsConfigurationFacade(project)
    private val language = Language.findLanguageByID("JAVA")!!
    private val codeParser = SpringSourceCodeParser()
    private val documentUtil = DocumentUtil()
    private val defaultConfig = SpringSmartGenerationConfiguration()

    override fun parseExistingMappings(code: String, fullPath: Boolean): List<SwaggerMethodDto> {
        return codeParser.getEndpointsMappings(code, fullPath)
    }

    override fun parsePathAndFilename(operationWithMethod: OperationWithMethodDto): FileMetadataDto {
        val tagWithDot = operationWithMethod.operation.tags.firstOrNull { it.contains('.') }
        val path: String
        val fileName: String
        if (tagWithDot != null) {
            path = tagWithDot.substringBeforeLast(".")
            fileName = tagWithDot.substringAfterLast('.')
        } else {
            path = defaultConfig.getControllerDefaultPath()
            fileName = defaultConfig.getControllerDefaultName()
        }
            return FileMetadataDto(path, fileName.plus(".").plus(getExtension()))
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
        operations: List<OperationWithMethodDto>,
        models: Map<String, Schema<Any>>
    ): PsiFile {
        val directory = documentUtil.createOrFindDirectory(project, scope, fileMetadataDto.packagePath)
        val controllerFile = documentUtil.findFileInDirectory(directory, fileMetadataDto.filename)
        controllerFile?.refresh(false, false)
        return if (controllerFile != null) {
            validateControllerAndAddMethods(controllerFile, controllerPath, operations, project, models)
        } else {
            generateMethodsAndController(operations, controllerPath, project, fileMetadataDto, directory, models)
        }
    }

    private fun generateMethodsAndController(
        operations: List<OperationWithMethodDto>,
        controllerPath: String,
        project: Project,
        fileMetadataDto: FileMetadataDto,
        directory: VirtualFile,
        models: Map<String, Schema<Any>>,
    ): PsiFile {
        val content = generateMethods(operations, controllerPath, project, models)
        val generatedController: PsiFile =
            generateController(controllerPath, project, code = content.asString(), fileMetadata = fileMetadataDto)
        documentUtil.createFileInDirectory(project, generatedController, directory)
        return generatedController
    }

    private fun validateControllerAndAddMethods(
        controllerFile: VirtualFile,
        controllerPath: String,
        operations: List<OperationWithMethodDto>,
        project: Project,
        models: Map<String, Schema<Any>>
    ): PsiFile {
        val controllerFileContent = documentUtil.loadText(controllerFile)
        if (isController(controllerFileContent).isController) {
            val controllerPathFromFile = parseControllerPath((controllerFileContent))
            if (controllerPath.startsWith(controllerPathFromFile)) {
                val content = generateMethods(operations, controllerPathFromFile, project, models)
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
        project: Project,
        models: Map<String, Schema<Any>>
    ): GeneratedMethodsAdapter {
        val accumulator = ArrayList<String>()
        endpointsToCreate.forEach { endpoint ->
            accumulator += syntaxUtil.generateEndpointCode(endpoint, controllerPath, models).plus("\n\n")
        }
        return SpringGeneratedMethodsAdapter(accumulator, project)
    }

    override fun generateEndpointsCodeWithPath(
        project: Project,
        existingCode: String,
        path: String,
        models: Map<String, Schema<Any>>
    ): GeneratedMethodsAdapter {
        try {
            val existingMappings: List<SwaggerMethodDto> = parseExistingMappings(existingCode, false)
            val endpointsToCreate: List<OperationWithMethodDto> =
                endpointsConfigurationFacade.identifyMissingEndpoints(path, existingMappings)

            return generateMethods(endpointsToCreate, path, project, models)
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

    override fun generateEndpointsCodePathUnknown(
        project: Project,
        existingCode: String,
        models: Map<String, Schema<Any>>
    ): GeneratedMethodsAdapter {
        val controllerPath = parseControllerPath(existingCode)
        return generateEndpointsCodeWithPath(project, existingCode, controllerPath, models)
    }

    private fun generateControllerCode(
        path: String,
        fileMetadata: FileMetadataDto?,
        classname: String,
        code: String = ""
    ): String {
        val apiAnnotation = if (fileMetadata != null) {
            val fullPath = SpringTypesUtil.generateFullTypeName(
                fileMetadata.packagePath,
                classname
            )
            "@io.swagger.annotations.Api(tags = \"$fullPath\")"
        } else {
            ""
        }
        return """
            |@org.springframework.web.bind.annotation.RestController
            |@org.springframework.web.bind.annotation.RequestMapping("$path")
            |$apiAnnotation
            |public class $classname {$code}
        """.trimMargin()
    }

    override fun generateController(
        path: String,
        project: Project,
        shouldOptimizeCode: Boolean,
        code: String,
        fileMetadata: FileMetadataDto?
    ): PsiFile {
        val newPsiFile =
            createPsiFileWithController(project, path, code, fileMetadata)
        if (shouldOptimizeCode) reformatAndOptimizeImports(newPsiFile, project)
        return newPsiFile
    }

    private fun createPsiFileWithController(
        project: Project,
        path: String,
        code: String,
        fileMetadata: FileMetadataDto? = null
    ): PsiFile {
        val newFilename = fileMetadata?.filename ?: generateFilename(path)
        val classname = newFilename.replace(".".plus(getExtension()), "")
        return PsiFileFactory.getInstance(project)
            .createFileFromText(newFilename, language, generateControllerCode(path, fileMetadata, classname, code))
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
