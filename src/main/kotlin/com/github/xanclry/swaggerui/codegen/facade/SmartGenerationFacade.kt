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

class SmartGenerationFacade(language: Language, private val project: Project) {

    private val endpointsGenerator = CodegenFactory.factoryMethod(language).createEndpointsGenerator(project)
    private val modelGenerator = CodegenFactory.factoryMethod(language).createModelGenerator(project)
    private val fileDocumentManager = FileDocumentManager.getInstance()
    private val endpointsConfigurationFacade = EndpointsConfigurationFacade(project)
    private val modelConfigurationFacade = ModelConfigurationFacade(project)
    private val documentUtil = DocumentUtil()

    fun runSmartGeneration(webModule: Module, modelModule: Module) {
        val webSourceRoots: Array<VirtualFile> = ModuleRootManager.getInstance(webModule).sourceRoots
        val modelSourceRoots: Array<VirtualFile> = ModuleRootManager.getInstance(modelModule).sourceRoots
        var editedFilesCounter = 0
        try {
            val webSourceRoot = filterSourceRoots(webSourceRoots)
            val fileWithOperationsMap = computeEndpointsOperationMap(webSourceRoot)

            val modelSourceRoot = filterSourceRoots(modelSourceRoots)
            val filteredModelsMap = modelConfigurationFacade.getFilteredModelsFromConfig(modelGenerator)

            WriteCommandAction.runWriteCommandAction(project) {

                filteredModelsMap.entries.forEach {
                    val model = modelGenerator.parseModelMapEntry(it, modelSourceRoot, filteredModelsMap)
                    if (model != null) {

                        endpointsGenerator.reformatAndOptimizeImports(model.psiFile, project)
                        documentUtil.createFileInDirectory(project, model.psiFile, model.directory)
                        editedFilesCounter++
                    }
                }

                fileWithOperationsMap.forEach { (fileMetadata, operationList) ->
                    val controllerPath =
                        endpointsConfigurationFacade.findEndpointsCommonPrefix(operationList.map { it.path })
                    endpointsGenerator.createOrFindControllerAndGenerateMethods(
                        project,
                        webSourceRoot,
                        fileMetadata,
                        controllerPath,
                        operationList,
                        filteredModelsMap
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
        } catch (e: IllegalArgumentException) {
            Notifier.notifyProjectWithMessageFromBundle(project, "notification.config.error.wrongUrl", NotificationType.ERROR)
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
        return endpointsConfigurationFacade.computeFileOperationsMap(
            missingEndpoints,
            endpointsGenerator::parsePathAndFilename
        )
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
        allExistingMappings: MutableSet<SwaggerMethodDto>
    ) {
        val existingMappingsFromFile: List<SwaggerMethodDto> = endpointsGenerator.parseExistingMappings(
            fileDocumentManager.getDocument(virtualFile)!!.text,
            true
        )
        existingMappingsFromFile.forEach { newMapping: SwaggerMethodDto ->
            val samePath = allExistingMappings.find { it.path == newMapping.path }
            if (samePath != null) {
                samePath.methodSet.addAll(newMapping.methodSet)
            } else {
                allExistingMappings.add(newMapping)
            }
        }
    }

}