package com.github.xanclry.swaggerui.actions

import com.github.xanclry.swaggerui.codegen.CodegenFactory
import com.github.xanclry.swaggerui.codegen.Language
import com.github.xanclry.swaggerui.util.Notifier
import com.intellij.notification.NotificationType
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.openapi.command.WriteCommandAction
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiFile

class GenerateCodeAction : AnAction() {

    override fun actionPerformed(e: AnActionEvent) {

        val psiFile = e.getData(CommonDataKeys.PSI_FILE)
        val project = e.project
        val editor = e.getData(CommonDataKeys.EDITOR)
        if (project != null && psiFile != null && editor != null) {
            generateCode(psiFile, editor, project)
        } else {
            Notifier.notifyProjectWithMessageFromBundle(project, "notification.codegen.error", NotificationType.ERROR)
        }
    }

    private fun generateCode(
        psiFile: PsiFile,
        editor: Editor,
        project: Project
    ) {
        val lang: Language = Language.valueOf(psiFile.language.id)
        val codegen = CodegenFactory.factoryMethod(lang).createCodegen(project)

        val codegenCheckResult = codegen.isFileSuitable(editor.document)

        if (codegenCheckResult.isAvailable) {
            val generatedCode = codegen.generateCode(project, editor)
            val offsetForNewCode = codegen.offsetForNewCode(editor.document)
            WriteCommandAction.runWriteCommandAction(project) {
                editor.document.insertString(offsetForNewCode, generatedCode)
            }
            editor.caretModel.moveToOffset(offsetForNewCode)
        } else {
            codegenCheckResult.reason?.let {
                Notifier.notifyProjectWithMessageFromBundle(
                    project,
                    it, NotificationType.ERROR
                )
            }
        }
    }


    override fun update(e: AnActionEvent) {
        val editor = e.getData(CommonDataKeys.EDITOR)
        e.presentation.isEnabled = editor != null
    }


}