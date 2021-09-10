package com.github.xanclry.swaggerui.codegen.facade

import com.github.xanclry.swaggerui.codegen.CodegenFactory
import com.github.xanclry.swaggerui.codegen.Language
import com.github.xanclry.swaggerui.codegen.exception.FileIsNotControllerException
import com.github.xanclry.swaggerui.codegen.exception.PathDontMatchException
import com.github.xanclry.swaggerui.model.OperationWithMethodDto
import com.github.xanclry.swaggerui.model.SwaggerMethodDto
import com.github.xanclry.swaggerui.model.file.FileMetadataDto
import com.github.xanclry.swaggerui.services.EndpointsConfigurationFacade
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

    private val codegen = CodegenFactory.factoryMethod(language).createCodegen(project)
    private val fileDocumentManager = FileDocumentManager.getInstance()
    private val endpointsConfigurationFacade = EndpointsConfigurationFacade(project)

    fun runSmartGeneration(module: Module) {
        val sourceRoots = ModuleRootManager.getInstance(module).sourceRoots
        var editedFilesCounter = 0
        try {
            val existingMappings: MutableSet<SwaggerMethodDto> = HashSet()
            val sourceRoot: VirtualFile =
                sourceRoots.filter { !it.path.contains("test") && !it.path.contains("resources") }[0]
            iterateVirtualFile(sourceRoot, existingMappings)
            val missingEndpoints: List<OperationWithMethodDto> =
                endpointsConfigurationFacade.identifyMissingEndpointsInProject(existingMappings)
            val fileOperationsMap: Map<FileMetadataDto, List<OperationWithMethodDto>> =
                endpointsConfigurationFacade.computeFileOperationsMap(missingEndpoints, codegen::parsePathAndFilename)

            WriteCommandAction.runWriteCommandAction(project) {
                fileOperationsMap.forEach { (fileMetadata, operationList) ->
                    val controllerPath =
                        endpointsConfigurationFacade.findEndpointsCommonPrefix(operationList.map { it.path })
                    codegen.createOrFindControllerAndGenerateMethods(project, sourceRoot, fileMetadata, controllerPath, operationList)
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
            Notifier.notifyProjectWithContentAfterBundleMessage(project, editedFilesCounter.toString(), "notification.codegen.smart.success.filesAffected", NotificationType.INFORMATION)
        }
    }

    private fun iterateVirtualFile(virtualFile: VirtualFile, existingMapping: MutableSet<SwaggerMethodDto>) {

        VfsUtilCore.iterateChildrenRecursively(virtualFile, {
            true
        }, iterator@{
            if (!it.isDirectory && it.extension == codegen.getExtension()) {
                addExistingEndpointsToList(it, existingMapping)
            }
            return@iterator true
        })
    }

    private fun addExistingEndpointsToList(
        virtualFile: VirtualFile,
        existingMapping: MutableSet<SwaggerMethodDto>
    ) {
        existingMapping.addAll(codegen.parseExistingMappings(fileDocumentManager.getDocument(virtualFile)!!.text, true))
    }

}