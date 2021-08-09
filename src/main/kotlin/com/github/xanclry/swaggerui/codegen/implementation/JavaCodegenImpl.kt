package com.github.xanclry.swaggerui.codegen.implementation

import com.github.xanclry.swaggerui.codegen.Codegen
import com.github.xanclry.swaggerui.codegen.CodegenAvailability
import com.github.xanclry.swaggerui.model.SwaggerMethod
import com.intellij.openapi.editor.Document
import kotlin.reflect.KFunction1

class JavaCodegenImpl : Codegen {
    override fun isFileSuitable(document: Document): CodegenAvailability {
        val code = document.text
        return runChecks(code, ::isController, ::hasRequestMapping)
    }

    override fun generateCode(): String {
        TODO("Not yet implemented")
    }

    private fun isController(text: String): String? {
        return if (text.contains("@Controller") || text.contains("@RestController")) {
            null
        } else {
            "notification.codegen.error.java.noController"
        }
    }

    private fun hasRequestMapping(text: String): String? {
        return if (text.contains("@RequestMapping")) {
            null
        } else {
            "notification.codegen.error.java.noRequestMapping"
        }
    }
}