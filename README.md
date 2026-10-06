[![Build](https://github.com/tjnewbry/shares-skin-changer/actions/workflows/build.yml/badge.svg)](https://github.com/tjnewbry/shares-skin-changer/actions/workflows/build.yml)

# Overview

**Share's Skin Changer** is a client-side Fabric mod for Minecraft 1.20.4 that changes your skin through the Mojang API without leaving the game.

- A **Change Skin** button on the title screen, with a preview of the skin Mojang's server currently has for you under it.
- Pick a skin file, check the detected arm width (3px slim or 4px classic, click the text to flip it), preview it on a 3D model (drag to rotate), and upload.
- A smaller preview shows what Mojang's server sees, with your name tag over it, a Reload button, and a log of upload and reload results.
- After a successful upload the skin refreshes in the running game, so singleplayer shows it without a restart (multiplayer servers pick it up when you join).
- It uses the session of the account you are logged into, so no token file is needed.

# History

The project started as a command-line program that read the access token from Prism Launcher's accounts file and uploaded a skin. Its validation and upload code was split into a UI-free `core` package, and the mod was built on top of it, sharing that code with the CLI. Once the mod covered everything, the CLI was removed.

- Last version with the CLI: tag [`cli-final`](../../tree/cli-final)
- Original CLI design notes: [docs/cli-design.md](docs/cli-design.md)
- [Software Demo Video](https://youtu.be/i0mMj63wjVo) (command-line version)

# Layout

```
src/net/tjnewbry/skinhead/core/   skin validation, arm detection, and the upload request
mod/                              Fabric mod project; compiles ../src as well as its own sources
docs/cli-design.md                the original CLI design, written before any code
docs/screenshots/                 screenshots for this README
test-skins/                       skins for checking arm width detection
```

# Building

Gradle 8.8 and Loom 1.6 are pinned so the mod builds on JDK 17:

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

- Old-style 64x32 skins are rejected.

# License

[MIT](LICENSE)
