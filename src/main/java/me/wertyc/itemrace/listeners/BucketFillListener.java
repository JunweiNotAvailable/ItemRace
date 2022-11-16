package me.wertyc.itemrace.listeners;

import me.wertyc.itemrace.ItemRace;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerBucketFillEvent;

public class BucketFillListener implements Listener {
    private final ItemRace plugin;

    public BucketFillListener(ItemRace plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onBucketFill(PlayerBucketFillEvent event) {
        if (!plugin.isStarted()) return;

        Player player = event.getPlayer();
        plugin.pickedUpItem(player, event.getBlockClicked().getType());
    }
}
