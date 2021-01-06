### 1.2.0 Release (02.01.2021 - 06.01.2021)
* Added multi-world support.
* Fixed winner also gets the losing reward.
* Fixed default null king entry on scoreboard.
* Removed `kotl.command.override` permission (still be able to use `kotl.admin` instead).
* Removed ChatManager argument from sub-commands.,
* Removed `Main#isBefore1_9_R1` method.
* Changed filled-map item in setup gui to tutorial video from wiki link.
* Changed PAPI's plugin version with Bukkit's.
* Changed some debugger keys.
* Now list command replaces values faster.
* Disabled locales in debug mode.
* Updated license header.

### 1.1.9 Release (22.12.2020)
* Fixed scoreboard issues.
* Tab complete improvement.
* Some additional optimizations.

### 1.1.8 Release (19.11.2020)
* Added online player completion to tab complete for stats command.
* Fixed restoring potion effects while ``InventoryManager`` option is enabled.
* Fixed some JavaDoc issues.
* Fixed possible NPE.
* Removed default false values for config options.
* Replaced some attribute methods with API methods.
* Changed package of a class - (Developer Alert)
  * me.despical.kotl.commands.admin.arena.CreateCommand
* General code optimization.

### 1.1.7 Release (05.11.2020 - 07.11.2020)
* Added Minecraft 1.16.4 compatibility.
* Added new language system.
* Added contributors section to pom.xml
* Added new bStats chart to get which locale is in use.
* Fixed license header.
* Fixed scoreboard not removing when player leave the game.
* Fixed a bug related to armors when restoring inventories.
* Fixed a MySQL syntax.
* Fixed a bug about restoring inventories.
* Moved script engine to switch case.
* Now ``Clear-Inventory`` option is false by default.
* Now sending arena creation message centered.
* Performance improvements.

### 1.1.6 Release (25.10.2020 - 30.10.2020)
* Fixed scoreboard issues.
* Fixed event issues.

**Also note that this update includes 1.1.6 beta release changes.**

### 1.1.6 Beta Release (25.10.2020)
* Fixed anyone can't execute commands.
* Added .editorconfig file for developers.

### 1.1.5 Release (14.10.2020)
* Fixed a bug about being king more than once.
* Replaced some attribute methods with API methods.
* Optimized sub command fields and commands.
* Performance optimizations and improvements.

### 1.1.4 Release (07.10.2020)
* Added license header.
* Added more in-game tips.
* Fixed custom armor stand holograms.
* Fixed getting unsupported exception.
* Fixed debugger sends prefix twice.
* Fixed item lores for Minecraft 1.13 and higher versions.
* Fixed bStats metrics.
* Made callback safe hologram deletion.
* Improved GUI performances.

### 1.1.3 Release (29.09.2020 - 04.10.2020)
* Added custom armor stand holograms.
* Added missing 1.14, 1.15 and 1.16 items.
* Added 1.8 compatibility.
* Added support for 1.16 hex colors.
* Added separate chat.
* Now HolographicDisplays is not a dependency.
* Fixed sending prefix twice.
* Removed unnecessary command exceptions.
* Changed MySQL updates to do only one instead of more than nearly 15.
* Changed a package name - (Developer Alert)
   * me.despical.kotl.handler -> me.despical.kotl.handlers
* Reworked on update checker.
* Reworked on debugger.
* Reworked on message utils.
* Reworked on tab completion.
* Reworked on stats saving.
* So many improvements for new versions of Java.
* Made code more readable.

**Also note that this update includes 1.1.3 beta release changes.**

### 1.1.3b Beta Release (14.09.2020)
* Fixed Mysql stats table is not creating.

### 1.1.2 Release (11.09.2020)
* Added option to change delay between hits.
* Added option clear effects on joining arena.
* Added Minecraft 1.16.3 compatibility.
* Added more in-game tips.
* Improved arena creation messages.
* Some performance improvements and bug fixes.
* Updated dependencies to latest versions.
* Removed unused functions and imports.

### 1.1.1 Release (05.09.2020)
* Fixed GUI issues.

### 1.1.1 Pre-Release (30.08.2020)
* Generated JavaDocs.

### 1.1.0 (24.08.2020)
* Added Minecraft 1.16.2 compatibility.

### 1.0.9 (22.08.2020)
* Added new option to disable inventory cleaning.

### 1.0.8 Release (18.08.2020)
* Added new message options.
* Removed Kotlin compatibility.
* Some performance improvements.

### 1.0.7 Release (31.07.2020)
* Fixed scoreboard wasn't be removed after deleting arena.
* Fixed PAPI variable for king name.
* Kotlin compatibility.

### 1.0.6 Release (27.06.2020)
* Fixed some statistic case sensitive names.
* Added new Mysql options.
* Improved arena editor.
* Some performance improvements.

### 1.0.5 Release (06.07.2020)
* Huge performance update.

**Also note that no game behavior has changed.**

### 1.0.4 Release (30.06.2020)
* Fixed scoreboard in 1.8.9 and older versions.
* Fixed a message bug with top 10 players command.
* Added rewards system.
* Added death blocks.
* Improved saving statistics.

### 1.0.3 Release (29.06.2020)
* Added scoreboard to in-game.
* Added new option to config.yml
* Added new messages to messages.yml

### 1.0.2 Release (28.06.2020)
* Added new message options.
* Added new version supports.
* Improved the arena setup menu.
* Fixed broken update checker.
* Fixed a bug about statistics.

### 1.0.1 Release (26.06.2020)
* Fixed broken messages in arena editor.
* Fixed broken permissions.
* Added wiki page, check it [here](https://github.com/Despical/KOTL/wiki).
* Update checker activated.
