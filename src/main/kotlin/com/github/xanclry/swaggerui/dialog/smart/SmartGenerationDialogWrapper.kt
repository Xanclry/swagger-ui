package com.github.xanclry.swaggerui.dialog.smart

import com.github.xanclry.swaggerui.codegen.Language
import com.intellij.openapi.ui.DialogWrapper
import com.intellij.ui.layout.panel
import javax.swing.DefaultComboBoxModel
import javax.swing.JComponent

class SmartGenerationDialogWrapper(private val moduleList: List<ModuleDto>) : DialogWrapper(true) {

    var selectedWebModule: ModuleDto? = null
    var selectedModelModule: ModuleDto? = null
    var selectedLanguage: Language = Language.SPRING

    init {
        super.init()
    }

    override fun createCenterPanel(): JComponent {
        selectedWebModule = defaultWebModule(moduleList)
        selectedModelModule = defaultModelModule(moduleList)
        return panel {
            row("Web module ") {
                comboBox(DefaultComboBoxModel(moduleList.toTypedArray()), { selectedWebModule }, { selectedWebModule = it })
            }
            row("Model, DTO module ") {
                comboBox(DefaultComboBoxModel(moduleList.toTypedArray()), { selectedModelModule }, { selectedModelModule = it })
            }
            row("Language: ") {
                comboBox(DefaultComboBoxModel(Language.values()), { selectedLanguage }, { selectedLanguage = it ?: Language.SPRING })
            }
        }
    }

    private fun defaultWebModule(moduleList: List<ModuleDto>): ModuleDto? {
        return moduleList.find { it.name.contains("web", true) }
    }

    private fun defaultModelModule(moduleList: List<ModuleDto>): ModuleDto? {
        return moduleList.find { it.name.contains("model", true) }
    }
}