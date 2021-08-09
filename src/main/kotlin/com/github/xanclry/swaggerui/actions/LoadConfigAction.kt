package com.github.xanclry.swaggerui.actions

import com.github.xanclry.swaggerui.MyBundle
import com.github.xanclry.swaggerui.util.Notifier
import com.intellij.ide.DataManager
import com.intellij.notification.NotificationGroup
import com.intellij.notification.NotificationType
import com.intellij.notification.Notifications
import com.intellij.openapi.actionSystem.*
import com.intellij.openapi.editor.Document
import com.intellij.openapi.fileEditor.FileDocumentManager
import com.intellij.openapi.fileEditor.FileEditorManager
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.PsiFile
import com.intellij.psi.PsiManager

class LoadConfigAction : AnAction() {
    override fun update(e: AnActionEvent) {
        e.presentation.isVisible = true
        e.presentation.isEnabled = true
    }

    override fun actionPerformed(e: AnActionEvent) {
            val dataContext = DataManager.getInstance().dataContext
            val project = CommonDataKeys.PROJECT.getData(dataContext)
        try {
//            val psiData: PsiFile? = e.getData(CommonDataKeys.PSI_FILE)

            Notifier.notifyProjectWithMessageFromBundle(project, "notification.loadConfig.success", NotificationType.INFORMATION)
        } catch (e: Exception) {
            Notifier.notifyProjectWithMessageFromBundle(project, "notification.loadConfig.error", NotificationType.ERROR)
            // TODO add link to settings window
        }
//        PsiManager.getInstance(project).addPsiTreeChangeListener()
//        val text = e.getData(LangDataKeys.EDITOR)?.document?.text
//        val currentDoc = project?.let { FileEditorManager.getInstance(it).selectedTextEditor?.document };
//        val currentFile = currentDoc?.let { FileDocumentManager.getInstance().getFile(it) };
//        val fileName = currentFile?.path
//        println("123")


    }
}