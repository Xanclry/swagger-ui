package com.github.xanclry.swaggerui.actions

import com.github.xanclry.swaggerui.MyBundle
import com.github.xanclry.swaggerui.codegen.CodegenFactory
import com.github.xanclry.swaggerui.dialog.ControllerGenerationDialogWrapper
import com.github.xanclry.swaggerui.dialog.GenerateControllerDto
import com.github.xanclry.swaggerui.util.Notifier
import com.intellij.notification.NotificationType
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.openapi.actionSystem.PlatformDataKeys
import com.intellij.openapi.command.WriteCommandAction
import com.intellij.openapi.editor.Document
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.DialogWrapper
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.PsiDirectory
import com.intellij.psi.PsiDocumentManager
import com.intellij.psi.PsiFile
import com.intellij.psi.PsiManager
import com.intellij.psi.impl.PsiManagerImpl
import com.intellij.psi.impl.file.PsiDirectoryImpl
import com.intellij.psi.util.elementType
import com.intellij.util.IncorrectOperationException

class GenerateControllerAction : AnAction() {

    override fun update(e: AnActionEvent) {
        val psi2 = e.dataContext.getData(PlatformDataKeys.PSI_FILE)
        e.presentation.isEnabled = !(psi2 != null && psi2.elementType != null)
    }

    override fun actionPerformed(e: AnActionEvent) {
        val controllerGenerationDialogWrapper = ControllerGenerationDialogWrapper()
        controllerGenerationDialogWrapper.title = MyBundle.message("dialog.generate.controller.title")

        controllerGenerationDialogWrapper.show()
        val exitCode = controllerGenerationDialogWrapper.exitCode

        if (DialogWrapper.OK_EXIT_CODE == exitCode) {
            val project = e.project
            val virtualFile = e.dataContext.getData(CommonDataKeys.VIRTUAL_FILE)
            if (project != null && virtualFile != null) {
                generateCode(virtualFile, project, controllerGenerationDialogWrapper.data)
            } else {
                Notifier.notifyProjectWithMessageFromBundle(
                    project,
                    "notification.codegen.error",
                    NotificationType.ERROR
                )
            }
        }
    }

    private fun generateCode(
        virtualFile: VirtualFile,
        project: Project,
        data: GenerateControllerDto
    ) {
        val codegen = CodegenFactory.factoryMethod(data.language).createCodegen(project)
        var newPsiFile: PsiFile? = null
        try {
            newPsiFile = codegen.generateEmptyController(data.path, project)
            val psiDirectory: PsiDirectory =
                PsiDirectoryImpl(PsiManager.getInstance(project) as PsiManagerImpl, virtualFile)
            WriteCommandAction.runWriteCommandAction(project) {
                psiDirectory.add(newPsiFile)
                val psiDocumentManager = PsiDocumentManager.getInstance(project)
                val document: Document? = psiDocumentManager.getDocument(newPsiFile)
                if (document != null) {
                    psiDocumentManager.commitDocument(document)
                }
            }
        } catch (e: IncorrectOperationException) {
            Notifier.notifyProjectWithMessageFromBundle(
                project,
                "notification.codegen.error.fileAlreadyExist",
                NotificationType.ERROR
            )
        } finally {
            newPsiFile?.clearCaches()
        }
    }
}