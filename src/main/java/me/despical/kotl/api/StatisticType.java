package me.despical.kotl.api;

import me.despical.kotl.user.User;

/**
 * @author Despical
 * <p>
 * Created at 5.05.2025
 */
public enum StatisticType {

    TOURS_PLAYED("toursplayed"),
    SCORE("score"),
    KILLS("kill"),
    DEATHS("death"),
    LOCAL_RESET_COOLDOWN("local_reset_cooldown", false);

    private static final StatisticType[] PERSISTENT_STATS = {TOURS_PLAYED, SCORE, KILLS, DEATHS};

    private final String name;
    private final boolean persistent;

    StatisticType(String name) {
        this(name, true);
    }

    StatisticType(String name, boolean persistent) {
        this.name = name;
        this.persistent = persistent;
    }

    public String getName() {
        return name;
    }

    public boolean isPersistent() {
        return persistent;
    }

    public String from(User user) {
        return Integer.toString(user.getStat(this));
    }

    public static StatisticType[] getPersistentStats() {
        return PERSISTENT_STATS;
    }
}
