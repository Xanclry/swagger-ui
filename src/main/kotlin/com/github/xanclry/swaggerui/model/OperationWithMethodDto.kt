package com.github.xanclry.swaggerui.model

import io.swagger.models.HttpMethod
import io.swagger.v3.oas.models.Operation

data class OperationWithMethodDto(val method: HttpMethod, val path: String, val operation: Operation)
