package com.tanitech.reslinter

import static org.hamcrest.CoreMatchers.hasItems
import static org.junit.jupiter.api.Assertions.assertEquals
import static org.junit.jupiter.api.Assertions.assertTrue
import static org.hamcrest.MatcherAssert.assertThat
import static org.hamcrest.CoreMatchers.containsString

import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.EnumSource
import org.junit.jupiter.api.Test
import org.gradle.api.Project
import org.gradle.testfixtures.ProjectBuilder
import org.junit.jupiter.api.BeforeEach

class ResourcesScannerTest {

    private ResourcesScanner scanner
    private Project project

    @BeforeEach
    void setup() {
        project = ProjectBuilder.builder().build()
        File collectedRefs = project.file("allUsedRefs")
        File sourceFile = project.file("src/a/b/c/Example.java")
        sourceFile.parentFile.mkdirs()
        sourceFile.write(TestUtil.getSourceFileContent())

        scanner = new ResourcesScanner(project, collectedRefs, [])
    }

    @ParameterizedTest
    @EnumSource(ResourceType.class)
    void collectOnlyUsedResources(ResourceType resourceType) {
        String[] usedResources = scanner.collectResourceReferences([resourceType] as ResourceType[])
        assertEquals(1, usedResources.size())
        assertThat(usedResources.first(), containsString("used_${resourceType.toString()}"))
    }

    @ParameterizedTest
    @EnumSource(ResourceType.class)
    void doNotReportVoidResource(ResourceType resourceType) {
        scanner.collectResourceReferences([resourceType] as ResourceType[])
        Map<Project, Set<String>> unused = scanner.getUnusedResourcesForType(resourceType)
        assertTrue(unused.containsKey(project))
        assertTrue(unused[project].isEmpty())
    }

    @Test
    void reportOnlyUnusedDrawableResource() {
        List<File> resourceFiles = []
        resourceFiles.add(project.file("src/a/res/drawable/used_drawable.xml"))
        resourceFiles.add(project.file("src/a/res/drawable/unused_drawable.xml"))

        resourceFiles.each {
            it.parentFile.mkdirs()
            it.createNewFile()
        }

        ResourceType resourceType = ResourceType.DRAWABLE
        scanner.collectResourceReferences([resourceType] as ResourceType[])
        Map<Project, Set<String>> unused = scanner.getUnusedResourcesForType(resourceType)

        assertTrue(unused.containsKey(project))
        assertEquals(1, unused[project].size())
        assertThat(unused[project], hasItems("unused_drawable"))
    }

    @Test
    void reportOnlyUnusedLayoutResource() {
        List<File> resourceFiles = []
        resourceFiles.add(project.file("src/a/res/layout/used_layout.xml"))
        resourceFiles.add(project.file("src/a/res/layout/unused_layout.xml"))

        resourceFiles.each {
            it.parentFile.mkdirs()
            it.createNewFile()
        }

        ResourceType resourceType = ResourceType.LAYOUT
        scanner.collectResourceReferences([resourceType] as ResourceType[])
        Map<Project, Set<String>> unused = scanner.getUnusedResourcesForType(resourceType)

        assertTrue(unused.containsKey(project))
        assertEquals(1, unused[project].size())
        assertThat(unused[project], hasItems("unused_layout"))
    }

    @Test
    void reportOnlyUnusedAnimationResource() {
        List<File> resourceFiles = []
        resourceFiles.add(project.file("src/a/res/anim/used_animation.xml"))
        resourceFiles.add(project.file("src/a/res/anim/unused_animation_1.xml"))
        resourceFiles.add(project.file("src/a/res/animator/unused_animation_2.xml"))

        resourceFiles.each {
            it.parentFile.mkdirs()
            it.createNewFile()
        }

        ResourceType resourceType = ResourceType.ANIM
        scanner.collectResourceReferences([resourceType] as ResourceType[])
        Map<Project, Set<String>> unused = scanner.getUnusedResourcesForType(resourceType)

        assertTrue(unused.containsKey(project))
        assertEquals(2, unused[project].size())
        assertTrue(unused[project].any { (it == "unused_animation_1") })
        assertTrue(unused[project].any { (it == "unused_animation_2") })
    }

    @Test
    void reportOnlyUnusedStringResource() {
        File resourceFile = project.file("src/a/res/values/strings.xml")
        resourceFile.parentFile.mkdirs()
        resourceFile.write("""
            <?xml version="1.0" encoding="utf-8"?>
            <resources xmlns:tools="http://schemas.android.com/tools">
                <string name="unused_string">This is an used string</string>
            </resources>
        """.stripIndent())

        ResourceType resourceType = ResourceType.STRING
        scanner.collectResourceReferences([resourceType] as ResourceType[])
        Map<Project, Set<String>> unused = scanner.getUnusedResourcesForType(resourceType)

        assertTrue(unused.containsKey(project))
        assertEquals(1, unused[project].size())
        assertThat(unused[project], hasItems("unused_string"))
    }

    @Test
    void reportOnlyUnusedColorResource() {
        File resourceFile = project.file("src/a/res/values/colors.xml")
        resourceFile.parentFile.mkdirs()
        resourceFile.write("""
            <?xml version="1.0" encoding="utf-8"?>
            <resources>
                <color name="used_color">#USED</color>
                <color name="unused_color">#UNUSED</color>
            </resources>
        """.stripIndent())

        ResourceType resourceType = ResourceType.COLOR
        scanner.collectResourceReferences([resourceType] as ResourceType[])
        Map<Project, Set<String>> unused = scanner.getUnusedResourcesForType(resourceType)

        assertTrue(unused.containsKey(project))
        assertEquals(1, unused[project].size())
        assertThat(unused[project], hasItems("unused_color"))
    }
}
