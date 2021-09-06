package com.github.xanclry.swaggerui.codegen.factory

import com.github.xanclry.swaggerui.codegen.Codegen
import com.github.xanclry.swaggerui.codegen.CodegenFactory
import com.github.xanclry.swaggerui.codegen.implementation.spring.SpringCodegenImpl
import com.intellij.openapi.project.Project

class SpringCodegenFactory : CodegenFactory() {
    override fun createCodegen(project: Project): Codegen {
        return SpringCodegenImpl(project)
    }
}
