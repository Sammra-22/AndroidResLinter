package com.tanitech.reslinter

import groovy.io.FileType
import groovy.transform.CompileStatic
import org.gradle.api.GradleException
import org.gradle.api.Plugin
import org.gradle.api.Project

@CompileStatic
class ResourcesLinterPlugin implements Plugin<Project> {

    private Set errors = []

    @Override
    void apply(Project project) {

        PluginExtension extension = project.extensions.create("androidResLinter", PluginExtension.class)
        File allUsedRefFile = new File(project.rootProject.buildDir, "reslinter/allUsedRefs")

        def scanProjectTask = project.tasks.register('scanAndroidReferences') {
            it.doLast {
                allUsedRefFile.delete()
                allUsedRefFile.parentFile.mkdirs()
                new ResourcesScanner(project, allUsedRefFile).collectResourceReferences(ResourceType.values())
            }
        }

        project.tasks.register('checkUnusedAndroidResources') {
            it.description = 'Find & flag unused android resources in all modules'
            it.group = 'Lint'
            it.dependsOn(scanProjectTask)
            it.doLast {
                Config config = Utils.readPluginConfig(extension.configFilePath)
                ResourcesScanner scanner = new ResourcesScanner(project, allUsedRefFile, config.whitelist)

                ResourceType.values().each { ResourceType resourceType ->
                    scanner.getUnusedResourcesForType(resourceType).each {
                        Project targetProject, Set<String> unusedResources ->
                            errors.addAll unusedResources.collect {
                                collectErrorMessages(targetProject, resourceType, it)
                            }.flatten().toSet()
                    }
                }

                if (!errors.empty) {
                    String summary = printErrors(errors, new File(project.buildDir, config.reportFilePath))
                    project.logger.error "\n==> RUN './gradlew :removeUnusedResources' to cleanup"
                    throw new GradleException(summary)
                } else {
                    project.logger.lifecycle "\nAll good! No unused resources spotted"
                }
            }
        }

        project.tasks.register('removeUnusedAndroidResources') {
            it.description = 'Cleanup unused android resources in all modules'
            it.group = 'Lint'
            it.dependsOn(scanProjectTask)
            it.doLast {
                Config config = Utils.readPluginConfig(extension.configFilePath)
                ResourcesScanner scanner = new ResourcesScanner(project, allUsedRefFile, config.whitelist)

                ResourceType.values().each { ResourceType resourceType ->
                    scanner.getUnusedResourcesForType(resourceType).each {
                        Project targetProject, Set<String> unusedResources ->
                            unusedResources.each {
                                removeUnusedResource(targetProject, resourceType, it)
                            }
                    }
                }
            }
        }
    }

    private static String printErrors(Set errors, File reportFile) {
        def header = "${errors.size()} unused resource(s) detected.\n\n"
        reportFile.parentFile.mkdirs()
        reportFile.write header
        errors.each {
            reportFile << it << "\n"
            println it
        }
        return header
    }

    private static List<String> collectErrorMessages(Project project, ResourceType resourceType, String resourceName) {
        def messages = []
        def errorTag = "[ERROR] Unused resource:"
        if (resourceType.isResourceValue()) {
            messages << "${errorTag} ${resourceType.name()} with name '${resourceName}'"
        } else {
            project.file("src").eachFileRecurse(FileType.FILES) {
                if (it.path.matches(resourceType.pathMatcher) && it.name.matches("$resourceName[.].*")) {
                    def path = project.rootProject.projectDir.relativePath(it)
                    messages << "${errorTag} ${path}"
                }
            }
        }
        return messages
    }

    private static void removeUnusedResource(Project project, ResourceType resourceType, String resourceName) {
        def infoTag = "[DELETED]"
        project.file("src").eachFileRecurse(FileType.FILES) {
            if (it.path.matches(resourceType.pathMatcher)) {
                if (resourceType.isResourceValue()) {
                    Utils.removeLineWithPattern(it, "name=\"${resourceName}\"")
                    println "${infoTag} '${resourceName}' from ${project.rootProject.projectDir.relativePath(it)}"
                } else if (it.name.matches("$resourceName[.].*")) {
                    it.delete()
                    println "${infoTag} ${project.rootProject.projectDir.relativePath(it)}"
                }
            }
        }
    }
}
