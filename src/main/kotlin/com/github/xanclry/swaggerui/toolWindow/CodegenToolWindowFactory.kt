package com.github.xanclry.swaggerui.toolWindow

import com.intellij.openapi.project.Project
import com.intellij.openapi.wm.ToolWindow
import com.intellij.openapi.wm.ToolWindowFactory
import com.intellij.ui.content.ContentFactory

class CodegenToolWindowFactory : ToolWindowFactory  {
    override fun createToolWindowContent(project: Project, toolWindow: ToolWindow) {
        val codegenToolWindow = CodegenToolWindow()
        val contentFactory = ContentFactory.SERVICE.getInstance()
        val content = contentFactory.createContent(codegenToolWindow.getContent(), "DisplayName", false)
        toolWindow.contentManager.addContent(content)
    }
}