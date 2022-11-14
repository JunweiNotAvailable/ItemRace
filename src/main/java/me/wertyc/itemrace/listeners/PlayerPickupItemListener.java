package me.wertyc.itemrace.listeners;

import me.wertyc.itemrace.ItemRace;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityPickupItemEvent;

public class PlayerPickupItemListener implements Listener {
    private final ItemRace plugin;

    public PlayerPickupItemListener(ItemRace plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerPickupItem(EntityPickupItemEvent event) {
        if (!plugin.isStarted() || !(event.getEntity() instanceof Player)) return;

        Player player = (Player) event.getEntity();
        plugin.pickedUpItem(player, event.getItem().getItemStack().getType());
    }
}
