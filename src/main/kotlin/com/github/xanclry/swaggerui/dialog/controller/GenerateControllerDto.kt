package com.github.xanclry.swaggerui.dialog.controller

import com.github.xanclry.swaggerui.codegen.Language

data class GenerateControllerDto(var path: String, var generateEmpty: Boolean, var language: Language)
