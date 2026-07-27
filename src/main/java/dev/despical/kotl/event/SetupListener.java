package dev.despical.kotl.event;

import dev.despical.kotl.arena.Arena;
import dev.despical.kotl.setup.pages.SetupHomePage;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

public final class SetupListener extends ListenerAdapter {

    @EventHandler
    public void onKingPlatePlace(BlockPlaceEvent event) {
        if (arenaRegistry.isInArena(event.getPlayer())) {
            return;
        }

        ItemStack item = event.getItemInHand();
        ItemMeta meta = item.getItemMeta();

        if (meta == null) {
            return;
        }

        var persistentDataContainer = meta.getPersistentDataContainer();
        String arenaId = persistentDataContainer.get(SetupHomePage.KING_PLATE_TOOL_KEY, PersistentDataType.STRING);

        if (arenaId == null) {
            return;
        }

        Arena arena = arenaRegistry.getArena(arenaId);
        if (arena == null) {
            return;
        }

        SetupHomePage.setKingPlate(event.getPlayer(), arena, event.getBlockPlaced().getLocation());
    }
}
