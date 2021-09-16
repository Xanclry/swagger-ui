package com.github.xanclry.swaggerui.codegen

import com.github.xanclry.swaggerui.model.ControllerFileMetadata
import com.github.xanclry.swaggerui.model.OperationWithMethodDto
import com.github.xanclry.swaggerui.model.SwaggerMethodDto
import com.github.xanclry.swaggerui.model.file.FileMetadataDto
import com.intellij.openapi.editor.Document
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import kotlin.reflect.KFunction1

interface EndpointsGenerator {

    fun runChecks(code: String, vararg functions: KFunction1<String, String?>): ControllerFileMetadata {
        val result = ControllerFileMetadata(true, null)
        for (f in functions.iterator()) {
            val r: String? = f.invoke(code)
            if (r != null) {
                result.error = r
                result.isController = false
                break
            }
        }
        return result
    }

    fun parseExistingMappings(code: String, fullPath: Boolean): List<SwaggerMethodDto>
    fun parsePathAndFilename(operationWithMethod: OperationWithMethodDto): FileMetadataDto
    fun parseControllerPath(existingCode: String): String

    fun isController(document: Document): ControllerFileMetadata
    fun isController(text: String): ControllerFileMetadata
    fun generateFilename(path: String): String

    fun generateController(
        path: String,
        project: Project,
        shouldOptimizeCode: Boolean = true,
        code: String = "",
        filename: String? = null
    ): PsiFile

    fun createOrFindControllerAndGenerateMethods(
        project: Project,
        scope: VirtualFile,
        fileMetadataDto: FileMetadataDto,
        controllerPath: String,
        operations: List<OperationWithMethodDto>
    ): PsiFile

    fun addMethodsToPsiFile(psiMethods: GeneratedMethodsAdapter?, psiFile: PsiFile, project: Project)
    fun generateEndpointsCodePathUnknown(project: Project, existingCode: String): GeneratedMethodsAdapter
    fun generateEndpointsCodeWithPath(project: Project, existingCode: String, path: String): GeneratedMethodsAdapter
    fun reformatAndOptimizeImports(psiElement: PsiElement, project: Project)
    fun getExtension(): String
    fun generateMethods(
        endpointsToCreate: List<OperationWithMethodDto>,
        controllerPath: String,
        project: Project
    ): GeneratedMethodsAdapter
}
