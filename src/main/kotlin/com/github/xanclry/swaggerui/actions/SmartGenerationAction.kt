package com.github.xanclry.swaggerui.actions

import com.github.xanclry.swaggerui.MyBundle
import com.github.xanclry.swaggerui.codegen.facade.SmartGenerationFacade
import com.github.xanclry.swaggerui.dialog.smart.ModuleDto
import com.github.xanclry.swaggerui.dialog.smart.SmartGenerationDialogWrapper
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.module.ModuleManager
import com.intellij.openapi.ui.DialogWrapper
import com.intellij.util.containers.stream
import java.util.stream.Collectors

class SmartGenerationAction : AnAction() {

    override fun update(e: AnActionEvent) {
        val moduleManager = ModuleManager.getInstance(e.project!!)
        val modules = moduleManager.modules
        e.presentation.isEnabled = modules.isNotEmpty()
    }

    override fun actionPerformed(e: AnActionEvent) {
        val project = e.project
        val moduleManager = ModuleManager.getInstance(project!!)
        val modules = moduleManager.modules

        val dialog =
            SmartGenerationDialogWrapper(modules.stream().map { ModuleDto(it.name, modules.indexOf(it)) }.collect(Collectors.toList()))
        dialog.title = MyBundle.message("dialog.generate.smart.title")
        dialog.show()

        val exitCode = dialog.exitCode
        if (DialogWrapper.OK_EXIT_CODE == exitCode && dialog.selectedModule != null && dialog.selectedLanguage != null) {
            val (_, id) = dialog.selectedModule!!
            val lang = dialog.selectedLanguage!!
            SmartGenerationFacade(lang, project).runSmartGeneration(modules[id])
        }
    }
}