package com.github.xanclry.swaggerui.actions

import com.github.xanclry.swaggerui.codegen.Language
import com.github.xanclry.swaggerui.codegen.facade.GenerateMethodsFacade
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
            val generateMethodsFacade = GenerateMethodsFacade(Language.parseJetbrainsLanguage(psiFile.language), project)
            generateMethodsFacade.generateCode(psiFile, editor, project)
        } else {
            Notifier.notifyProjectWithMessageFromBundle(project, "notification.codegen.error", NotificationType.ERROR)
        }
    }

    override fun update(e: AnActionEvent) {
        val editor = e.getData(CommonDataKeys.EDITOR)
        e.presentation.isEnabled = editor != null
    }
}
