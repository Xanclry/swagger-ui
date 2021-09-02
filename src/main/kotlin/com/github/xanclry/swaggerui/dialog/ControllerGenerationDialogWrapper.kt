package com.github.xanclry.swaggerui.dialog

import com.github.xanclry.swaggerui.codegen.Language
import com.intellij.openapi.ui.DialogWrapper
import com.intellij.ui.layout.GrowPolicy
import com.intellij.ui.layout.panel
import javax.swing.JComponent

class ControllerGenerationDialogWrapper : DialogWrapper(true) {

    // todo select language
    var data: GenerateControllerDto = GenerateControllerDto("/", true, Language.JAVA)

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
            row {
                checkBox("Generate empty controller", { data.generateEmpty }, { data.generateEmpty = it })
            }
        }
    }

}