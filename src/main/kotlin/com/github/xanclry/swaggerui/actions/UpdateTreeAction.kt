package com.github.xanclry.swaggerui.actions

import com.github.xanclry.swaggerui.state.settings.SettingsState
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent

class UpdateTreeAction : AnAction() {
    override fun update(e: AnActionEvent) {
        val configUrl = SettingsState.instance.configUrl
        e.presentation.isEnabled = configUrl != ""
    }

    override fun actionPerformed(e: AnActionEvent) {
        println()
    }
}