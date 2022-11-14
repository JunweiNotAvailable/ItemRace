package me.wertyc.itemrace.listeners;

import me.wertyc.itemrace.ItemRace;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

import java.util.Objects;

public class InventoryClickListener implements Listener {
    private final ItemRace plugin;

    public InventoryClickListener(ItemRace plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!event.getView().getTitle().equals("Duration")) return;

        ItemStack clickedItem = event.getCurrentItem();
        if (clickedItem == null) return;
        String itemName = Objects.requireNonNull(clickedItem.getItemMeta()).getDisplayName();
        Player player = (Player) event.getWhoClicked();
        // duration
        if (itemName.endsWith("minutes")) {
            // close inventory
            player.closeInventory();
            plugin.setDuration(Integer.parseInt(itemName.substring(0, itemName.length() - 8)) * 60);
            plugin.stop();
            plugin.start();
            event.setCancelled(true);
        }
    }
}
