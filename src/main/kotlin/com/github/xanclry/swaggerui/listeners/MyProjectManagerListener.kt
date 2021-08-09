package com.github.xanclry.swaggerui.listeners

import com.github.xanclry.swaggerui.services.CodegenService
import com.intellij.openapi.components.service
import com.intellij.openapi.project.Project
import com.intellij.openapi.project.ProjectManagerListener

internal class MyProjectManagerListener : ProjectManagerListener {

    override fun projectOpened(project: Project) {
        project.service<CodegenService>()
    }
}
