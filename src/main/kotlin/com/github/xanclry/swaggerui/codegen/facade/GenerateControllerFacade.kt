package com.github.xanclry.swaggerui.codegen.facade

import com.github.xanclry.swaggerui.MyBundle
import com.github.xanclry.swaggerui.codegen.CodegenFactory
import com.github.xanclry.swaggerui.codegen.exception.LanguageNotSupportedException
import com.github.xanclry.swaggerui.dialog.ControllerGenerationDialogWrapper
import com.github.xanclry.swaggerui.dialog.GenerateControllerDto
import com.github.xanclry.swaggerui.util.Notifier
import com.intellij.notification.NotificationType
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.openapi.command.WriteCommandAction
import com.intellij.openapi.editor.Document
import com.intellij.openapi.fileEditor.FileDocumentManager
import com.intellij.openapi.fileEditor.FileEditorManager
import com.intellij.openapi.fileEditor.OpenFileDescriptor
import com.intellij.openapi.progress.ProgressIndicator
import com.intellij.openapi.progress.ProgressManager
import com.intellij.openapi.progress.Task
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.PsiDirectory
import com.intellij.psi.PsiDocumentManager
import com.intellij.psi.PsiFile
import com.intellij.psi.PsiManager
import com.intellij.psi.impl.PsiManagerImpl
import com.intellij.psi.impl.file.PsiDirectoryImpl
import com.intellij.util.IncorrectOperationException

class GenerateControllerFacade {
    fun generateCode(
        e: AnActionEvent,
        controllerGenerationDialogWrapper: ControllerGenerationDialogWrapper
    ) {
        var document: Document? = null
        val project = e.project
        val virtualFile = e.dataContext.getData(CommonDataKeys.VIRTUAL_FILE)
        if (project != null && virtualFile != null) {

            ProgressManager.getInstance()
                .run(object : Task.Modal(project, MyBundle.message("modal.generating.controller.title"), false) {
                    override fun run(indicator: ProgressIndicator) {
                        indicator.isIndeterminate = false
                        indicator.text = MyBundle.message("modal.generating.controller.message")
                        document = createDocument(virtualFile, project, controllerGenerationDialogWrapper.data)
                    }
                })

            openEditorOnNewFile(project, document)
        } else {
            Notifier.notifyProjectWithMessageFromBundle(
                project,
                "notification.codegen.error",
                NotificationType.ERROR
            )
        }
    }

    private fun createDocument(
        virtualDirectory: VirtualFile,
        project: Project,
        data: GenerateControllerDto
    ): Document? {
        var newPsiFile: PsiFile? = null
        try {
            val codegen = CodegenFactory.factoryMethod(data.language).createCodegen(project)
            var document: Document? = null
            var code = ""
            if (!data.generateEmpty) {
                code = codegen.generateEndpointsCodeWithPath(project, code, data.path).asString()
            }
            WriteCommandAction.runWriteCommandAction(project) {
                newPsiFile = codegen.generateController(data.path, project, true, code)
                val psiDirectory: PsiDirectory =
                    PsiDirectoryImpl(PsiManager.getInstance(project) as PsiManagerImpl, virtualDirectory)
                document = commitNewFile(project, psiDirectory, newPsiFile!!)

            }
            return document
        } catch (e: IncorrectOperationException) {
            Notifier.notifyProjectWithMessageFromBundle(
                project,
                "notification.codegen.error.fileAlreadyExist",
                NotificationType.ERROR
            )
        } catch (e: LanguageNotSupportedException) {
            Notifier.notifyProject(project, e.message!!, NotificationType.ERROR)
        } catch (e: Exception) {
            Notifier.notifyProjectWithMessageFromBundle(project, "notification.codegen.error", NotificationType.ERROR)
        } finally {
            newPsiFile?.clearCaches()
        }
        return null
    }

    private fun commitNewFile(
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

    private fun openEditorOnNewFile(
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