rootProject.name = "KmpGLDemo"
enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")

pluginManagement {
    repositories {
        google {
            mavenContent {
                includeGroupAndSubgroups("androidx")
                includeGroupAndSubgroups("com.android")
                includeGroupAndSubgroups("com.google")
            }
        }
        mavenCentral()
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    repositories {
        google {
            mavenContent {
                includeGroupAndSubgroups("androidx")
                includeGroupAndSubgroups("com.android")
                includeGroupAndSubgroups("com.google")
            }
        }
        mavenCentral()
    }
}

includeBuild("library") {
    dependencySubstitution {
        substitute(module("kmpgl:kmpgl-core")).using(project(":kmpgl-core"))
        substitute(module("kmpgl:kmpgl-compose")).using(project(":kmpgl-compose"))
    }
}
include(":composeApp")
