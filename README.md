# Endpoint UI Codegen

![Build](https://github.com/Xanclry/swagger-ui/workflows/Build/badge.svg)
[![Version](https://img.shields.io/jetbrains/plugin/v/PLUGIN_ID.svg)](https://plugins.jetbrains.com/plugin/17438-endpoint-ui-codegen)
[![Downloads](https://img.shields.io/jetbrains/plugin/d/PLUGIN_ID.svg)](https://plugins.jetbrains.com/plugin/17438-endpoint-ui-codegen)

<!-- Plugin description -->

## Overview

This is Simple Swagger/OpenAPI endpoint generator tool. It allows you to generate missing controller methods or generate
a new controller with a specified path.

Supported languages:

- Spring

## Setting Up

For the plugin to work correctly, you need to specify the path to your Swagger/OpenAPI configuration.

You can do this in `Settings` > `Tools` > `Endpoint UI Codegen Config`.

## Usage

#### Generating missing endpoints

To generate the missing endpoints, open the file with the controller in the editor. Your controller must be annotated *
@RestController* or *@Controller* and *@RequestMapping*
with the specified path.

The method is considered present if the code contains the annotation "@*METHOD_NAME* Mapping("/some/path")

#### New controller generation

Right-click on the required package, select `New` > `Endpoint UI Codegen` > `Controller`.

In the dialog box that appears, you can specify the path of the controller and whether the controller will be empty. 
Name of the class will be generated according to the URL.

<!-- Plugin description end -->

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
