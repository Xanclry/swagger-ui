package com.github.xanclry.swaggerui.codegen

import com.intellij.psi.PsiElement
import com.intellij.psi.PsiMethod

interface GeneratedMethodsAdapter {
    fun asString(): String
    fun asPsiList(context: PsiElement?): List<PsiMethod>
}