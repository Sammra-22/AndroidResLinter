val junitVersion = "5.6.2"
val hamcrestVersion = "2.2"

plugins {
    `kotlin-dsl`
}

allprojects {
    apply(plugin = "java")

    repositories {
        google()
        jcenter()
    }
}

subprojects {
    dependencies {
        testImplementation(gradleTestKit())
        testImplementation("org.junit.jupiter:junit-jupiter-api:$junitVersion")
        testImplementation("org.junit.jupiter:junit-jupiter-params:$junitVersion")
        testImplementation("org.hamcrest:hamcrest:$hamcrestVersion")
        testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:$junitVersion")
    }

    rootProject.dependencies {
        runtimeOnly(project(path))
    }
}
