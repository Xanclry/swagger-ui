package com.github.xanclry.swaggerui.services

import com.github.xanclry.swaggerui.MyBundle
import com.intellij.openapi.editor.Document
import com.intellij.openapi.project.Project

class CodegenService(project: Project) {

    init {
        println(MyBundle.message("projectService", project.name))
    }

    fun isFileSuitable(document: Document) {

    }



}
