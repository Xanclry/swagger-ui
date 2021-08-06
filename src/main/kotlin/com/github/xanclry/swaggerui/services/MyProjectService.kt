package com.github.xanclry.swaggerui.services

import com.github.xanclry.swaggerui.MyBundle
import com.intellij.openapi.project.Project

class MyProjectService(project: Project) {

    init {
        println(MyBundle.message("projectService", project.name))
    }
}
