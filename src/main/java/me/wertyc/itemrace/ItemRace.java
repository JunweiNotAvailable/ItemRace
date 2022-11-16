package me.wertyc.itemrace;

import me.wertyc.itemrace.commands.ItemRaceCommand;
import me.wertyc.itemrace.listeners.BucketFillListener;
import me.wertyc.itemrace.listeners.InventoryClickListener;
import me.wertyc.itemrace.listeners.PlayerQuitListener;
import me.wertyc.itemrace.listeners.PlayerPickupItemListener;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scoreboard.*;

import java.util.*;

public final class ItemRace extends JavaPlugin {

    private List<Player> players = null;
    private boolean started = false;
    private Map<Player, List<Material>> playersItemList = null;

    private int startingCountDownTaskId = -1;
    private int raceTaskId = -1;

    private int duration = -1;
    private int time = -1;

    @Override
    public void onEnable() {
        Bukkit.getOnlinePlayers().forEach(player -> player.setScoreboard(Objects.requireNonNull(Bukkit.getScoreboardManager()).getNewScoreboard()));
        Objects.requireNonNull(getCommand("itemrace")).setExecutor(new ItemRaceCommand(this));
        getServer().getPluginManager().registerEvents(new PlayerPickupItemListener(this), this);
        getServer().getPluginManager().registerEvents(new InventoryClickListener(this), this);
        getServer().getPluginManager().registerEvents(new BucketFillListener(this), this);
        getServer().getPluginManager().registerEvents(new PlayerQuitListener(this), this);
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
        time = duration;
        raceTaskId = getServer().getScheduler().scheduleSyncRepeatingTask(this, () -> {
            if (counting[0]) return;
            if (time == 0) {
                cancelRaceTaskId();
                announceWinner();
                started = false;
                return;
            } else if (time == duration) {
                players.forEach(p -> p.getInventory().clear());
                players.forEach(this::createBoard);
            }
            time--;
            players.forEach(this::createBoard);
        }, 0, 20);
    }

    public void announceWinner() {
        int max = -1;
        for (Player player: players) {
            int score = playersItemList.get(player).size();
            if (score > max) {
                max = score;
            }
        }
        for (Player player: players) {
            if (playersItemList.get(player).size() == max) {
                players.forEach(p -> {
                    p.sendMessage(ChatColor.GREEN + "" + ChatColor.BOLD + player.getDisplayName() + " has won the game");
                });
            }
        }
    }

    // stop the race
    public void stop() {
        if (players == null || !started) return;
        // cancel tasks
        if (startingCountDownTaskId != -1) {
            cancelStartingTaskId();
        }
        if (raceTaskId != -1) {
            cancelRaceTaskId();
        }
        started = false;
        players.forEach(player -> player.setScoreboard(Objects.requireNonNull(Bukkit.getScoreboardManager()).getNewScoreboard()));
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
        players.forEach(this::createBoard);
    }

    // create board
    public void createBoard(Player player) {
        ScoreboardManager manager = Bukkit.getScoreboardManager();
        assert manager != null;

        Scoreboard scoreboard = manager.getNewScoreboard();
        Objective objective = scoreboard.registerNewObjective("ItemRaceScoreboard", "dummy", ChatColor.LIGHT_PURPLE + "" + ChatColor.BOLD + "Score");
        objective.setDisplaySlot(DisplaySlot.SIDEBAR);
        // sort players by score
        List<Player> sortedPlayers = new ArrayList<>();
        for (int i = 0; i < players.size(); i++) {
            Player bestPlayer = null;
            int bestScore = -1;
            for (Player p: players) {
                if (playersItemList.get(p).size() > bestScore && !sortedPlayers.contains(p)) {
                    bestPlayer = p;
                    bestScore = playersItemList.get(p).size();
                }
            }
            if (bestPlayer != null) {
                sortedPlayers.add(bestPlayer);
            }
        }
        // time left
        Score timeScore = objective.getScore(ChatColor.RED + " Time left" + ChatColor.WHITE + ": " + String.format("%02d:%02d        ", time / 60, time % 60));
        timeScore.setScore(7);
        Score blank = objective.getScore("");
        blank.setScore(6);
        // top 5 players score
        for (int i = 0; i < Math.min(sortedPlayers.size(), 5); i++) {
            String aquaColor = sortedPlayers.get(i) == player ? (ChatColor.AQUA + "" + ChatColor.BOLD) : ChatColor.AQUA + "";
            String whiteColor = sortedPlayers.get(i) == player ? (ChatColor.WHITE + ": " + ChatColor.BOLD) : ChatColor.WHITE + ": ";
            String yellowColor = sortedPlayers.get(i) == player ? (ChatColor.YELLOW + "" + ChatColor.BOLD) : ChatColor.YELLOW + "";
            Score score = objective.getScore(aquaColor + String.format("%2d. ", i + 1) +
                    yellowColor + sortedPlayers.get(i).getDisplayName() +
                    whiteColor + playersItemList.get(sortedPlayers.get(i)).size() + "  ");
            score.setScore(5 - i);
        }
        if (sortedPlayers.size() > 5 && !sortedPlayers.subList(0, 5).contains(player)) {
            Score playerScore = objective.getScore(ChatColor.AQUA + "" + ChatColor.BOLD + String.format("%2d. ", sortedPlayers.indexOf(player) + 1) +
                    ChatColor.YELLOW + player.getDisplayName() +
                    ChatColor.WHITE + ": " + ChatColor.BOLD + playersItemList.get(player).size() + "  ");
            playerScore.setScore(0);
        }
        player.setScoreboard(scoreboard);
    }

    public void removePlayer(Player player) {
        players.remove(player);
        playersItemList.remove(player);
    }
}
