package com.github.xanclry.swaggerui.model.file

import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.PsiFile

data class PsiFileWithDirectory(val psiFile: PsiFile, val directory: VirtualFile)
