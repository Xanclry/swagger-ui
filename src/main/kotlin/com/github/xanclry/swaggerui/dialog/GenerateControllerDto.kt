package com.github.xanclry.swaggerui.dialog

import com.github.xanclry.swaggerui.codegen.Language

data class GenerateControllerDto(var path: String, var generateEmpty: Boolean, var language: Language)
