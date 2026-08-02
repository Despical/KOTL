/*
 * KOTL - Don't let others climb to top of the ladders!
 * Copyright (C) 2026  Berke Akçen
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package dev.despical.kotl.util;

import dev.despical.commons.serializer.InventorySerializer;
import dev.despical.kotl.KOTL;
import org.bukkit.entity.Player;

import java.io.File;

/**
 * Owns the durable inventory snapshots used while a player is in an arena.
 *
 * @author Despical
 * <p>
 * Created at 02.08.2026
 */
public final class PlayerInventoryManager {

    private final KOTL plugin;
    private final File snapshotDirectory;

    public PlayerInventoryManager(KOTL plugin) {
        this.plugin = plugin;
        this.snapshotDirectory = new File(plugin.getDataFolder(), "inventories");
    }

    public boolean save(Player player) {
        return InventorySerializer.saveInventoryToFile(plugin, player)
            && hasSnapshot(player);
    }

    public boolean hasSnapshot(Player player) {
        File snapshot = snapshotFile(player);
        return snapshot.isFile() && snapshot.length() > 0;
    }

    public boolean restore(Player player) {
        if (!hasSnapshot(player)) {
            return false;
        }

        player.getActivePotionEffects().forEach(effect -> player.removePotionEffect(effect.getType()));
        InventorySerializer.loadInventory(plugin, player);

        return snapshotFile(player).delete();
    }

    private File snapshotFile(Player player) {
        return new File(snapshotDirectory, player.getUniqueId() + ".inventory");
    }
}
