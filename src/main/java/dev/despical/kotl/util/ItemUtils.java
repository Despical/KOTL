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

import com.destroystokyo.paper.profile.PlayerProfile;
import dev.despical.commons.XMaterial;
import dev.despical.commons.reflection.XReflection;
import dev.despical.fileitems.SpecialItem;
import dev.despical.kotl.KOTL;
import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.jetbrains.annotations.Contract;

import java.util.List;
import java.util.Locale;

/**
 * @author Despical
 * <p>
 * Created at 05.06.2026
 */
public class ItemUtils {

    public static final ItemStack[] EMPTY_ARMORS = new ItemStack[4];

    private static final KOTL PLUGIN = KOTL.getInstance();
    private static final boolean SUPPORTS_1_21_5 = XReflection.of(ItemMeta.class).method("void setHideTooltip(boolean _)").exists();

    public static void applyPlayerProfileIfSkull(OfflinePlayer player, ItemStack item) {
        if (item.getType() == XMaterial.PLAYER_HEAD.get()) {
            SkullMeta skullMeta = (SkullMeta) item.getItemMeta();
            skullMeta.setPlayerProfile(player.getPlayerProfile());

            item.setItemMeta(skullMeta);
        }
    }

    public static void applyProfileIfSkull(PlayerProfile profile, ItemStack item) {
        if (item.getType() != XMaterial.PLAYER_HEAD.get()) {
            return;
        }

        SkullMeta skullMeta = (SkullMeta) item.getItemMeta();
        skullMeta.setPlayerProfile(profile);
        item.setItemMeta(skullMeta);
    }

    @Contract(pure = true)
    public static ItemStack formatItemStack(SpecialItem specialItem, Var... vars) {
        ItemStack item = specialItem.getItemStack();
        ItemMeta meta = item.getItemMeta();

        String displayName = specialItem.getCustomKey("name");
        Component nameComponent = PLUGIN.getChatManager().parseMessage(displayName, vars);
        meta.displayName(nameComponent);

        List<String> lore = specialItem.getCustomKey("lore");
        if (lore != null) {
            meta.lore(lore.stream().map(line -> PLUGIN.getChatManager().parseMessage(line, vars)).toList());
        }

        boolean decorationOnly = SUPPORTS_1_21_5 && specialItem.getCustomKey("decoration-only") != null;
        if (decorationOnly) {
            meta.setHideTooltip(true);
        }

        item.addItemFlags(ItemFlag.values());
        item.setItemMeta(meta);
        return item;
    }

    @Contract(pure = true)
    public static String formatMaterialName(Material material) {
        String[] words = material.name().toLowerCase(Locale.ENGLISH).split("_");
        StringBuilder builder = new StringBuilder();

        for (String word : words) {
            if (!builder.isEmpty()) {
                builder.append(' ');
            }

            builder.append(Character.toUpperCase(word.charAt(0))).append(word.substring(1));
        }

        return builder.toString();
    }
}
