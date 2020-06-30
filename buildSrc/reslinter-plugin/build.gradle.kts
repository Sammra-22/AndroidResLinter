plugins {
    id("java-gradle-plugin")
    id("groovy")
    id("com.gradle.plugin-publish") version "0.12.0"
}

description = "Checks unused android resources in the whole project."
extra["longDescription"] = """
    Lint check for unused Android resources: Detect unused layouts/drawables/anims/strings/colors
    and flag them as build errors.
    """.trimIndent()

version = "1.0.0"
group = "com.tanitech.gradle"

pluginBundle {
    website = "https://github.com/Sammra-22/AndroidResLinter"
    vcsUrl = "https://github.com/Sammra-22/AndroidResLinter.git"
    tags = listOf("linting", "staticAnalysis", "androidResources")
}

gradlePlugin {
    plugins {
        create("AndroidResourcesLinter") {
            id = "com.tanitech.reslinter"
            displayName = "AndroidResLinter"
            description = "Detect and remove unused Android resources in a given Gradle project"
            implementationClass = "com.tanitech.reslinter.ResourcesLinterPlugin"
        }
    }
}
