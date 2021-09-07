package com.github.xanclry.swaggerui.codegen.facade

import com.github.xanclry.swaggerui.codegen.CodegenFactory
import com.github.xanclry.swaggerui.codegen.Language
import com.github.xanclry.swaggerui.model.SwaggerMethodDto
import com.github.xanclry.swaggerui.util.Notifier
import com.intellij.notification.NotificationType
import com.intellij.openapi.fileEditor.FileDocumentManager
import com.intellij.openapi.module.Module
import com.intellij.openapi.project.Project
import com.intellij.openapi.roots.ModuleRootManager
import com.intellij.openapi.vfs.VfsUtilCore
import com.intellij.openapi.vfs.VirtualFile

class SmartGenerationFacade(language: Language, private val project: Project) {

    private val codegen = CodegenFactory.factoryMethod(language).createCodegen(project)
    private val fileDocumentManager = FileDocumentManager.getInstance()

    fun runSmartGeneration(module: Module) {
        val sourceRoots = ModuleRootManager.getInstance(module).sourceRoots
        try {
            val existingMappings: MutableSet<SwaggerMethodDto> = HashSet()
            sourceRoots.forEach { iterateVirtualFile(it, existingMappings) }
            println()
        } catch (e: Exception) {
            Notifier.notifyProjectWithMessageFromBundle(project, "notification.codegen.error", NotificationType.ERROR)
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