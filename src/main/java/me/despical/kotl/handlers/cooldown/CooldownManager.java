package me.despical.kotl.handlers.cooldown;

import me.despical.kotl.Main;
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

	private double cooldownCounter = 0;

	private final List<Cooldown> cooldowns;

	public CooldownManager(Main plugin) {
		this.cooldowns = new ArrayList<>();

		plugin.getServer().getScheduler().runTaskTimerAsynchronously(plugin, () -> cooldownCounter += .5, 20, 10);
	}

	public void setCooldown(User user, String name, double seconds) {
		if (seconds == 0) {
			var cooldownOpt = cooldowns.stream().filter(cooldown -> cooldown.uuid.equals(user.getUniqueId()) && cooldown.name.equals(name)).findFirst();

			cooldownOpt.ifPresent(cooldowns::remove);
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

		return 0;
	}

	private record Cooldown(UUID uuid, String name, double seconds) {
	}
}