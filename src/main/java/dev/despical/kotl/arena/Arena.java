package dev.despical.kotl.arena;

import dev.despical.kotl.arena.options.ArenaOption;
import dev.despical.kotl.game.Game;
import lombok.Getter;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Despical
 * <p>
 * Created at 20.06.2020
 */
@Getter
public class Arena {

    private final String id;
    private final Game game;
    private final Map<ArenaOption<?>, Object> options;

    public Arena(String id) {
        this.id = id;
        this.options = new HashMap<>();
        this.game = new Game(this);
    }

    public <T> T getOption(ArenaOption<T> option) {
        Object value = options.computeIfAbsent(option, ArenaOption::getDefaultValue);

        try {
            return option.getType().cast(value);
        } catch (ClassCastException e) {
            return option.getDefaultValue();
        }
    }

    public <T> void setOption(ArenaOption<T> option, T value) {
        options.put(option, value);
    }

    public boolean isOptionPresent(ArenaOption<?> option) {
        return options.containsKey(option);
    }
}
