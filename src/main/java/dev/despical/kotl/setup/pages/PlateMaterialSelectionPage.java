package dev.despical.kotl.setup.pages;

import dev.despical.fileitems.SpecialItem;
import dev.despical.inventoryframework.Gui;
import dev.despical.inventoryframework.GuiItem;
import dev.despical.inventoryframework.pane.PaginatedPane;
import dev.despical.inventoryframework.pane.StaticPane;
import dev.despical.kotl.arena.options.ArenaKeys;
import dev.despical.kotl.setup.SetupMenu;
import dev.despical.kotl.setup.SetupPage;
import dev.despical.kotl.util.ItemUtils;
import dev.despical.kotl.util.Var;
import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class PlateMaterialSelectionPage extends SetupPage {

    public PlateMaterialSelectionPage(SetupMenu menu) {
        super(menu);
    }

    @Override
    public void beforeOpening(Gui gui) {
        gui.setRows(5);
    }

    @Override
    public void injectItems(PaginatedPane paginatedPane) {
        StaticPane pane = new StaticPane(9, 5);
        paginatedPane.addPane(0, pane);

        SpecialItem template = itemManager.getItem("pressure-plate-material");
        List<Material> materials = getConfiguredMaterials(template);
        List<Integer> slots = template.getCustomKey("slots");

        for (int i = 0; i < materials.size() && i < slots.size(); i++) {
            pane.addItem(createPlateItem(template, materials.get(i)), slots.get(i));
        }

        pane.addItem(createGoBackItem(), template.getCustomKey("back-slot"));
    }

    private List<Material> getConfiguredMaterials(SpecialItem template) {
        List<String> configuredMaterials = template.getCustomKey("materials");
        List<Material> materials = new ArrayList<>();

        for (String configuredMaterial : configuredMaterials) {
            materials.add(Material.getMaterial(configuredMaterial.toUpperCase(Locale.ENGLISH)));
        }

        return materials;
    }

    private GuiItem createPlateItem(SpecialItem template, Material material) {
        ItemStack item = template.getItemStack().clone();
        item.setType(material);

        boolean selected = arena.getOption(ArenaKeys.ARENA_PLATE) == material;
        String status = template.getCustomKey(selected ? "selected-status" : "unselected-status");
        String displayName = ItemUtils.formatMaterialName(material);

        ItemMeta meta = item.getItemMeta();

        if (meta != null) {
            meta.displayName(chatManager.parseMessage(template.getCustomKey("name"),
                Var.of("%material%", displayName),
                Var.of("%status%", status)
            ));

            List<String> lore = template.getCustomKey("lore");

            if (lore != null) {
                List<Component> components = lore.stream()
                    .map(line -> chatManager.parseMessage(line,
                        Var.of("%material%", displayName),
                        Var.of("%status%", status)
                    ))
                    .toList();
                meta.lore(components);
            }

            item.setItemMeta(meta);
        }

        return GuiItem.of(item, event -> {
            Player player = (Player) event.getWhoClicked();
            arena.setOption(ArenaKeys.ARENA_PLATE, material);

            player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 1f, 1.8f);
            chatManager.sendMessage(player, "setup.king-plate-material-selected", Var.of("%material%", displayName));
            menu.openPlateMaterialSelection();
        });
    }
}
