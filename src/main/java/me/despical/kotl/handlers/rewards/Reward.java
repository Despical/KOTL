/*
 * KOTL - Don't let others to climb top of the ladders!
 * Copyright (C) 2021 Despical and contributors
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */

package me.despical.kotl.handlers.rewards;

import me.despical.kotl.utils.Debugger;
import org.apache.commons.lang.StringUtils;

/**
 * @author Despical
 * <p>
 * Created at 30.06.2020
 */
public class Reward {

	private String executableCode;

	private final RewardType type;
	private final RewardExecutor executor;
	private final double chance;

	public Reward(RewardType type, String rawCode) {
		this.type = type;
		String processedCode = rawCode;

		if (rawCode.contains("p:")) {
			this.executor = RewardExecutor.PLAYER;
			processedCode = StringUtils.replace(processedCode, "p:", "");
		} else if (rawCode.contains("script:")) {
			this.executor = RewardExecutor.SCRIPT;
			processedCode = StringUtils.replace(processedCode, "script:", "");
		} else {
			this.executor = RewardExecutor.CONSOLE;
		}

		if (processedCode.contains("chance(")) {
			int loc = processedCode.indexOf(")");

			if (loc == -1) {
				Debugger.sendConsoleMessage("&cRewards configuration is broken! Make sure you don't forget using ')' character in chance condition! Command: " + rawCode);
				this.chance = 101d;
				return;
			}

			String chanceStr = processedCode;
			chanceStr = chanceStr.substring(0, loc).replaceAll("[^0-9]+", "");

			processedCode = StringUtils.replace(processedCode, "chance(" + chanceStr + "):", "");
			this.chance = Double.parseDouble(chanceStr);
		} else {
			this.chance = 100.0;
		}

		this.executableCode = processedCode;
	}

	public RewardExecutor getExecutor() {
		return executor;
	}

	public String getExecutableCode() {
		return executableCode;
	}

	public double getChance() {
		return chance;
	}

	public RewardType getType() {
		return type;
	}

	public enum RewardType {
		WIN("win"), LOSE("lose");

		private final String path;

		RewardType(String path) {
			this.path = path;
		}

		public String getPath() {
			return "rewards." + path;
		}
	}

	public enum RewardExecutor {
		CONSOLE, PLAYER, SCRIPT
	}
}