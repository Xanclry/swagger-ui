# Endpoint UI Codegen

![Build](https://github.com/Xanclry/swagger-ui/workflows/Build/badge.svg)
[![Version](https://img.shields.io/jetbrains/plugin/v/17438-endpoint-ui-codegen.svg)](https://plugins.jetbrains.com/plugin/17438-endpoint-ui-codegen)
[![Downloads](https://img.shields.io/jetbrains/plugin/d/17438-endpoint-ui-codegen.svg)](https://plugins.jetbrains.com/plugin/17438-endpoint-ui-codegen)

<!-- Plugin description -->

## Overview

This is Simple Swagger/OpenAPI endpoint generator tool. It allows you to generate missing <kbd>controller methods</kbd> or generate
a new controller with a specified path.

Supported languages:

- Spring

## Setting Up

For the plugin to work correctly, you need to specify the path to your Swagger/OpenAPI configuration.

You can do this in `Settings` > `Tools` > `Endpoint UI Codegen Config`.

<!-- Plugin description end -->

## Usage

### Generating missing endpoints

To generate the missing endpoints, open the file with the controller in the editor. Your controller must be annotated *
@RestController* or *@Controller* and *@RequestMapping*
with the specified path.

The method is considered present if the code contains the annotation "@*METHOD_NAME* Mapping("/some/path")

### New controller generation

Right-click on the required package, select `New` > `Endpoint UI Codegen` > `Controller`.

In the dialog box that appears, you can specify the path of the controller and whether the controller will be empty. 
Name of the class will be generated according to the URL.

### Smart Generation 

Smart generation allows you to create missing controllers and models based on configuration. 
In addition, the plugin automatically recognizes packages for generated files.
Information about package must be stored in the configuration.

For controllers, this is the <kbd>tag name</kbd>:

```
 "/api/cinema/{id}": {
      "get": {
        "tags": [
          "com.example.your.package.here.CinemaRestController"
        ],
        "summary": "getById",
        ...
```

And for model, this is the <kbd>description</kbd>: 

```
"Actor": {
  "type": "object",
  "properties": {
    "firstName": {
      "type": "string"
    },
    "isRetired": {
      "type": "boolean"
    },
    "lastName": {
      "type": "string"
    }
  },
  "title": "Actor",
  "description": "com.example.your.package.here" 
},
```

### Endpoints Blacklist

In case you don't want the plugin to handle certain paths, you can use the blacklist. 

This blacklist should be located in extensions for the OpenApi configuration.
Name of the extension must be `x-swagger-ui-blacklist` and contain an array of strings.

For endpoints that _**exactly match**_ those contained in the blacklist
won't be generated an endpoint (for any HTTP method).

Config example
```
{
  "swagger": "2.0",
  "info": {
    "description": "Example Description",
    "title": "example.com API",
    "x-swagger-ui-blacklist": [
      "/api/first/url",
      "/api/second/url"
    ]
  },
  "host": "example.com",
  . 
  .
  and so on
```


## Compilation

- First specify new version in both `gradle.properties` and `resources/META-INF/plugin.xml`.
- Then for compilation use this Gradle task:

`gradle clean buildDependents -x test -x detekt -x ktlintMainSourceSetCheck`

- You can find the zipped plugin at
  `build/distributions/`

## Installation

- Using IDE built-in plugin system:

  <kbd>Settings/Preferences</kbd> > <kbd>Plugins</kbd> > <kbd>Marketplace</kbd> > <kbd>Search for "Endpoint UI
  Codegen"</kbd> >
  <kbd>Install Plugin</kbd>

- Manually:

  Download the [latest release](https://github.com/Xanclry/swagger-ui/releases/latest) and install it manually using
  <kbd>Settings/Preferences</kbd> > <kbd>Plugins</kbd> > <kbd>⚙️</kbd> > <kbd>Install plugin from disk...</kbd>

---
Plugin based on the [IntelliJ Platform Plugin Template][template].

[template]: https://github.com/JetBrains/intellij-platform-plugin-template
