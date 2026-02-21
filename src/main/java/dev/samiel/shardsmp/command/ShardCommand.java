package dev.samiel.shardsmp.command;

import dev.samiel.shardsmp.ShardSMP;
import dev.samiel.shardsmp.manager.ShardManager;
import org.bukkit.ChatColor;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.entity.Zombie;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

public class ShardCommand implements CommandExecutor {

    private final ShardSMP plugin;
    private final ShardManager shardManager;

    public ShardCommand(ShardSMP plugin, ShardManager shardManager) {
        this.plugin = plugin;
        this.shardManager = shardManager;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(ChatColor.RED + "Only players can use this command.");
            return true;
        }

        if (label.equalsIgnoreCase("shards")) {
            int shards = shardManager.getShards(player);
            player.sendMessage("\u00a76[\u00a7eShardSMP\u00a76] \u00a7fYour shards: \u00a7e" + shards + "\u00a7f/6");
            return true;
        }

        if (label.equalsIgnoreCase("spl")) {
            Zombie zombie = (Zombie) player.getWorld().spawnEntity(player.getLocation(), EntityType.ZOMBIE);
            zombie.setCustomName("\u00a7cShard Dummy");
            zombie.setCustomNameVisible(true);
            zombie.setRemoveWhenFarAway(false);
            player.sendMessage("\u00a76[\u00a7eShardSMP\u00a76] \u00a7aShard Dummy spawned! Kill it to gain a shard.");
            return true;
        }

        return true;
    }
}
