plugins {
    id("java-gradle-plugin")
    id("groovy")
}

description = "Checks unused android resources in the whole project."
extra["longDescription"] = """
    Lint check for unused Android resources: Detect unused layouts/drawables/anims/strings/colors
    and flag them as build errors.
    """.trimIndent()

gradlePlugin {
    plugins {
        create("AndroidResourcesLinter") {
            id = "com.tanitech.reslinter"
            implementationClass = "com.tanitech.reslinter.ResourcesLinterPlugin"
        }
    }
}
