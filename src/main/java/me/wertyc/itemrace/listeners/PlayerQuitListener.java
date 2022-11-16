package me.wertyc.itemrace.listeners;

import me.wertyc.itemrace.ItemRace;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;

public class PlayerQuitListener implements Listener {
    private final ItemRace plugin;

    public PlayerQuitListener(ItemRace plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        if (plugin.isStarted()) {
            plugin.removePlayer(event.getPlayer());
        }
    }
}
