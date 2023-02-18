<h1 align="center">King of the Ladder</h1>

<div align="center">

[![Build](https://github.com/Despical/KOTL/actions/workflows/build.yml/badge.svg?branch=master)](https://github.com/Despical/KOTL/actions/workflows/build.yml)
[![](https://jitpack.io/v/Despical/KOTL.svg)](https://jitpack.io/#Despical/KOTL)
[![](https://img.shields.io/badge/JavaDocs-latest-lime.svg)](https://javadoc.jitpack.io/com/github/Despical/KOTL/latest/javadoc/index.html)
[![Discord](https://img.shields.io/discord/719922452259668000.svg?color=lime&label=Discord)](https://discord.gg/rVkaGmyszE)
[![Support](https://img.shields.io/badge/Patreon-Support-lime.svg?logo=Patreon)](https://www.patreon.com/despical)

King of the Ladder is an old Minecraft minigame that supports almost every version. Each player have to kick other players
out of the ladders to be the king. The king tries to stand on the top of the ladders while not letting others to climb and
punching them out of the way! If you have any problem with this plugin check out our [wiki](https://github.com/Despical/KOTL/wiki).
If you still didn't find an answer see documentation section below.

</div>

## Documentation
- [Wiki](https://github.com/Despical/KOTL/wiki)
- [JavaDocs](https://javadoc.jitpack.io/com/github/Despical/KOTL/latest/javadoc/index.html)
- [Tutorial Video](https://www.youtube.com/watch?v=O_vkf_J4OgY)
- [Discord Community](https://www.discord.gg/rVkaGmyszE)

## Donations
You like the KOTL? Then [donate](https://www.patreon.com/despical) back me to support the development.
Donations are more like motivation than money and, they are speeding up the development.

## Using King of the Ladder API
The project isn't in the Central Repository yet, so specifying a repository is needed.<br>

<details>
<summary>Maven dependency</summary>

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
    <version>2.4.9</version>
    <scope>compile</scope>
</dependency>
```

</details>

<details>
<summary>Gradle dependency</summary>

```
repositories {
    maven { url 'https://jitpack.io' }
}
```
```
dependencies {
    compileOnly group: "com.github.Despical", name: "KOTL", version: "2.4.9;
}
```
</details>

## License
This code is under [GPL-3.0 License](http://www.gnu.org/licenses/gpl-3.0.html)

See the [LICENSE](https://github.com/Despical/KOTL/blob/master/LICENSE) file for required notices and attributions.

## Contributing

I accept Pull Requests via GitHub. There are some guidelines which will make applying PRs easier for me:
+ Ensure you didn't use spaces! Please use tabs for indentation.
+ Respect the code style.
+ Do not increase the version numbers in any examples files and the README.md to the new version that this Pull Request would represent.
+ Create minimal diffs - disable on save actions like reformat source code or organize imports. If you feel the source code should be reformatted create a separate PR for this change.

You can learn more about contributing via GitHub in [contribution guidelines](../CONTRIBUTING.md).

## Building from source
To build this project from source code, run the following from Git Bash:
```
git clone https://www.github.com/Despical/KOTL.git && cd KOTL
mvn clean package -Dmaven.javadoc.skip=true
```

> **Note** Don't forget to install Maven before building.
