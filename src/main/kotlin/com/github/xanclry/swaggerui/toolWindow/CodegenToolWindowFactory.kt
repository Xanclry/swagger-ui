package com.github.xanclry.swaggerui.toolWindow

import com.intellij.ide.util.gotoByName.GotoActionModel
import com.intellij.openapi.actionSystem.ActionManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.wm.ToolWindow
import com.intellij.openapi.wm.ToolWindowFactory
import com.intellij.psi.PsiManager
import com.intellij.ui.content.ContentFactory

class CodegenToolWindowFactory : ToolWindowFactory  {
    override fun createToolWindowContent(project: Project, toolWindow: ToolWindow) {
        val codegenToolWindow = CodegenToolWindow()
        val contentFactory = ContentFactory.SERVICE.getInstance()
        val content = contentFactory.createContent(codegenToolWindow.getContent(), "DisplayName", false)
        toolWindow.contentManager.addContent(content)
//        PsiManager.getInstance(project).addPsiTreeChangeListener()
//        ActionManager.getInstance().createActionToolbar("Swagger Codegen",  )
    }

}