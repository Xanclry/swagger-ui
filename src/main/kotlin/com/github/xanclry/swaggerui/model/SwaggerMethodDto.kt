package com.github.xanclry.swaggerui.model

import io.swagger.models.HttpMethod

data class SwaggerMethodDto(val method: MutableSet<HttpMethod>, var path: String)