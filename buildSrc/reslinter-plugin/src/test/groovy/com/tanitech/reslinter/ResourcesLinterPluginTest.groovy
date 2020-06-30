package com.tanitech.reslinter

import static org.hamcrest.MatcherAssert.assertThat
import static org.hamcrest.CoreMatchers.containsString

import org.gradle.api.Project
import org.gradle.testfixtures.ProjectBuilder
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class ResourcesLinterPluginTest {

    private Project project

    @BeforeEach
    void setup() {
        project = ProjectBuilder.builder().build()

        File settings = project.file("settings.gradle")
        settings.write("""include(":a:b:c")""")

        File build = project.file("build.gradle")
        build.write("""
            boolean isRunningOnTeamCity() {
                return false
            }
            """
        )

        File module = project.file("a/b/c/build.gradle")
        module.parentFile.mkdirs()
        module.write("""
            plugins {
                id("com.spotify.android.resources-linter")
            }
            """
        )

        File sourceFile = project.file("a/b/c/src/main/u/v/Example.kt")
        sourceFile.parentFile.mkdirs()
        sourceFile.write(TestUtil.getSourceFileContent() + """
            mViews = UsedViewBindLayoutBinding.inflate(getLayoutInflater());
        """)
    }

    @Test
    void failIfMissingAppropriateFlag() {
        def result = TestUtil.getRunnerWithDefault(project.rootDir).withArguments(
                "lintUnusedResources"
        ).buildAndFail()

        assertThat(result.output, containsString("Run this task with -DincludeApps=all"))
    }

    @Test
    void skipAllUsedResources() {
        List<File> resourceFiles = []
        resourceFiles.add(project.file("a/b/c/src/main/res/drawable/used_drawable.xml"))
        resourceFiles.add(project.file("a/b/c/src/main/res/layout/used_layout.xml"))
        resourceFiles.add(project.file("a/b/c/src/main/res/layout/used_view_bind_layout.xml"))
        resourceFiles.add(project.file("a/b/c/src/main/res/anim/used_animation.xml"))

        resourceFiles.each {
            it.parentFile.mkdirs()
            it.createNewFile()
        }

        TestUtil.getRunnerWithDefault(project.rootDir).build()
    }

    @Test
    void flagUnusedStringResource() {
        File resourceFile = project.file("a/b/c/src/main/res/values/strings.xml")
        resourceFile.parentFile.mkdirs()
        resourceFile.write("""
            <?xml version="1.0" encoding="utf-8"?>
            <resources xmlns:tools="http://schemas.android.com/tools">
                <string name="unused_string">This is an used string</string>
            </resources>
        """.stripIndent())

        def result = TestUtil.getRunnerWithDefault(project.rootDir).buildAndFail()
        assertThat(result.output, containsString("unused_string"))
    }

    @Test
    void flagUnusedColorResource() {
        File resourceFile = project.file("a/b/c/src/main/res/values/colors.xml")
        resourceFile.parentFile.mkdirs()
        resourceFile.write("""
            <?xml version="1.0" encoding="utf-8"?>
             <resources>
                <color name="unused_color">#UNUSED</color>
            </resources>
        """.stripIndent())

        def result = TestUtil.getRunnerWithDefault(project.rootDir).buildAndFail()
        assertThat(result.output, containsString("unused_color"))
    }

    @Test
    void flagUnusedLayoutResource() {
        File resourceFile = project.file("a/b/c/src/main/res/layout/unused_layout.xml")
        resourceFile.parentFile.mkdirs()
        resourceFile.createNewFile()

        def result = TestUtil.getRunnerWithDefault(project.rootDir).buildAndFail()
        assertThat(result.output, containsString("unused_layout"))
    }

    @Test
    void flagUnusedDrawableResource() {
        File resourceFile = project.file("a/b/c/src/main/res/drawable/unused_drawable.xml")
        resourceFile.parentFile.mkdirs()
        resourceFile.createNewFile()

        def result = TestUtil.getRunnerWithDefault(project.rootDir).buildAndFail()
        assertThat(result.output, containsString("unused_drawable"))
    }

    @Test
    void flagUnusedAnimationResource() {
        File resourceFile = project.file("a/b/c/src/main/res/anim/unused_animation.xml")
        resourceFile.parentFile.mkdirs()
        resourceFile.createNewFile()

        def result = TestUtil.getRunnerWithDefault(project.rootDir).buildAndFail()
        assertThat(result.output, containsString("unused_animation"))
    }
}
