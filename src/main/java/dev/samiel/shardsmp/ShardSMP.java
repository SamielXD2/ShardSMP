package dev.samiel.shardsmp;

import dev.samiel.shardsmp.listener.ShardListener;
import dev.samiel.shardsmp.manager.ShardManager;
import org.bukkit.plugin.java.JavaPlugin;

public class ShardSMP extends JavaPlugin {

    private ShardManager shardManager;

    @Override
    public void onEnable() {
        shardManager = new ShardManager(this);
        getServer().getPluginManager().registerEvents(new ShardListener(this, shardManager), this);
        getServer().getScheduler().runTaskTimer(this, () -> {
            for (org.bukkit.entity.Player player : getServer().getOnlinePlayers()) {
                shardManager.applyPassiveEffects(player);
            }
        }, 20L, 20L);
    }

    @Override
    public void onDisable() {}

    public ShardManager getShardManager() {
        return shardManager;
    }
}