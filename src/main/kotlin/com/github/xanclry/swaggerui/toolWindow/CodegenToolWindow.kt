package com.github.xanclry.swaggerui.toolWindow

import com.github.xanclry.swaggerui.state.SettingsState
import javax.swing.JButton
import javax.swing.JPanel

class CodegenToolWindow {
    private var panel1: JPanel? = null
    private var loadConfigButton: JButton? = null
    private var button2: JButton? = null

    init {
        loadConfigButton?.addActionListener {
            loadConfig()
        }
    }

    fun getContent(): JPanel? {
        return panel1
    }

    private fun loadConfig() {
        val url = SettingsState.instance.configUrl
        println()
    }

}