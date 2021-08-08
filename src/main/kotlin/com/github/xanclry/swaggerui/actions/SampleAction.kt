package com.github.xanclry.swaggerui.actions

import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent

class SampleAction : AnAction() {
    override fun update(e: AnActionEvent) {
        e.presentation.isVisible = true
        e.presentation.isEnabled = true
    }

    override fun actionPerformed(e: AnActionEvent) {
        println("Hello")

    }
}