package com.github.xanclry.swaggerui.actions

import com.github.xanclry.swaggerui.codegen.CodegenFactory
import com.github.xanclry.swaggerui.codegen.Language
import com.github.xanclry.swaggerui.util.Notifier
import com.intellij.notification.NotificationType
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.CommonDataKeys

class GenerateCodeAction : AnAction() {

    override fun actionPerformed(e: AnActionEvent) {

        val psiFile = e.getData(CommonDataKeys.PSI_FILE)
        val project = e.project
        val editor = e.getData(CommonDataKeys.EDITOR)
        if (project != null && psiFile != null && editor != null) {
            val lang = Language.valueOf(psiFile.language.id)
            // TODO fix
            val codegen = CodegenFactory.factoryMethod(lang).createCodegen()
            val codegenCheckResult = codegen.isFileSuitable(editor.document)
            val available = codegenCheckResult.isAvailable
            if (available) {
                codegen.generateCode(project)
            } else {
                codegenCheckResult.reason?.let {
                    Notifier.notifyProjectWithMessageFromBundle(project,
                        it, NotificationType.ERROR)
                }
            }
        } else {
            Notifier.notifyProjectWithMessageFromBundle(project, "notification.codegen.error", NotificationType.ERROR)
        }

    }

    override fun update(e: AnActionEvent) {
        val editor = e.getData(CommonDataKeys.EDITOR)
        e.presentation.isEnabled = editor != null
    }


}