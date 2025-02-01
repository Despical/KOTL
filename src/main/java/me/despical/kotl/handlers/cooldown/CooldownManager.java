package me.despical.kotl.handlers.cooldown;

import me.despical.kotl.KOTL;
import me.despical.kotl.user.User;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * @author Despical
 * <p>
 * Created at 30.04.2024
 */
public class CooldownManager {

    private final List<Cooldown> cooldowns;
    private double cooldownCounter = 0;

    public CooldownManager(KOTL plugin) {
        this.cooldowns = new ArrayList<>();

        plugin.getServer().getScheduler().runTaskTimerAsynchronously(plugin, () -> cooldownCounter += .5, 20, 10);
    }

    public void setCooldown(User user, String name, double seconds) {
        var cooldownOpt = cooldowns.stream().filter(cooldown -> cooldown.uuid.equals(user.getUniqueId()) && cooldown.name.equals(name)).findFirst();

        if (seconds == 0) {
            cooldownOpt.ifPresent(cooldowns::remove);
            return;
        }

        if (cooldownOpt.isPresent() && cooldowns.contains(cooldownOpt.get())) {
            cooldownOpt.get().seconds = seconds + cooldownCounter;
            return;
        }

        cooldowns.add(new Cooldown(user.getUniqueId(), name, seconds + cooldownCounter));
    }

    public double getCooldown(User user, String name) {
        var cooldownOptional = cooldowns.stream().filter(cooldown -> cooldown.uuid.equals(user.getUniqueId()) && cooldown.name.equals(name)).findFirst();

        if (cooldownOptional.isEmpty() || cooldownOptional.get().seconds <= cooldownCounter) return 0;

        if (cooldownOptional.get().seconds < cooldownCounter) {
            cooldowns.remove(cooldownOptional.get());
            return 0;
        }

        return cooldownOptional.get().seconds - cooldownCounter;
    }

    private static class Cooldown {

        final UUID uuid;
        final String name;
        double seconds;

        Cooldown(UUID uuid, String name, double seconds) {
            this.uuid = uuid;
            this.name = name;
            this.seconds = seconds;
        }
    }
}
