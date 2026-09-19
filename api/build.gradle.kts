plugins {
    id("java-library")
    id("com.vanniktech.maven.publish") version "0.35.0"
}

dependencies {
    compileOnly(libs.paperApi)
}

java {
    withSourcesJar()
    toolchain.languageVersion.set(JavaLanguageVersion.of(16))
}

tasks.named("publish") {
    dependsOn("publishToMavenLocal")
}

mavenPublishing {
    publishToMavenCentral()
    signAllPublications()

    coordinates(findProperty("centralGroup").toString(), "storm-api", version.toString())

    pom {
        name.set("Storm API")
        description.set("API for the Storm Bukkit/Paper plugin, which grants players timed, configurable effects and lets other plugins register their own effect types, apply and clear effects, and react to effect lifecycle events.")
        inceptionYear.set("2026")
        url.set("https://github.com/Wyne10/Storm")
        licenses {
            license {
                name.set("MIT License")
                url.set("https://opensource.org/licenses/MIT")
                distribution.set("https://opensource.org/licenses/MIT")
            }
        }
        developers {
            developer {
                id.set("Wyne10")
                name.set("Wyne")
                email.set("izmodenov1997@gmail.com")
                organization.set("BigTeam")
                organizationUrl.set("https://github.com/NeverMined-Entertainment")
            }
        }
        scm {
            url.set("https://github.com/Wyne10/Storm")
            connection.set("scm:git:git://github.com/Wyne10/Storm.git")
            developerConnection.set("scm:git:ssh://git@github.com/Wyne10/Storm.git")
        }
    }
}
