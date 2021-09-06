package com.github.xanclry.swaggerui.codegen

import com.github.xanclry.swaggerui.codegen.exception.LanguageNotSupportedException
import com.intellij.lang.Language

enum class Language {
    SPRING;

    companion object {
        fun parseJetbrainsLanguage(lang: Language): com.github.xanclry.swaggerui.codegen.Language {
            return when (lang.id) {
                "JAVA" -> SPRING
                else -> {
                    throw LanguageNotSupportedException("Language ${lang.id} not supported yet")
                }
            }
        }
    }
}
