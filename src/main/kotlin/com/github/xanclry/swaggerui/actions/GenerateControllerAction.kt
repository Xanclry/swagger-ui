package com.github.xanclry.swaggerui.actions

import com.github.xanclry.swaggerui.MyBundle
import com.github.xanclry.swaggerui.codegen.facade.GenerateControllerFacade
import com.github.xanclry.swaggerui.dialog.controller.ControllerGenerationDialogWrapper
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.PlatformDataKeys
import com.intellij.openapi.ui.DialogWrapper
import com.intellij.psi.util.elementType

class GenerateControllerAction : AnAction() {

    override fun update(e: AnActionEvent) {
        val psi2 = e.dataContext.getData(PlatformDataKeys.PSI_FILE)
        e.presentation.isEnabled = !(psi2 != null && psi2.elementType != null)
    }

    override fun actionPerformed(e: AnActionEvent) {

        val controllerGenerationDialogWrapper = ControllerGenerationDialogWrapper()
        controllerGenerationDialogWrapper.title = MyBundle.message("dialog.generate.controller.title")
        controllerGenerationDialogWrapper.show()

        val exitCode = controllerGenerationDialogWrapper.exitCode
        if (DialogWrapper.OK_EXIT_CODE == exitCode) {
            val generateControllerFacade = GenerateControllerFacade()
            generateControllerFacade.generateCode(e, controllerGenerationDialogWrapper.data)
        }
    }

}