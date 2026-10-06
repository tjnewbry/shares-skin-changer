# Overview

This project lets users change their minecraft skin in two ways, both sharing the same core code:

- **Command-line program** (`src/`): locates account tokens and sends a payload to the Mojang API.
- **Fabric mod "Share's Skin Changer"** (`mod/`, Minecraft 1.20.4): adds a **Change Skin** button to the title screen. Pick a skin file, check the detected arm width (3px slim or 4px classic, with a button to flip it), preview the skin on a 3D model (drag to rotate), upload, and see the API response on screen. After a successful upload the mod refreshes your skin in the running game, so singleplayer shows it without a restart (multiplayer servers pick it up when you join). The mod uses the session of the account you are logged into, so no token file is needed.

[Software Demo Video](https://youtu.be/i0mMj63wjVo) (command-line version)

# Layout

```
src/net/tjnewbry/skinhead/core/   shared by the CLI and the mod (skin validation, API upload)
src/net/tjnewbry/skinhead/cli/    command-line only (launcher config, token lookup, prompts)
mod/                              Fabric mod project; compiles ../src minus cli/
docs/cli-design.md                the original CLI design, written before any code
test-skins/                       skins for checking arm width detection
```

# Building

**CLI** (needs a JDK; `jar` is found through `JAVA_HOME`, the PATH, or the JDK that `java` runs from):

```
./build.sh
java -jar dist/mc-skin-changer.jar
```

**Mod** (Gradle 8.8 and Loom 1.6 are pinned so it builds on JDK 17):

```
cd mod
./gradlew build
```

The jar is written to `mod/build/libs/shares-skin-changer-<version>+1.20.4.jar`. Drop it in a Minecraft 1.20.4 Fabric instance's `mods` folder alongside Fabric API.

# Development Environment

OpenJDK 17 or newer (the mod targets Java 17).

# Useful Websites

- [Mojang API Documentation](https://minecraft.wiki/w/Mojang_API)
- [Minecraft Skin Checker](https://namemc.com/)
- [Fabric Develop](https://fabricmc.net/develop)

# Future Work

- The CLI only supports Prism Launcher because that's what I use. If this was anything more than a proof of concept then I would also support Minecraft Launcher, Modrinth, and MultiMC.
- Old-style 64x32 skins are rejected.

# License

[MIT](LICENSE)
