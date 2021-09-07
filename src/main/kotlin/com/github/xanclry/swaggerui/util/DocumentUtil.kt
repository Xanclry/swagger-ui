package com.github.xanclry.swaggerui.util

import com.intellij.openapi.editor.Document
import com.intellij.openapi.fileEditor.FileDocumentManager
import com.intellij.openapi.fileEditor.FileEditorManager
import com.intellij.openapi.fileEditor.OpenFileDescriptor
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiDirectory
import com.intellij.psi.PsiDocumentManager
import com.intellij.psi.PsiFile

class DocumentUtil {

    companion object {
        fun openEditorOnNewFile(
            project: Project,
            document: Document?
        ) {
            val fileEditorManager = FileEditorManager.getInstance(project)
            val virtualFile = FileDocumentManager.getInstance().getFile(document!!)
            if (virtualFile != null) {
                fileEditorManager.openTextEditor(OpenFileDescriptor(project, virtualFile), true)
            }
        }
    }

    fun commitNewFile(
        project: Project,
        psiDirectory: PsiDirectory,
        newPsiFile: PsiFile,
    ): Document {
        val savedPsiFile = psiDirectory.add(newPsiFile)
        val psiDocumentManager = PsiDocumentManager.getInstance(project)
        val document = psiDocumentManager.getDocument(savedPsiFile as PsiFile)
        psiDocumentManager.commitDocument(document!!)
        return document
    }

}