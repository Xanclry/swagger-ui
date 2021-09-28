package com.github.xanclry.swaggerui.codegen

import com.github.xanclry.swaggerui.model.file.PsiFileWithDirectory
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.PsiFile
import io.swagger.v3.oas.models.media.Schema

interface ModelGenerator {
    fun filterModels(schemas: Map<String, Schema<Any>>): Map<String, Schema<Any>>
    fun getModelFileExtension(): String
    fun getFilenameFromModelName(modelName: String): String
    fun generateModelPsiFile(filename: String, code: String = ""): PsiFile
    fun generateModelCode(modelName: String, packagePath: String, models: Map<String, Schema<Any>>): String
    fun computeListOfModelPsiFiles(
        sourceRoot: VirtualFile,
        models: Map<String, Schema<Any>>
    ): List<PsiFileWithDirectory>
}