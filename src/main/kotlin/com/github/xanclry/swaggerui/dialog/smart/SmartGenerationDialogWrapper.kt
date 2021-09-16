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
        return panel {
            row("Select module for generation: ") {
                comboBox(DefaultComboBoxModel(moduleList.toTypedArray()), { selectedWebModule }, { selectedWebModule = it })
            }
            row("Select module with models: ") {
                comboBox(DefaultComboBoxModel(moduleList.toTypedArray()), { selectedModelModule }, { selectedModelModule = it })
            }
            row("Language: ") {
                comboBox(DefaultComboBoxModel(Language.values()), { selectedLanguage }, { selectedLanguage = it ?: Language.SPRING })
            }
        }
    }
}