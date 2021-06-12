# King of the Ladder
[![](https://jitpack.io/v/Despical/KOTL.svg)](https://jitpack.io/#Despical/KOTL)
[![](https://img.shields.io/badge/JavaDocs-latest-lime.svg)](https://javadoc.jitpack.io/com/github/Despical/KOTL/latest/javadoc/index.html)
[![discord](https://img.shields.io/discord/719922452259668000.svg?color=lime&label=Discord)](https://discord.gg/Vhyy4HA)
![GitHub Workflow Status](https://img.shields.io/github/workflow/status/Despical/KOTL/KOTL%20Build)

King of the Ladder is an old Minecraft mini game. Each player have to kick other players out of the ladders
to be the king. The king tries to stand on the top of the ladders while not letting others to climb and punching
them out of the way!

## Documentation
More information can be found on the [wiki page](https://github.com/Despical/KOTL/wiki). The [Javadoc](https://javadoc.jitpack.io/com/github/Despical/KOTL/latest/javadoc/index.html) can be browsed. Questions
related to the usage of King of the Ladder should be posted on my [Discord server](https://discord.com/invite/Vhyy4HA).

## Using King of the Ladder API
The project isn't in the Central Repository yet, so specifying a repository is needed.<br>
To add this project as a dependency to your project, add the following to your pom.xml:

### Maven dependency

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
    <version>1.2.1</version>
    <scope>compile</scope>
</dependency>
```

### Gradle dependency
```
repositories {
    maven { url 'https://jitpack.io' }
}
```
```
dependencies {
    compileOnly group: "com.github.Despical", name: "KOTL", version: "1.2.1";
}
```

## License
This code is under [GPL-3.0 License](http://www.gnu.org/licenses/gpl-3.0.html)

See the [LICENSE](https://github.com/Despical/KOTL/blob/master/LICENSE) file for required notices and attributions.

## Donations
You like the KOTL? Then [donate](https://www.patreon.com/despical) back me to support the development.

## Contributing

I accept Pull Requests via GitHub. There are some guidelines which will make applying PRs easier for me:
+ Ensure you didn't use spaces! Please use tabs for indentation.
+ Respect the code style.
+ Do not increase the version numbers in any examples files and the README.md to the new version that this Pull Request would represent.
+ Create minimal diffs - disable on save actions like reformat source code or organize imports. If you feel the source code should be reformatted create a separate PR for this change.

You can learn more about contributing via GitHub in [contribution guidelines](CONTRIBUTING.md).

## Building from source
If you want to build this project from source code, run the following from Git Bash:
```
git clone https://www.github.com/Despical/KOTL.git && cd KOTL
mvn clean package
```
Also don't forget to install Maven before building.