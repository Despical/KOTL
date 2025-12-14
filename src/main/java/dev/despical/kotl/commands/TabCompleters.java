package dev.despical.kotl.commands;

import dev.despical.commandframework.CommandArguments;
import dev.despical.commandframework.CompleterHelper;
import dev.despical.commandframework.annotations.Completer;
import dev.despical.kotl.KOTL;

import java.util.List;

/**
 * @author Despical
 * <p>
 * Created at 14.12.2025
 */
public class TabCompleters {

    private final KOTL plugin;

    public TabCompleters(KOTL kotl) {
        this.plugin = kotl;
        this.plugin.getCommandFramework().registerCommands(this);
    }

    @Completer(
        name = "kotl"
    )
    public List<String> onTabComplete(CommandArguments arguments, CompleterHelper helper) {
        int length = arguments.getLength();

        if (length > 0) {
            if (helper.equalsAny(0, "create", "list", "help", "reload", "version")) {
                return helper.empty();
            }
        }

        if (length == 1) {
            List<String> commands = plugin.getCommandFramework().getSubCommands().stream()
                .map(cmd -> cmd.name().replace(arguments.getLabel() + '.', ""))
                .toList();
            return helper.copyMatches(0, arguments.hasPermission("kotl.admin") ? commands : List.of("top", "stats"));
        }

        if (length == 2) {
            if (!helper.equalsAny(0, "delete", "edit", "help", "kick", "stats", "top")) {
                return helper.empty();
            }

            if (helper.equalsAny(0, "top")) {
                return helper.copyMatches(1, List.of("tours_played", "score", "kills", "deaths"));
            }

            if (helper.equalsAny(0, "stats", "kick")) {
                return helper.playerNames();
            }

            return helper.copyMatches(1, plugin.getArenaRegistry().getArenaNames());
        }

        return helper.empty();
    }
}
