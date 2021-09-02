package com.github.xanclry.swaggerui.dialog

import com.github.xanclry.swaggerui.codegen.Language
import com.intellij.openapi.ui.DialogWrapper
import com.intellij.ui.layout.GrowPolicy
import com.intellij.ui.layout.panel
import javax.swing.DefaultComboBoxModel
import javax.swing.JComponent

class ControllerGenerationDialogWrapper : DialogWrapper(true) {

    var data: GenerateControllerDto = GenerateControllerDto("/", true, Language.SPRING)

    init {
        super.init()
    }

    override fun createCenterPanel(): JComponent {
        return panel {
            row("Controller path: ") {
                textField(
                    { data.path },
                    { data.path = it })
                    .focused().growPolicy(GrowPolicy.MEDIUM_TEXT)
            }
            row("Language: ") {
                comboBox(DefaultComboBoxModel(Language.values()), { data.language }, { data.language = it ?: Language.SPRING })
            }
            row {
                checkBox("Generate empty controller", { data.generateEmpty }, { data.generateEmpty = it })
            }
        }
    }

}