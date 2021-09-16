package com.github.xanclry.swaggerui.codegen

import com.github.xanclry.swaggerui.codegen.exception.LanguageNotSupportedException
import com.github.xanclry.swaggerui.codegen.factory.SpringCodegenFactory
import com.intellij.openapi.project.Project

abstract class CodegenFactory {
    companion object {
        fun factoryMethod(language: Language): CodegenFactory {
            when (language) {
                Language.SPRING -> return SpringCodegenFactory()
                else -> {
                    throw LanguageNotSupportedException("Language $language not supported yet")
                }
            }
        }
    }

    abstract fun createEndpointsGenerator(project: Project): EndpointsGenerator

    abstract fun createModelGenerator(project: Project): ModelGenerator

}
