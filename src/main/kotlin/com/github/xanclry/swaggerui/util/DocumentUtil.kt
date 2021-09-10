package com.github.xanclry.swaggerui.util

import com.github.xanclry.swaggerui.model.file.FileMetadataDto
import com.intellij.openapi.command.WriteCommandAction
import com.intellij.openapi.editor.Document
import com.intellij.openapi.fileEditor.FileDocumentManager
import com.intellij.openapi.fileEditor.FileEditorManager
import com.intellij.openapi.fileEditor.OpenFileDescriptor
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VfsUtil
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.PsiDirectory
import com.intellij.psi.PsiDocumentManager
import com.intellij.psi.PsiFile
import com.intellij.psi.PsiManager
import com.intellij.psi.impl.PsiManagerImpl
import com.intellij.psi.impl.file.PsiDirectoryImpl

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

    fun createOrFindDirectory(project: Project, root: VirtualFile, targetPackagePath: String): VirtualFile {
        var currentPackage = root
        val directoryNames: List<String> = targetPackagePath.split(".")
        WriteCommandAction.runWriteCommandAction(project) {
            for (directoryName in directoryNames) {
                currentPackage =
                    currentPackage.findChild(directoryName) ?: currentPackage.createChildDirectory(null, directoryName)
            }
        }
        return currentPackage
    }

    fun createOrFindFile(project: Project, scope: VirtualFile, fileMetadata: FileMetadataDto): VirtualFile {
        val targetDirectory: VirtualFile = createOrFindDirectory(project, scope, fileMetadata.packagePath)
        var file: VirtualFile? = findFileInDirectory(targetDirectory, fileMetadata.filename)
        if (file == null) {
            file = createFileInDirectory(targetDirectory, fileMetadata.filename)
        }
        return file
    }

    fun loadText(virtualFile: VirtualFile): String {
        return VfsUtil.loadText(virtualFile)
    }

    fun createFileInDirectory(project: Project, newPsiFile: PsiFile, virtualDirectory: VirtualFile): Document {
        val psiDirectory: PsiDirectory =
            PsiDirectoryImpl(PsiManager.getInstance(project) as PsiManagerImpl, virtualDirectory)
        return commitNewFile(project, psiDirectory, newPsiFile)
    }

    fun findFileInDirectory(directory: VirtualFile, filename: String): VirtualFile? {
        return directory.findChild(filename)
    }

    private fun createFileInDirectory(directory: VirtualFile, filename: String): VirtualFile {
        return directory.createChildData(null, filename)
    }

    fun editVirtualFile(virtualFile: VirtualFile, newText: String) {
        virtualFile.setBinaryContent(newText.toByteArray(virtualFile.charset))
    }


}