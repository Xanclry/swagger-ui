package com.github.xanclry.swaggerui.codegen.factory

import com.github.xanclry.swaggerui.codegen.CodegenFactory
import com.github.xanclry.swaggerui.codegen.EndpointsGenerator
import com.github.xanclry.swaggerui.codegen.ModelGenerator
import com.github.xanclry.swaggerui.codegen.implementation.spring.SpringEndpointsGeneratorImpl
import com.github.xanclry.swaggerui.codegen.implementation.spring.SpringModelGeneratorImpl
import com.intellij.openapi.project.Project

class SpringCodegenFactory : CodegenFactory() {
    override fun createEndpointsGenerator(project: Project): EndpointsGenerator {
        return SpringEndpointsGeneratorImpl(project)
    }

    override fun createModelGenerator(project: Project): ModelGenerator {
        return SpringModelGeneratorImpl(project)
    }
}
