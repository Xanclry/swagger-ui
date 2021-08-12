package com.github.xanclry.swaggerui.state.settings

import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.components.PersistentStateComponent
import com.intellij.openapi.components.State
import com.intellij.openapi.components.Storage
import com.intellij.util.xmlb.XmlSerializerUtil

@State(name = "com.github.xanclry.swaggerui.state.settings.SettingsState", storages = [Storage("swaggerCodegenUi.xml")])
internal class SettingsState : PersistentStateComponent<SettingsState?> {
    var configUrl = ""

    // todo remove hardcode
    var configType = SettingsPathType.URL
    override fun getState(): SettingsState {
        return this
    }

    override fun loadState(state: SettingsState) {
        XmlSerializerUtil.copyBean(state, this)
    }

    companion object {
        val instance: SettingsState
            get() = ApplicationManager.getApplication().getService(
                SettingsState::class.java
            )
    }
}