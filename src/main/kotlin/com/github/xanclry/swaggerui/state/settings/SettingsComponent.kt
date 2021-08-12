package com.github.xanclry.swaggerui.state.settings

import com.intellij.ui.components.JBLabel
import com.intellij.ui.components.JBTextField
import com.intellij.util.ui.FormBuilder
import javax.swing.JComponent
import javax.swing.JPanel

class SettingsComponent {
    val panel: JPanel
    private val configUrl = JBTextField()
    val preferredFocusedComponent: JComponent
        get() = this.configUrl
    var configUrlText: String
        get() = this.configUrl.text
        set(newText) {
            this.configUrl.text = newText
        }

    init {
        panel = FormBuilder.createFormBuilder()
            .addLabeledComponent(JBLabel("Enter config url: "), this.configUrl, 1, false)
            .addComponentFillVertically(JPanel(), 0)
            .panel
    }
}