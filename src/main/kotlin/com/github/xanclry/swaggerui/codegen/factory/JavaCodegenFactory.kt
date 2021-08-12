package com.github.xanclry.swaggerui.codegen.factory

import com.github.xanclry.swaggerui.codegen.Codegen
import com.github.xanclry.swaggerui.codegen.CodegenFactory
import com.github.xanclry.swaggerui.codegen.implementation.java.JavaCodegenImpl
import com.intellij.openapi.project.Project

class JavaCodegenFactory : CodegenFactory() {
    override fun createCodegen(project: Project): Codegen {
        return JavaCodegenImpl(project)
    }
}