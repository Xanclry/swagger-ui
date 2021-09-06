package com.github.xanclry.swaggerui.codegen.facade

import com.github.xanclry.swaggerui.MyBundle
import com.github.xanclry.swaggerui.codegen.CodegenFactory
import com.github.xanclry.swaggerui.codegen.GeneratedMethodsAdapter
import com.github.xanclry.swaggerui.codegen.Language
import com.github.xanclry.swaggerui.codegen.exception.LanguageNotSupportedException
import com.github.xanclry.swaggerui.util.Notifier
import com.intellij.notification.NotificationType
import com.intellij.openapi.command.WriteCommandAction
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.progress.ProgressIndicator
import com.intellij.openapi.progress.ProgressManager
import com.intellij.openapi.progress.Task
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiClass
import com.intellij.psi.PsiFile
import com.intellij.psi.PsiJavaFile

class GenerateMethodsFacade {
    fun generateCode(psiFile: PsiFile, editor: Editor, project: Project) {
        try {
            val lang: Language = Language.parseJetbrainsLanguage(psiFile.language)
            val codegen = CodegenFactory.factoryMethod(lang).createCodegen(project)

            val codegenCheckResult = codegen.isFileSuitable(editor.document)

            if (codegenCheckResult.isAvailable) {
                val classes: Array<PsiClass> = (psiFile as PsiJavaFile).classes
                var psiMethods: GeneratedMethodsAdapter? = null

                ProgressManager.getInstance()
                    .run(object : Task.Modal(project, MyBundle.message("modal.generating.methods.title"), false) {
                        override fun run(indicator: ProgressIndicator) {
                            indicator.isIndeterminate = false
                            indicator.text = MyBundle.message("modal.generating.methods.message")
                            psiMethods = codegen.generateEndpointsCodePathUnknown(project, editor.document.text)
                        }
                    })
                if (psiMethods != null) {
                    WriteCommandAction.runWriteCommandAction(project) {
                        psiMethods!!.asPsiList(psiFile).forEach { classes[0].add(it) }
                        codegen.reformatAndOptimizeImports(psiFile, project)
                    }
                }

            } else {
                codegenCheckResult.reason?.let {
                    Notifier.notifyProjectWithMessageFromBundle(
                        project,
                        it, NotificationType.ERROR
                    )
                }
            }
        } catch (e: LanguageNotSupportedException) {
            Notifier.notifyProject(project, e.message!!, NotificationType.ERROR)
        }
    }
}