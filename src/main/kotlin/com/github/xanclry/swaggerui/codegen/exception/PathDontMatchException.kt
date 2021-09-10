package com.github.xanclry.swaggerui.codegen.exception

class PathDontMatchException(actual: String, expected: String) :
    RuntimeException("Wrong path value. Actual $actual, expected: $expected")