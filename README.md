# King of the Ladder

[![CI](https://github.com/Despical/KOTL/actions/workflows/build.yml/badge.svg)](https://github.com/Despical/KOTL/actions/workflows/build.yml)
![Java 25](https://img.shields.io/badge/Java-25-007396.svg)
![Gradle](https://img.shields.io/badge/Gradle-9.6.1-079ec0?logo=gradle&logoColor=white)
![Minecraft](https://img.shields.io/badge/Minecraft-26.2-62b47a)
[![License: GPL v3](https://img.shields.io/badge/License-GPLv3-blue.svg)](LICENSE)

King of the Ladder is a competitive Minecraft minigame where players climb to the top of a ladder tower, claim the crown, and knock challengers back down.

Players enter an arena built around a ladder and fight their way toward the king plate at the top. Reaching the plate makes a player the king and adds to their score, but holding the position becomes harder as other players climb up and try to take the crown. The goal is to become king as many times as possible while keeping opponents away from the top.

---

## Features

- Configurable arenas with an in-game setup menu, area selection, plate materials, game modes, and per-arena settings.
- Customizable messages, scoreboards, boss bars, sounds, items, and menus.
- Player statistics, per-arena records, leaderboards, cooldowns, and PlaceholderAPI support.
- Flat-file storage by default, with optional MySQL persistence.
- Public API for arena flow, king changes, and player statistic events.
- Optional hooks for common server plugins such as PlaceholderAPI and world-management plugins.

---

## Requirements

- Java 25
- A Paper-compatible Minecraft server

---

## Resources

- [Documentation](https://docs.despical.dev/kotl/)
- [Javadocs](https://javadoc.despical.dev/kotl/)
- [SpigotMC](https://spigotmc.org/resources/king-of-the-ladder.80686/)
- [BuiltByBit](https://builtbybit.com/resources/king-of-the-ladder.51128/)

---

## Building

Clone the repository:

```bash
git clone https://github.com/Despical/KOTL.git
cd KOTL
```

Build the plugin jar:

```bash
./gradlew shadowJar
```

On Windows:

```cmd
gradlew.bat shadowJar
```

The packaged jar is created under `build/libs/`.

Run the full verification used by CI:

```bash
./gradlew build
```

On Windows:

```cmd
gradlew.bat build
```

---

## Configuration

Configuration files are bundled in `src/main/resources` and copied to the plugin data folder on first startup. Most server-facing behavior can be adjusted without rebuilding the plugin.

Common files:

- `config.yml` controls gameplay, storage, chat, commands, cooldowns, and arena detection.
- `arenas.yml` stores arena data and records.
- `messages.yml`, `scoreboard.yml`, `bossbar.yml`, and `items.yml` control presentation.
- `mysql.yml` configures MySQL when database storage is enabled.
- `menu/` contains menu layouts used by setup and statistics screens.

---

## API

King of the Ladder exposes Bukkit events under `dev.despical.kotl.api.events` for game shutdown, arena entry and exit, king changes, and player statistic updates.

Maven:

```xml
<repository>
    <id>jitpack.io</id>
    <url>https://jitpack.io</url>
</repository>
```

```xml
<dependency>
    <groupId>com.github.Despical</groupId>
    <artifactId>KOTL</artifactId>
    <version>main-SNAPSHOT</version>
    <scope>provided</scope>
</dependency>
```

Gradle:

```gradle
repositories {
    maven { url = 'https://jitpack.io' }
}
```

```gradle
dependencies {
    compileOnly group: 'com.github.Despical', name: 'KOTL', version: 'main-SNAPSHOT'
}
```

---

## Integrations

Optional integrations declared by the plugin include:

- PlaceholderAPI
- Multiverse-Core
- SlimeWorldManager
- SlimeWorldPlugin
- MultiWorld
- My_Worlds
- WorldGuard

---

## Security

We prioritize user privacy and application integrity. Please do not open public issues for discovered vulnerabilities.

Read our [SECURITY.md](SECURITY.md) for responsible disclosure reporting.

---

## Contributing

We welcome Pull Requests from the community. To help us maintain clean project history and formatting, please follow these guidelines:

* **No tabs:** Use spaces exclusively for indentation.
* **Style consistency:** Respect the established code architecture and style templates.
* **Version control cleanliness:** Do not increment project version numbers in example configurations within your PR.
* **Minimal diffs:** Disable automated reformat-on-save settings that affect untouched files.

Learn more via our formal [Contribution Guidelines](CONTRIBUTING.md).

Please also follow our [Code of Conduct](CODE_OF_CONDUCT.md) when participating in the project.

---

## License

This project is licensed under the [GPL-3.0 License](http://www.gnu.org/licenses/gpl-3.0.html).

See the [LICENSE](LICENSE) file for comprehensive copyright notices and third-party attributions.
