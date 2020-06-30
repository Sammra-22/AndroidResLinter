# Reslinter

**Gradle Plugin to lint Android resources**

Detect and remove unused Android resources in any Gradle project.
The plugin supports multi-project Gradle builds i.e resolves modules cross references.

## Supported Android resources

- layouts
- drawables
- animations
- strings/plurals
- colors

## Gradle tasks

Check all unused Android resources in the current module & submodules:

```
> ./gradlew checkUnusedAndroidResources
```

Delete all detected unused Android resources:

```
> ./gradlew removeUnusedAndroidResources
```

## Getting started

Using the plugins DSL:
```
plugins {
  id("com.tanitech.reslinter") version "1.0.0"
}
```

Using legacy plugin application:
```
buildscript {
  repositories {
    maven {
      url = uri("https://plugins.gradle.org/m2/")
    }
  }
  dependencies {
    classpath("gradle.plugin.com.tanitech.gradle:reslinter-plugin:1.0.0")
  }
}

apply(plugin = "com.tanitech.reslinter")
```
