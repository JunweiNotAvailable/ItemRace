package me.wertyc.itemrace.listeners;

import me.wertyc.itemrace.ItemRace;
import org.bukkit.Instrument;
import org.bukkit.Note;
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
        ItemStack clickedItem = event.getCurrentItem();
        if (clickedItem == null || clickedItem.getItemMeta() == null) return;
        String itemName = clickedItem.getItemMeta().getDisplayName();
        Player player = (Player) event.getWhoClicked();
        // duration
        if (event.getView().getTitle().equals("Duration") && itemName.endsWith("minutes")) {
            // close inventory
            player.playNote(player.getLocation(), Instrument.PLING, Note.flat(1, Note.Tone.A));
            player.closeInventory();
            plugin.setDuration(Integer.parseInt(itemName.substring(0, itemName.length() - 8)) * 60);
            plugin.stop();
            plugin.start();
            event.setCancelled(true);
            return;
        }
        // click items
        if (plugin.isStarted()) {
            int clickedSlot = event.getHotbarButton();
            // if click using key and the slot is empty
            if (clickedSlot == -1 || player.getInventory().getItem(clickedSlot) == null) {
                plugin.pickedUpItem(player, clickedItem.getType());
            }
        }
    }
}
