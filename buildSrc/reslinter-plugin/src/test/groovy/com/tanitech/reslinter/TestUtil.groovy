package com.tanitech.reslinter

import org.gradle.testkit.runner.GradleRunner

class TestUtil {

    static String getSourceFileContent() {
        return """
            package com.foo.bar;
            class SomeAndroidClass {
                void someMethod(){
                    setContentView(R.layout.used_layout);
                    imageView.setImageResource(R.drawable.used_drawable);
                    AnimationUtils.loadAnimation(this, R.anim.used_animation);
                    textView.setText(R.string.used_string)
                    textView.setTextColor(getResources().getColor(R.color.used_color));
                }
            }
        """.stripIndent()
    }

    static GradleRunner getRunnerWithDefault(File projectDir) {
        return GradleRunner.create()
                .withProjectDir(projectDir)
                .withArguments(
                        "-DincludeApps=all",
                        "lintUnusedResources"
                )
                .withPluginClasspath()
    }
}
