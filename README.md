# Share's Skin Changer

[![Build](https://github.com/tjnewbry/shares-skin-changer/actions/workflows/build.yml/badge.svg)](https://github.com/tjnewbry/shares-skin-changer/actions/workflows/build.yml)

**Share's Skin Changer** is a client-side Fabric mod for Minecraft 1.20.4 that changes your skin through the Mojang API without leaving the game.

- A **Change Skin** button on the title screen, with a preview of the skin Mojang's server currently has for you under it.
- Pick a skin file, check the detected arm width (3px slim or 4px classic, click the text to flip it), preview it on a 3D model (drag to rotate), and upload.
- A smaller preview shows what Mojang's server sees, with your name tag over it, a Reload button, and a log of upload and reload results.
- After a successful upload the skin refreshes in the running game, so singleplayer shows it without a restart (multiplayer servers pick it up when you join).
- It uses the session of the account you are logged into, so no token file is needed.

## Instructions for Build and Use

Steps to build and/or run the software:

1. Install JDK 17 or newer.
2. Clone the repository: `git clone https://github.com/tjnewbry/shares-skin-changer.git`
3. Go into the mod project: `cd shares-skin-changer/mod`
4. Build: `./gradlew build` (on Windows Command Prompt: `gradlew.bat build`)
5. Copy `mod/build/libs/shares-skin-changer-<version>+1.20.4.jar` into the `mods` folder of a Minecraft 1.20.4 Fabric instance that also has Fabric API installed.

Instructions for using the software:

1. Launch the instance while logged into your Microsoft account.
2. On the title screen, click **Change Skin**.
3. Click **Choose File...** and pick a 64x64 PNG skin.
4. Check the detected arm width. Click the arm text to switch between slim (3px) and classic (4px).
5. Drag the preview to look at the skin from any side, then click **Upload**.
6. The log shows **Uploaded!**, and the "what the server sees" preview updates. Click **Reload** at any time to fetch your skin from Mojang's server again.

## Development Environment

To recreate the development environment, you need the following software and/or libraries with the specified versions:

* JDK 17 (the mod targets Java 17)
* Gradle 8.8 (downloaded automatically by the included Gradle wrapper)
* Fabric Loom 1.6 (Gradle plugin, pinned so the build runs on JDK 17)
* Minecraft 1.20.4 with Yarn mappings 1.20.4+build.3
* Fabric Loader 0.15.11
* Fabric API 0.97.3+1.20.4

## Useful Websites to Learn More

I found these websites useful in developing this software:

* [Mojang API Documentation](https://minecraft.wiki/w/Mojang_API)
* [Minecraft Skin Checker](https://namemc.com/)
* [Fabric Develop](https://fabricmc.net/develop)

## Future Work

The following items I plan to fix, improve, and/or add to this project in the future:

* [ ] Support old-style 64x32 skins (currently rejected)
* [ ] Move the Fabric project to the repository root now that the CLI is gone
* [ ] Read skins with Minecraft's own image class instead of Java's ImageIO

## History

The project started as a command-line program that read the access token from Prism Launcher's accounts file and uploaded a skin. Its validation and upload code was split into a UI-free `core` package, and the mod was built on top of it, sharing that code with the CLI. Once the mod covered everything, the CLI was removed.

- Last version with the CLI: tag [`cli-final`](../../tree/cli-final)
- Original CLI design notes: [docs/cli-design.md](docs/cli-design.md)
- [Software Demo Video](https://youtu.be/i0mMj63wjVo) (command-line version)

## Layout

```
src/net/tjnewbry/skinhead/core/   skin validation, arm detection, and the upload request
mod/                              Fabric mod project; compiles ../src as well as its own sources
docs/cli-design.md                the original CLI design, written before any code
docs/screenshots/                 screenshots for this README
test-skins/                       skins for checking arm width detection
```

## License

[MIT](LICENSE)
