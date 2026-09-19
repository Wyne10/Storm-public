---
description: >-
  Clone with submodules, build the shaded plugin jar, run a test server, and
  publish the API locally.
---

# Building from source

## What you need

Git, a JDK to run Gradle with, and nothing else—the Gradle wrapper fetches Gradle itself, and the build provisions the JDKs it actually compiles and runs with: Java 16 for compilation, and a JetBrains Runtime 21 for the test server.

## Clone

`gradle/libs.versions.toml` is a symlink into the `libs` submodule, which holds the version catalog shared across these plugins. A clone without it fails while Gradle configures the build:

```bash
git clone --recurse-submodules https://github.com/Wyne10/Storm.git
```

If you already cloned without `--recurse-submodules`, run `git submodule update --init`.

## Build

```bash
./gradlew shadowJar
```

The shaded plugin jar lands in `build/libs/Storm-<version>.jar`, ready to drop into a server's `plugins/` folder. The build strips unused classes and relocates Guice, Guava, Adventure, EnhancedLegacyText and WUtils under `me.wyne.storm.shadow`, so the plugin can't collide with another plugin bundling the same libraries.

| Command                            | What it does                                                                          |
| ---------------------------------- | ------------------------------------------------------------------------------------- |
| `./gradlew shadowJar -Pdebug=true` | Builds without relocating anything, which keeps stack traces readable while debugging |
| `./gradlew :api:build`             | Builds the API module on its own                                                      |
| `./gradlew clean shadowJar`        | Rebuilds from scratch                                                                 |

The project version lives in `gradle.properties` and names both the jar and the published API artifact.

## Run a test server

```bash
./gradlew runServer
```

This builds the shaded jar, downloads Paper 1.16.5 along with CommandAPI, PlaceholderAPI, LuckPerms, ProtocolLib, ViaVersion, and ViaBackwards, then starts a server in `run-1.16.5/` with the plugin installed. Add `-Pdebug=true` to run on 1.19.4 in `run-1.19.4/` instead, with relocation off.

ConnectionSource and InfPoints aren't downloaded. Put their jars in the server's `plugins/` folder yourself to test storage or purchases.

The server's copy of the config is `run-<version>/plugins/Storm/config.yml`. Add effects to it and run [`/effects reload`](commands-and-permissions.md) to try them without restarting.

## Publish the API

The `api` module is the only thing published; the plugin jar itself is distributed as a jar, not as a dependency.

To try a release locally, into `~/.m2`:

```bash
./gradlew :api:publishToMavenLocal
```
