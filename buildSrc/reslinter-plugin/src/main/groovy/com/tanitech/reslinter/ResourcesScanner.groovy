package com.tanitech.reslinter

import groovy.io.FileType
import org.gradle.api.Project

/**
 * Scan and spot unused resources in a given project
 **/
class ResourcesScanner {

    final Project project
    final List<String> whitelisted
    File allUsedReferences

    ResourcesScanner(Project project, File allUsedReferences, List<String> whitelisted = []) {
        this.project = project
        this.allUsedReferences = allUsedReferences
        this.whitelisted = whitelisted
    }

    private Closure<Boolean> isNotWhitelisted = {
        Project project -> !whitelisted.any { project.path.startsWith(it) }
    }

    Map<Project, Set<String>> getUnusedResourcesForType(ResourceType resourceType) {
        /* Scan all defined resources for a given type & compare with the listed used resources */
        def unusedResourcesMap = [:]
        Utils.getProjectsWithSource(project).findAll(isNotWhitelisted).each {
            Project project ->
                def definedResources = getDeclaredResources(project, resourceType)
                def allUsedTypedResources = (allUsedReferences as String[]).findAll {
                    it.matches "^(${resourceType.refMatcher})[.].*"
                }.collect {
                    it.tokenize(".")[1]
                }.toSet()
                unusedResourcesMap[project] = definedResources.findAll { !allUsedTypedResources.contains(it) }.toSet()
        }
        return unusedResourcesMap
    }

    Set<String> collectResourceReferences(ResourceType[] resourceTypes) {
        /* Collect all used dependencies from Java, Kotlin and XML files */
        def resourcesPattern = resourceTypes.collect { it.refMatcher }.join("|")
        def dataBindPattern = "Binding"
        Set<String> usedResReferences = []
        Utils.getProjectsWithSource(project.rootProject).each {
            Project project ->
                project.logger.lifecycle "Scanning ${project.path} ..."
                project.file("src").eachFileRecurse(FileType.FILES) {
                    if (it.path.matches(".*\\.(java|kt|xml)")) {
                        it.text.findAll("($resourcesPattern)\\s*(\\.|/)\\s*[a-z0-9_]+").each {
                            usedResReferences << it.replaceAll('\\s', '').replaceAll('/', '.')
                        }
                        // Data Binding resources
                        if (resourceTypes.contains(ResourceType.LAYOUT)) {
                            it.text.findAll("[A-Z][a-zA-Z0-9_]+$dataBindPattern\\b").each {
                                def dataBindRef = it.replaceAll(/$dataBindPattern$/, '')
                                def dataBindRes = dataBindRef.replaceAll(/\B[A-Z]/) { String[] parts ->
                                    '_' + parts.join('')
                                }.toLowerCase()
                                usedResReferences << "layout.$dataBindRes"
                            }
                        }
                    }
                }
        }
        usedResReferences.each { allUsedReferences << it << "\n" }
        return usedResReferences
    }

    private static String[] getDeclaredResources(Project project, ResourceType resourceType) {
        Set defined = []
        project.file("src").eachFileRecurse(FileType.FILES) {
            if (it.path.matches(resourceType.pathMatcher)) {
                if (resourceType.isResourceValue()) {
                    defined.addAll it.text.findAll('name="[a-z0-9_]*"').collect { it.tokenize('"')[1] }
                } else {
                    defined << it.name.tokenize(".")[0]
                }

            }
        }
        return defined
    }
}
