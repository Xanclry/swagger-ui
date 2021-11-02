package com.github.xanclry.swaggerui.util

class EndpointUtil {

    companion object {
        fun compareEndpoints(a: String, b: String): Boolean {
            val normalizedA = normalizeEndpointPath(a)
            val normalizedB = normalizeEndpointPath(b)
            return normalizedA == normalizedB
        }

        private fun normalizeEndpointPath(path: String) =
            path.removePrefix("/").removeSuffix("/").replace("//", "/")
    }
}