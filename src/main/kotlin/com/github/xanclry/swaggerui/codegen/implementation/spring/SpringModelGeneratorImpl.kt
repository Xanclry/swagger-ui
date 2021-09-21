package com.github.xanclry.swaggerui.codegen.implementation.spring

import com.github.xanclry.swaggerui.codegen.ModelGenerator
import com.github.xanclry.swaggerui.codegen.implementation.spring.util.SpringModelSyntaxUtil
import com.intellij.lang.Language
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiFile
import com.intellij.psi.PsiFileFactory
import io.swagger.v3.oas.models.media.Schema

class SpringModelGeneratorImpl(val project: Project) : ModelGenerator {

    private val modelSyntaxUtil = SpringModelSyntaxUtil()

    private val language = Language.findLanguageByID("JAVA")!!

    override fun filterModels(schemas: Map<String, Schema<Any>>): Map<String, Schema<Any>> {
        return schemas.filterNot { (key, _) -> key.contains(Regex("[<>«»]")) }
    }

    override fun getFilenameFromModelName(modelName: String): String {
        return modelName.plus(getModelFileExtension())
    }

    override fun getModelFileExtension(): String {
        return ".java"
    }

    override fun generateModelPsiFile(filename: String, code: String): PsiFile {
        return PsiFileFactory.getInstance(project)
            .createFileFromText(filename, language, code)
    }

    override fun generateModelCode(modelName: String, packagePath: String, models: Map<String, Schema<Any>>): String {
        return modelSyntaxUtil.generateModelCode(modelName, packagePath, models)
    }

}