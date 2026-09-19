import net.minecrell.pluginyml.bukkit.BukkitPluginDescription
import org.codehaus.plexus.util.Os

plugins {
    kotlin("jvm") version "2.4.20"
    alias(libs.plugins.shadow)
    alias(libs.plugins.runPaper)
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
    compileOnly(libs.infPoints)

    implementation(project(":api"))
    implementation(libs.guice)
    implementation(libs.enhancedLegacy)
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
            relocate("com.google.inject", "me.wyne.storm.shadow.google.guice")
            relocate("com.google.common", "me.wyne.storm.shadow.google.common")
            relocate("net.kyori", "me.wyne.storm.shadow.net.kyori")
            relocate("dev.vankka", "me.wyne.storm.shadow.dev.vankka")
            relocate("me.wyne.wutils", "me.wyne.storm.shadow.wutils")
        }
    }

    runServer {
        val minecraftVersion: String = if (Os.isFamily(Os.FAMILY_WINDOWS) || isDebug) "1.19.4" else "1.16.5"
        val viaVersion = "5.11.0"
        val commandApiVersion = "9.4.2"
        downloadPlugins {
            url("https://download.luckperms.net/1671/bukkit/loader/LuckPerms-Bukkit-5.5.84.jar")
            github("PlaceholderAPI", "PlaceholderAPI", "2.12.2", "PlaceholderAPI-2.12.2.jar")
            github("dmulloy2", "ProtocolLib", "5.4.0", "ProtocolLib.jar")
            github("ViaVersion", "ViaVersion", viaVersion, "ViaVersion-$viaVersion.jar")
            github("ViaVersion", "ViaBackwards", viaVersion, "ViaBackwards-$viaVersion.jar")
            github("CommandAPI", "CommandAPI", commandApiVersion, "CommandAPI-$commandApiVersion.jar")
        }
        runDirectory(layout.projectDirectory.dir("run-$minecraftVersion").asFile)
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
    main = "me.wyne.storm.Storm"
    apiVersion = "1.16"
    softDepend = listOf("PlaceholderAPI", "CommandAPI", "ConnectionSource", "InfPoints")
    permissions {
        register("effects.*") {
            children = listOf("effects.reload", "effects.apply", "effects.clear", "effects.purchase")
            default = BukkitPluginDescription.Permission.Default.OP
        }
        register("effects.reload") {
            description = "Allows to reload plugin"
        }
        register("effects.apply") {
            description = "Allows to apply effects on players"
        }
        register("effects.clear") {
            description = "Allows to clear effects on players"
        }
        register("effects.purchase") {
            description = "Allows to purchase effects for players"
        }
    }
}