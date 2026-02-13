import net.minecrell.pluginyml.bukkit.BukkitPluginDescription
import org.codehaus.plexus.util.Os

plugins {
    kotlin("jvm") version "2.3.10"
    alias(libs.plugins.shadow)
    alias(libs.plugins.runPaperFork)
    alias(libs.plugins.pluginYml)
}

kotlin {
    jvmToolchain(16)
}

dependencies {
    compileOnly(libs.paperApi)
    compileOnly(libs.placeholderApi)
    compileOnly(libs.commandApi)
    compileOnly(libs.connectionSource)

    implementation(project(":api"))
    implementation(libs.guice)
    implementation(libs.adventureMini)
    implementation(libs.adventureBukkit)
    implementation(libs.adventurePlain)

    implementation(libs.wutilsConfig)
    implementation(libs.wutilsConfigurables)
    implementation(libs.wutilsI18nKotlin)
    implementation(libs.wutilsCommonKotlin)
}

tasks {
    val isDebug = findProperty("debug")?.toString()?.toBoolean() ?: false

    shadowJar {
        archiveBaseName.set(findProperty("name").toString())
        archiveClassifier.set("")
        minimize()
        if (!isDebug) {
            relocate("com.google.inject", "org.bigcraft.storm.shadow.google.guice")
            relocate("com.google.common", "org.bigcraft.storm.shadow.google.common")
            relocate("net.kyori", "org.bigcraft.storm.shadow.net.kyori")
            relocate("me.wyne.wutils", "org.bigcraft.storm.shadow.wutils")
        }
    }

    runServer {
        val minecraftVersion: String = if (Os.isFamily(Os.FAMILY_WINDOWS) || isDebug) "1.19.4" else "1.16.5"
        val viaVersion = "5.7.1"
        val commandApiVersion = "9.4.2"
        downloadPlugins {
            url("https://download.luckperms.net/1620/bukkit/loader/LuckPerms-Bukkit-5.5.32.jar")
            github("PlaceholderAPI", "PlaceholderAPI", "2.12.2", "PlaceholderAPI-2.12.2.jar")
            github("dmulloy2", "ProtocolLib", "5.4.0", "ProtocolLib.jar")
            github("ViaVersion", "ViaVersion", viaVersion, "ViaVersion-$viaVersion.jar")
            github("ViaVersion", "ViaBackwards", viaVersion, "ViaBackwards-$viaVersion.jar")
            github("CommandAPI", "CommandAPI", commandApiVersion, "CommandAPI-$commandApiVersion.jar")
        }
        runDirectory(layout.projectDirectory.dir("run-$minecraftVersion").asFile)
        serverTemplates(layout.projectDirectory.dir("run-template").asFile)
        minecraftVersion(minecraftVersion)
    }

    compileJava {
        options.encoding = Charsets.UTF_8.name()
    }
}

tasks.withType(xyz.jpenilla.runtask.task.AbstractRun::class) {
    javaLauncher = javaToolchains.launcherFor {
        vendor = JvmVendorSpec.JETBRAINS
        languageVersion = JavaLanguageVersion.of(21)
    }
    jvmArgs("-XX:+AllowEnhancedClassRedefinition", "-DPaper.IgnoreJavaVersion=true")
}

bukkit {
    name = findProperty("name").toString()
    version = getVersion().toString()
    website = findProperty("website").toString()
    author = findProperty("author").toString()
    main = "org.bigcraft.storm.Storm"
    apiVersion = "1.16"
    softDepend = listOf("PlaceholderAPI", "CommandAPI")
    permissions {
        register("effects.*") {
            children = listOf("effects.reload")
            default = BukkitPluginDescription.Permission.Default.OP
        }
        register("effects.reload") {
            description = "Allows to reload plugin"
        }
    }
}