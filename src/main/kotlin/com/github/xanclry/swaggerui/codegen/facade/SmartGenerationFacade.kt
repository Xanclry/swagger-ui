package com.github.xanclry.swaggerui.codegen.facade

import com.github.xanclry.swaggerui.codegen.CodegenFactory
import com.github.xanclry.swaggerui.codegen.Language
import com.github.xanclry.swaggerui.codegen.exception.FileIsNotControllerException
import com.github.xanclry.swaggerui.codegen.exception.PathDontMatchException
import com.github.xanclry.swaggerui.model.OperationWithMethodDto
import com.github.xanclry.swaggerui.model.SwaggerMethodDto
import com.github.xanclry.swaggerui.model.file.FileMetadataDto
import com.github.xanclry.swaggerui.services.facade.EndpointsConfigurationFacade
import com.github.xanclry.swaggerui.services.facade.ModelConfigurationFacade
import com.github.xanclry.swaggerui.util.DocumentUtil
import com.github.xanclry.swaggerui.util.Notifier
import com.intellij.notification.NotificationType
import com.intellij.openapi.command.WriteCommandAction
import com.intellij.openapi.fileEditor.FileDocumentManager
import com.intellij.openapi.module.Module
import com.intellij.openapi.project.Project
import com.intellij.openapi.roots.ModuleRootManager
import com.intellij.openapi.vfs.VfsUtilCore
import com.intellij.openapi.vfs.VirtualFile
import io.swagger.v3.oas.models.media.Schema

class SmartGenerationFacade(language: Language, private val project: Project) {

    private val endpointsGenerator = CodegenFactory.factoryMethod(language).createEndpointsGenerator(project)
    private val modelGenerator = CodegenFactory.factoryMethod(language).createModelGenerator(project)
    private val fileDocumentManager = FileDocumentManager.getInstance()
    private val endpointsConfigurationFacade = EndpointsConfigurationFacade(project)
    private val modelConfigurationFacade = ModelConfigurationFacade(project)
    private val documentUtil = DocumentUtil()

    fun runSmartGeneration(webModule: Module, modelModule: Module) {
        val webSourceRoots: Array<VirtualFile> = ModuleRootManager.getInstance(webModule).sourceRoots
        val modelSourceRoots: Array<VirtualFile> = ModuleRootManager.getInstance(webModule).sourceRoots
        var editedFilesCounter = 0
        try {
            val webSourceRoot = filterSourceRoots(webSourceRoots)
            val modelSourceRoot = filterSourceRoots(modelSourceRoots)
            val fileOperationsMap = computeEndpointsOperationMap(webSourceRoot)
            val modelOperations = computeModelOperationMap(modelSourceRoot)

            WriteCommandAction.runWriteCommandAction(project) {
                fileOperationsMap.forEach { (fileMetadata, operationList) ->
                    val controllerPath =
                        endpointsConfigurationFacade.findEndpointsCommonPrefix(operationList.map { it.path })
                    endpointsGenerator.createOrFindControllerAndGenerateMethods(
                        project,
                        webSourceRoot,
                        fileMetadata,
                        controllerPath,
                        operationList
                    )
                    editedFilesCounter++
                }
            }

        } catch (e: FileIsNotControllerException) {
            Notifier.notifyProjectWithContentBeforeBundleMessage(
                project,
                e.message!!,
                "notification.codegen.smart.error.fileIsNotController",
                NotificationType.ERROR
            )
        } catch (e: PathDontMatchException) {
            Notifier.notifyProject(project, e.message!!, NotificationType.ERROR)
        } catch (e: Exception) {
            Notifier.notifyProjectWithMessageFromBundle(project, "notification.codegen.error", NotificationType.ERROR)
        } finally {
            Notifier.notifyProjectWithContentAfterBundleMessage(
                project,
                editedFilesCounter.toString(),
                "notification.codegen.smart.success.filesAffected",
                NotificationType.INFORMATION
            )
        }
    }

    private fun filterSourceRoots(sourceRoots: Array<VirtualFile>): VirtualFile {
        return sourceRoots.filter { !it.path.contains("test") && !it.path.contains("resources") }[0]
    }

    private fun computeEndpointsOperationMap(sourceRoot: VirtualFile): Map<FileMetadataDto, List<OperationWithMethodDto>> {
        val existingMappings: MutableSet<SwaggerMethodDto> = HashSet()
        iterateVirtualFile(sourceRoot, existingMappings)
        val missingEndpoints: List<OperationWithMethodDto> =
            endpointsConfigurationFacade.identifyMissingEndpointsInProject(existingMappings)
        return endpointsConfigurationFacade.computeFileOperationsMap(missingEndpoints, endpointsGenerator::parsePathAndFilename)
    }

    private fun computeModelOperationMap(sourceRoot: VirtualFile): Any {
        val allModels: MutableMap<String, Schema<Any>>? = modelConfigurationFacade.parseModels()
        if (allModels != null) {
            val filteredModels = modelGenerator.filterModels(allModels)
            filteredModels.entries.forEach {
                val packagePath = it.value.description
                val directory = documentUtil.createOrFindDirectory(project, sourceRoot, packagePath)
                val filename = modelGenerator.getFilenameFromModelName(it.key)
                val modelFile = documentUtil.findFileInDirectory(directory, filename)
                if (modelFile == null) {
                    val modelFileContent = modelGenerator.generateModelCode(it.key, filteredModels)
                    val newPsiModelFile = modelGenerator.generateModelPsiFile(project, filename, modelFileContent)
                } else {

                }
                println()
            }
        }
        return Any()
    }

    private fun iterateVirtualFile(virtualFile: VirtualFile, existingMapping: MutableSet<SwaggerMethodDto>) {

        VfsUtilCore.iterateChildrenRecursively(virtualFile, {
            true
        }, iterator@{
            if (!it.isDirectory && it.extension == endpointsGenerator.getExtension()) {
                addExistingEndpointsToList(it, existingMapping)
            }
            return@iterator true
        })
    }

    private fun addExistingEndpointsToList(
        virtualFile: VirtualFile,
        existingMapping: MutableSet<SwaggerMethodDto>
    ) {
        existingMapping.addAll(endpointsGenerator.parseExistingMappings(fileDocumentManager.getDocument(virtualFile)!!.text, true))
    }

}