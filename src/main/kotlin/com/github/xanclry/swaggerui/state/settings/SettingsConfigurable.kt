package com.github.xanclry.swaggerui.state.settings

import com.intellij.openapi.options.Configurable
import org.jetbrains.annotations.Nls
import javax.swing.JComponent

class SettingsConfigurable : Configurable {
    private var mySettingsComponent: SettingsComponent? = null

    // A default constructor with no arguments is required because this implementation
    // is registered as an applicationConfigurable EP
    override fun getDisplayName(): String {
        return "Swagger Codegen Settings"
    }

    override fun getPreferredFocusedComponent(): JComponent {
        return mySettingsComponent!!.preferredFocusedComponent
    }

    override fun createComponent(): JComponent {
        mySettingsComponent = SettingsComponent()
        return mySettingsComponent!!.panel
    }

    override fun isModified(): Boolean {
        val settings =
            SettingsState.instance
        return mySettingsComponent!!.configUrlText != settings.configUrl
    }

    override fun apply() {
        val settings = SettingsState.instance
        settings.configUrl = mySettingsComponent!!.configUrlText
    }

    override fun reset() {
        val settings = SettingsState.instance
        mySettingsComponent!!.configUrlText = settings.configUrl
    }

    override fun disposeUIResources() {
        mySettingsComponent = null
    }
}