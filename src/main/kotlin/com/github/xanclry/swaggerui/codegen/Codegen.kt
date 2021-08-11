package com.github.xanclry.swaggerui.codegen

import com.intellij.openapi.editor.Document
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.project.Project
import kotlin.reflect.KFunction1

interface Codegen {

    fun runChecks(code: String, vararg functions: KFunction1<String, String?>) : CodegenAvailability {
        val result = CodegenAvailability(true, null)
        for (f in functions.iterator()) {
            val r: String? = f.invoke(code)
            if (r != null) {
                result.reason = r
                result.isAvailable = false
                break
            }
        }
        return result
    }

    fun isFileSuitable(document: Document): CodegenAvailability
    fun generateCode(project: Project, editor: Editor): String
    fun offsetForNewCode(document: Document): Int
}