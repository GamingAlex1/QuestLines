package QuestLines.LineQuests;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashMap;
import java.util.UUID;

public final class QuestLines extends JavaPlugin implements Listener {

    private HashMap<UUID, Integer> playerStoneCount = new HashMap<>();
    private HashMap<UUID, BossBar> playerBossBars = new HashMap<>();
    private HashMap<UUID, Long> lastBlockBreakTime = new HashMap<>();

    @Override
    public void onEnable() {
        // Plugin startup logic
        Bukkit.getLogger().info("Kys simulator enabled!");
        getServer().getPluginManager().registerEvents(this, this);

        // Schedule a repeating task to check for boss bar removal
        new BukkitRunnable() {
            @Override
            public void run() {
                long currentTime = System.currentTimeMillis();
                for (UUID playerUUID : lastBlockBreakTime.keySet()) {
                    long lastBreakTime = lastBlockBreakTime.get(playerUUID);
                    if (currentTime - lastBreakTime > 5000) { // 5 seconds
                        BossBar bossBar = playerBossBars.remove(playerUUID);
                        if (bossBar != null) {
                            bossBar.removeAll();
                        }
                    }
                }
            }
        }.runTaskTimer(this, 0L, 20L); // Run every second (20 ticks)
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
        Bukkit.getLogger().info("Goodbye world!");
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        Player player = event.getPlayer();
        UUID playerUUID = player.getUniqueId();

        if (event.getBlock().getType() == Material.STONE) {
            int count = playerStoneCount.getOrDefault(playerUUID, 0) + 1;
            playerStoneCount.put(playerUUID, count);
            lastBlockBreakTime.put(playerUUID, System.currentTimeMillis());

            BossBar bossBar = playerBossBars.get(playerUUID);
            if (bossBar == null) {
                bossBar = Bukkit.createBossBar("Mining Stone: 0/20", BarColor.GREEN, BarStyle.SEGMENTED_20);
                bossBar.addPlayer(player);
                playerBossBars.put(playerUUID, bossBar);
            }

            bossBar.setTitle("Mining Stone: " + count + "/20");
            bossBar.setProgress(count / 20.0);

            if (count >= 20) {
                player.getInventory().addItem(new org.bukkit.inventory.ItemStack(Material.DIAMOND, 1));
                player.sendMessage("You have completed the quest and received 1 diamond!");
                playerStoneCount.put(playerUUID, 0);
                bossBar.setTitle("Mining Stone: 0/20");
                bossBar.setProgress(0.0);
            }
        }
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (cmd.getName().equalsIgnoreCase("war")) {
            sender.sendMessage("Kys");
            return true;
        }
        return false;
    }
}
