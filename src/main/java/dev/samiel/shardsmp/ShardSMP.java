package dev.samiel.shardsmp;

import dev.samiel.shardsmp.command.ShardCommand;
import dev.samiel.shardsmp.listener.ShardListener;
import dev.samiel.shardsmp.manager.ShardManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.command.PluginCommand;

public class ShardSMP extends JavaPlugin {

    private ShardManager shardManager;

    @Override
    public void onEnable() {
        shardManager = new ShardManager(this);
        getServer().getPluginManager().registerEvents(new ShardListener(this, shardManager), this);
        ShardCommand shardCommand = new ShardCommand(this, shardManager);

        PluginCommand shardx = getCommand("shardx");
        PluginCommand spl = getCommand("spl");
        PluginCommand shardadmin = getCommand("shardadmin");

        if (shardx != null) shardx.setExecutor(shardCommand);
        else getLogger().severe("Command 'shardx' not found in plugin.yml!");

        if (spl != null) spl.setExecutor(shardCommand);
        else getLogger().severe("Command 'spl' not found in plugin.yml!");

        if (shardadmin != null) shardadmin.setExecutor(shardCommand);
        else getLogger().severe("Command 'shardadmin' not found in plugin.yml!");

        getServer().getScheduler().runTaskTimer(this, () -> {
            for (org.bukkit.entity.Player player : getServer().getOnlinePlayers()) {
                shardManager.applyPassiveEffects(player);
            }
        }, 20L, 20L);
    }

    @Override
    public void onDisable() {
        shardManager.saveData();
    }

    public ShardManager getShardManager() {
        return shardManager;
    }
}
