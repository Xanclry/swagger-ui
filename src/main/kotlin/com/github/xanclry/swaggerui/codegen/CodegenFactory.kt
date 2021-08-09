package com.github.xanclry.swaggerui.codegen

import com.github.xanclry.swaggerui.codegen.exception.LanguageNotSupportedException
import com.github.xanclry.swaggerui.codegen.factory.JavaCodegenFactory

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

    abstract fun createCodegen(): Codegen
}