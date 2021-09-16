package com.github.xanclry.swaggerui.codegen.implementation.spring.util

import com.github.xanclry.swaggerui.codegen.GeneratedMethodsAdapter
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiMethod
import com.intellij.psi.impl.PsiJavaParserFacadeImpl
import java.util.stream.Collectors

class SpringGeneratedMethodsAdapter(private val methodList: List<String>, private val project: Project) :
    GeneratedMethodsAdapter {

    override fun asString(): String {
        return methodList.stream().reduce { acc, string -> acc + string }.orElse("")
    }

    override fun asPsiList(context: PsiElement?): List<PsiMethod> {
        val psiJavaParserFacadeImpl = PsiJavaParserFacadeImpl(project)
        return methodList.stream()
            .map { psiJavaParserFacadeImpl.createMethodFromText(it, context) }
            .collect(Collectors.toList())
    }

}
