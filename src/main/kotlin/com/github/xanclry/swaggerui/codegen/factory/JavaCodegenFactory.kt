package com.github.xanclry.swaggerui.codegen.factory

import com.github.xanclry.swaggerui.codegen.Codegen
import com.github.xanclry.swaggerui.codegen.CodegenFactory
import com.github.xanclry.swaggerui.codegen.implementation.java.JavaCodegenImpl

class JavaCodegenFactory : CodegenFactory() {
    override fun createCodegen(): Codegen {
        return JavaCodegenImpl()
    }
}