package com.github.xanclry.swaggerui.codegen

import com.github.xanclry.swaggerui.codegen.exception.LanguageNotSupportedException
import com.github.xanclry.swaggerui.codegen.factory.JavaCodegenFactory
import com.intellij.openapi.project.Project

abstract class CodegenFactory {
    companion object {
        fun factoryMethod(language: Language): CodegenFactory {
            when (language) {
                Language.JAVA -> return JavaCodegenFactory()
                else -> {
                    throw LanguageNotSupportedException("Language $language not supported yet")
                }
            }
        }
    }

    abstract fun createCodegen(project: Project): Codegen
}