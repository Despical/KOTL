/*
 * KOTL - Don't let others to climb top of the ladders!
 * Copyright (C) 2023 Despical
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

package me.despical.kotl.handler.rewards;

import me.despical.commons.util.LogUtils;
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
			final int loc = processedCode.indexOf(")");

			if (loc == -1) {
				LogUtils.sendConsoleMessage("&cRewards configuration is broken! Make sure you don't forget using ')' character in chance condition! Command: " + rawCode);
				this.chance = 101;
				return;
			}

			String chanceStr = processedCode;
			chanceStr = chanceStr.substring(0, loc).replaceAll("[^0-9]+", "");

			processedCode = StringUtils.replace(processedCode, "chance(" + chanceStr + "):", "");
			this.chance = Double.parseDouble(chanceStr);
		} else {
			this.chance = 100;
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
		WIN, LOSE;

		public String getPath() {
			return "rewards." + name().toLowerCase();
		}
	}

	public enum RewardExecutor {
		CONSOLE, PLAYER, SCRIPT
	}
}