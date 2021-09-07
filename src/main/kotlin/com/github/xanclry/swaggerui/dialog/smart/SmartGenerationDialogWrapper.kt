package com.github.xanclry.swaggerui.dialog.smart

import com.intellij.openapi.ui.DialogWrapper
import com.intellij.ui.layout.panel
import javax.swing.DefaultComboBoxModel
import javax.swing.JComponent

class SmartGenerationDialogWrapper(private val moduleList: List<ModuleDto>) : DialogWrapper(true) {

    var selectedModule: ModuleDto? = null

    init {
        super.init()
    }

    override fun createCenterPanel(): JComponent {
        return panel {
            row("Select module for generation: ") {
                comboBox(DefaultComboBoxModel(moduleList.toTypedArray()), { selectedModule }, { selectedModule = it })
            }
        }
    }
}