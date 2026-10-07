# Share's Skin Changer

[![Build](https://github.com/tjnewbry/shares-skin-changer/actions/workflows/build.yml/badge.svg)](https://github.com/tjnewbry/shares-skin-changer/actions/workflows/build.yml)

**Share's Skin Changer** is a client-side Fabric mod for Minecraft 1.20.4 that adds a menu on the title screen allowing players to change their skin without leaving the game.

[Software Demo Video](https://youtu.be/id9l-zBFdKs)

## Mod Dependencies

* Fabric API

## Instructions for Installation and Use

Install the latest version of Fabric for Minecraft 1.20.4 and put the .jar file from the most recent release into your mods folder. There are many Minecraft mod launchers, such Modrinth, that can guide this process.

Once mod is installed, the "Change Skin" menu can be found in the top left corner of the title screen.

## Development Environment

* JDK 17 (the mod targets Java 17)
* Gradle 8.8 (downloaded automatically by the included Gradle wrapper)
* Fabric Loom 1.6 (Gradle plugin, pinned so the build runs on JDK 17)
* Minecraft 1.20.4 with Yarn mappings 1.20.4+build.3
* Fabric Loader 0.15.11
* Fabric API 0.97.3+1.20.4

## Useful Websites to Learn More

* [Mojang API Documentation](https://minecraft.wiki/w/Mojang_API)
* [Minecraft Skin Checker](https://namemc.com/)
* [Fabric Develop](https://fabricmc.net/develop)

## Future Work

* [ ] Code motion and optimization pass
* [ ] Read skins with Minecraft's own image class instead of Java's ImageIO
* [ ] Port to other Minecraft versions, including those that require Java 25

## History

The project started as a command-line program that read the access token from Prism Launcher's accounts file and uploaded a skin. Its validation and upload code was split into a UI-free `core` package, and the mod was built on top of it, sharing that code with the CLI. Once the mod covered everything, the CLI was removed.

- Last version with the CLI: tag [`cli-final`](../../tree/cli-final)
- Original CLI design notes: [docs/cli-design.md](docs/cli-design.md)
- [Software Demo Video](https://youtu.be/i0mMj63wjVo) (command-line version)

## Layout

```
src/main/java/net/tjnewbry/skinhead/core/   skin validation, arm detection, and the upload request (from the CLI)
src/main/java/net/tjnewbry/skinhead/mod/    the Fabric mod: title screen hook, skin screen, previews
src/main/resources/                         fabric.mod.json and the mod icon
docs/cli-design.md                          the original CLI design, written before any code
docs/screenshots/                           screenshots for this README
test-skins/                                 skins for checking arm width detection
```

## License

[MIT](LICENSE)
