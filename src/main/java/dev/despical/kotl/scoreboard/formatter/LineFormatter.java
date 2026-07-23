package dev.despical.kotl.scoreboard.formatter;

import dev.despical.kotl.game.Game;
import dev.despical.kotl.user.User;

@FunctionalInterface
public interface LineFormatter {

    String format(User user, Game game, String line);
}
