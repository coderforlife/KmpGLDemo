import org.gradle.api.publish.maven.MavenPublication
import org.gradle.api.tasks.bundling.Jar
import org.gradle.kotlin.dsl.`maven-publish`

plugins {
    `maven-publish`
    signing
}

publishing {
    // Configure all publications
    publications.withType<MavenPublication> {
        // Stub javadoc.jar artifact
        artifact(tasks.register("${name}JavadocJar", Jar::class) {
            archiveClassifier.set("javadoc")
            archiveAppendix.set(this@withType.name)
        })

        groupId = "edu.moravian.kmpgl"
        artifactId = "alpha"
        version = "0.1"

        // Provide artifacts information required by Maven Central
        pom {
            name.set("Kotlin Multiplatform GL Library")
            description.set("Library for using OpenGL ES in Kotlin Multiplatform")
            url.set("https://github.com/MoravianUniversity/KmpGL")

            licenses {
                license {
                    name.set("MIT")
                    url.set("https://opensource.org/licenses/MIT")
                }
            }
            developers {
                developer {
                    id.set("Jeffrey Bush")
                    name.set("Jeffrey Bush")
                    organization.set("Moravian University")
                    organizationUrl.set("https://github.com/MoravianUniversity/")
                }
            }
            scm {
                url.set("https://github.com/MoravianUniversity/KmpGL")
            }
        }
    }
}

signing {
    if (project.hasProperty("signing.gnupg.keyName")) {
        useGpgCmd()
        sign(publishing.publications)
    }
}
