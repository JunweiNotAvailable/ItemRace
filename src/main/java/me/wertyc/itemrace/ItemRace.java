package me.wertyc.itemrace;

import me.wertyc.itemrace.commands.ItemRaceCommand;
import me.wertyc.itemrace.listeners.InventoryClickListener;
import me.wertyc.itemrace.listeners.PlayerPickupItemListener;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.*;

public final class ItemRace extends JavaPlugin {

    private List<Player> players = null;
    private boolean started = false;
    private Map<Player, List<Material>> playersItemList = null;

    private int startingCountDownTaskId = -1;
    private int raceTaskId = -1;

    private int duration = -1;

    @Override
    public void onEnable() {
        Objects.requireNonNull(getCommand("itemrace")).setExecutor(new ItemRaceCommand(this));
        getServer().getPluginManager().registerEvents(new PlayerPickupItemListener(this), this);
        getServer().getPluginManager().registerEvents(new InventoryClickListener(this), this);
    }

    public boolean isStarted() {
        return started;
    }

    public void setDuration(int duration) {
        this.duration = duration;
    }

    // open duration menu
    public void openMenu(Player player) {
        Inventory menu = Bukkit.createInventory(player, 9, "Duration");
        List<ItemStack> items = new ArrayList<>();
        for (int i = 5; i <= 20; i += 5) {
            ItemStack clock = new ItemStack(Material.CLOCK);
            ItemMeta clockMeta = clock.getItemMeta();
            assert clockMeta != null;
            clockMeta.setDisplayName(i + " minutes");
            clock.setItemMeta(clockMeta);
            items.add(clock);
        }
        menu.setContents(items.toArray(new ItemStack[0]));
        player.openInventory(menu);
    }

    // start the race
    public void start() {
        reset();
        // starting count down
        int[] startingSec = {5};
        boolean[] counting = {true};
        startingCountDownTaskId = getServer().getScheduler().scheduleSyncRepeatingTask(this, () -> {
            if (startingSec[0] == 0) {
                cancelStartingTaskId();
                players.forEach(player -> player.sendMessage(ChatColor.GOLD + "" + ChatColor.BOLD + "Game started"));
                counting[0] = false;
                started = true;
                return;
            }
            players.forEach(player -> player.sendMessage(ChatColor.GOLD + "" + ChatColor.BOLD + "Game starting in " + startingSec[0] + ((startingSec[0] == 1) ? " second" : " seconds")));
            startingSec[0]--;
        }, 10, 20);

        // start the race
        int[] timeLeft = {duration};
        raceTaskId = getServer().getScheduler().scheduleSyncRepeatingTask(this, () -> {
            if (counting[0]) return;
            if (timeLeft[0] == 0) {
                cancelRaceTaskId();
                announceWinner();
                started = false;
                return;
            }
            if (timeLeft[0] % 60 == 0) {
                players.forEach(player -> player.sendMessage(ChatColor.GREEN + ""+ (timeLeft[0] / 60) + ((timeLeft[0] == 60) ? " minute left" : " minutes left")));
            } else if (timeLeft[0] < 10 || timeLeft[0] == 30) {
                players.forEach(player -> player.sendMessage(ChatColor.GREEN + "" + (timeLeft[0]) + (timeLeft[0] == 1 ? " second " : " seconds ") + "left"));
            }
            timeLeft[0]--;
        }, 0, 20);
    }

    public void announceWinner() {
        Player winner = null;
        int max = -1;
        for (Player player: players) {
            int score = playersItemList.get(player).size();
            if (score > max) {
                max = score;
                winner = player;
            }
        }
        Player finalWinner = winner;
        players.forEach(player -> {
            assert finalWinner != null;
            player.sendMessage(ChatColor.GREEN + "" + ChatColor.BOLD + finalWinner.getDisplayName() + " has won the game");
        });
    }

    // stop the race
    public void stop() {
        if (players == null) return;
        if (!started) {
            players.forEach(player -> player.sendMessage(ChatColor.GOLD + "" + ChatColor.BOLD + "No game to be stopped"));
            return;
        }
        // cancel tasks
        if (startingCountDownTaskId != -1) {
            cancelStartingTaskId();
        }
        if (raceTaskId != -1) {
            cancelRaceTaskId();
        }
        started = false;
        players.forEach(player -> player.sendMessage(ChatColor.GOLD + "" + ChatColor.BOLD + "Game has been stopped"));
    }

    // reset
    public void reset() {
        // reset players
        players = new ArrayList<>();
        players.addAll(Bukkit.getOnlinePlayers());
        // reset player's item list
        playersItemList = new HashMap<>();
        for (Player player: players) {
            player.getInventory().clear();
            playersItemList.put(player, new ArrayList<>());
        }
        // reset task ids
        startingCountDownTaskId = -1;
        raceTaskId = -1;
    }

    private void cancelStartingTaskId() {
        getServer().getScheduler().cancelTask(startingCountDownTaskId);
        startingCountDownTaskId = -1;
    }

    private void cancelRaceTaskId() {
        getServer().getScheduler().cancelTask(raceTaskId);
        raceTaskId = -1;
    }

    // call when a player picked up items
    public void pickedUpItem(Player player, Material material) {
        List<Material> itemList = playersItemList.get(player);
        if (itemList.contains(material)) return;
        itemList.add(material);
        players.forEach(p -> p.sendMessage(ChatColor.GREEN + player.getDisplayName() + " picked up " + itemList.size() + " items"));
    }
}
