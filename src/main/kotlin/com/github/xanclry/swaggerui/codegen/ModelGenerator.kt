package com.github.xanclry.swaggerui.codegen

import com.intellij.openapi.project.Project
import com.intellij.psi.PsiFile
import io.swagger.v3.oas.models.media.Schema

interface ModelGenerator {
    fun filterModels(schemas: Map<String, Schema<Any>>): Map<String, Schema<Any>>
    fun getModelFileExtension(): String
    fun getFilenameFromModelName(modelName: String): String
    fun generateModelPsiFile(project: Project, filename: String, code: String = ""): PsiFile
    fun generateModelCode(modelName: String, models: Map<String, Schema<Any>>): String
}