package com.github.xanclry.swaggerui.toolWindow

import com.github.xanclry.swaggerui.model.SwaggerMethodDto
import com.intellij.ui.components.JBList
import javax.swing.JButton
import javax.swing.JList
import javax.swing.JPanel

class CodegenToolWindow {
    private var panel1: JPanel? = null
    private var loadConfigButton: JButton? = null
    private var updateTreeButton: JButton? = null
    private var methodList: JList<SwaggerMethodDto> = JBList()

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
        println()
    }

    private fun updateTree() {
        println()
    }

}