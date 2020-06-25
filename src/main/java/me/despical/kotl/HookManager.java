package me.despical.kotl;

import java.util.EnumMap;
import java.util.Map;
import java.util.logging.Level;

import org.bukkit.Bukkit;

import me.despical.kotl.utils.Debugger;

/**
 * @author Despical
 * <p>
 * Created at 20.06.2020
 */
public class HookManager {

	private Map<HookFeature, Boolean> hooks = new EnumMap<>(HookFeature.class);

	public HookManager() {
		enableHooks();
	}

	private void enableHooks() {
		for (HookFeature feature : HookFeature.values()) {
			boolean hooked = true;
			for (Hook requiredHook : feature.getRequiredHooks()) {
				if (!Bukkit.getPluginManager().isPluginEnabled(requiredHook.getPluginName())) {
					hooks.put(feature, false);
					Debugger.debug(Level.INFO, "Feature {0} won't be enabled because {1} is not installed! Please install it in order to enable this feature in-game!",
						feature.name(), requiredHook.getPluginName());
					hooked = false;
					break;
				}
			}
			if (hooked) {
				hooks.put(feature, true);
				Debugger.debug(Level.INFO, "Feature {0} enabled!", feature.name());
			}
		}
	}

	public boolean isFeatureEnabled(HookFeature feature) {
		return hooks.get(feature);
	}
	
	public enum HookFeature {
		HOLOGRAPHIC_DISPLAYS(Hook.HOLOGRAPHIC_DISPLAYS);

		private Hook[] requiredHooks;

		HookFeature(Hook... requiredHooks) {
			this.requiredHooks = requiredHooks;
		}

		public Hook[] getRequiredHooks() {
			return requiredHooks;
		}
	}

	public enum Hook {
		HOLOGRAPHIC_DISPLAYS("HolographicDisplays");

		private String pluginName;

		Hook(String pluginName) {
			this.pluginName = pluginName;
		}

		public String getPluginName() {
			return pluginName;
		}
	}
}