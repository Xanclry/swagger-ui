package com.github.xanclry.swaggerui.codegen.facade

import com.github.xanclry.swaggerui.MyBundle
import com.github.xanclry.swaggerui.codegen.CodegenFactory
import com.github.xanclry.swaggerui.codegen.exception.LanguageNotSupportedException
import com.github.xanclry.swaggerui.dialog.controller.GenerateControllerDto
import com.github.xanclry.swaggerui.util.DocumentUtil
import com.github.xanclry.swaggerui.util.DocumentUtil.Companion.openEditorOnNewFile
import com.github.xanclry.swaggerui.util.Notifier
import com.intellij.notification.NotificationType
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.openapi.command.WriteCommandAction
import com.intellij.openapi.editor.Document
import com.intellij.openapi.progress.ProgressIndicator
import com.intellij.openapi.progress.ProgressManager
import com.intellij.openapi.progress.Task
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.PsiDirectory
import com.intellij.psi.PsiFile
import com.intellij.psi.PsiManager
import com.intellij.psi.impl.PsiManagerImpl
import com.intellij.psi.impl.file.PsiDirectoryImpl
import com.intellij.util.IncorrectOperationException

class GenerateControllerFacade {

    private val documentUtil = DocumentUtil()

    fun generateCode(
        e: AnActionEvent,
        generateControllerDto: GenerateControllerDto
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
                        document = generateDocument(virtualFile, project, generateControllerDto)
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

    private fun generateDocument(
        virtualDirectory: VirtualFile,
        project: Project,
        generateControllerDto: GenerateControllerDto
    ): Document? {
        var newPsiFile: PsiFile? = null
        try {
            val codegen = CodegenFactory.factoryMethod(generateControllerDto.language).createCodegen(project)
            var document: Document? = null
            var code = ""
            if (!generateControllerDto.generateEmpty) {
                code = codegen.generateEndpointsCodeWithPath(project, code, generateControllerDto.path).asString()
            }
            WriteCommandAction.runWriteCommandAction(project) {
                newPsiFile = codegen.generateController(generateControllerDto.path, project, true, code)
                val psiDirectory: PsiDirectory =
                    PsiDirectoryImpl(PsiManager.getInstance(project) as PsiManagerImpl, virtualDirectory)
                document = documentUtil.commitNewFile(project, psiDirectory, newPsiFile!!)

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

}