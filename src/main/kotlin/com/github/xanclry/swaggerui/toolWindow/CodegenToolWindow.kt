package com.github.xanclry.swaggerui.toolWindow

import com.github.xanclry.swaggerui.model.SwaggerMethod
import com.github.xanclry.swaggerui.state.SettingsState
import com.intellij.openapi.wm.ToolWindowManager
import com.intellij.ui.components.JBList
import javax.swing.JButton
import javax.swing.JList
import javax.swing.JPanel

class CodegenToolWindow {
    private var panel1: JPanel? = null
    private var loadConfigButton: JButton? = null
    private var updateTreeButton: JButton? = null
    private var methodList: JList<SwaggerMethod> = JBList()

    init {
        loadConfigButton?.addActionListener {
            loadConfig()
        }
        updateTreeButton?.addActionListener {
            updateTree()
        }
    }

    fun getContent(): JPanel? {
        return panel1
    }

    private fun loadConfig() {
        val url = SettingsState.instance.configUrl
        println()
    }

    private fun updateTree() {
        println()
    }

}