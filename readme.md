# Reslinter

**Gradle Plugin to lint Android resources**

Detect and remove unused Android resources in any Gradle project.
The plugin supports multi-project Gradle builds i.e resolves modules cross references.

## Android resources supported

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
