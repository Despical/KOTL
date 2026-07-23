package dev.despical.kotl.scoreboard.formatter;

import dev.despical.commons.string.StringFormatUtils;
import dev.despical.kotl.arena.Arena;
import dev.despical.kotl.arena.options.ArenaKeys;
import dev.despical.kotl.stats.Statistics;
import dev.despical.kotl.util.Utils;
import dev.despical.kotl.util.Var;

public final class GlobalFormatter {

    private static final String date = StringFormatUtils.formatToday();

    private GlobalFormatter() {
    }

    public static final LineFormatter INSTANCE = (user, game, line) -> {
        Arena arena = game.getArena();

        Var[] vars = {
            Var.ofPlayer(user),
            Var.of("%date%", date),
            Var.of("%arena%", arena.getId()),
            Var.of("%players%", game.getPlayers().size()),
            Var.of("%king%", arena.getOption(ArenaKeys.KING) == null ? "None" : arena.getOption(ArenaKeys.KING)),
            Var.of("%last_king%", arena.getOption(ArenaKeys.LAST_KING)),
            Var.of("%top_king%", arena.getOption(ArenaKeys.TOP_KING)),
            Var.of("%top_king_score%", arena.getOption(ArenaKeys.TOP_KING_SCORE)),
            Var.of("%score%", user.getStatistic(Statistics.SCORE)),
            Var.of("%arena_score%", user.getArenaScore(arena.getId())),
            Var.of("%tours_played%", user.getStatistic(Statistics.TOURS_PLAYED)),
            Var.of("%kills%", user.getStatistic(Statistics.KILL)),
            Var.of("%deaths%", user.getStatistic(Statistics.DEATH))
        };

        return Utils.format(line, vars);
    };
}
