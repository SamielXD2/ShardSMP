package dev.samiel.shardsmp.command;

import dev.samiel.shardsmp.ShardSMP;
import dev.samiel.shardsmp.manager.ShardManager;
import org.bukkit.Bukkit;
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
    private static final String PREFIX = "\u00a76[\u00a7eShardX\u00a76] ";

    public ShardCommand(ShardSMP plugin, ShardManager shardManager) {
        this.plugin = plugin;
        this.shardManager = shardManager;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {

        if (label.equalsIgnoreCase("shardx")) {
            if (!(sender instanceof Player player)) {
                sender.sendMessage(ChatColor.RED + "Only players can use this.");
                return true;
            }
            int shards = shardManager.getShards(player);
            player.sendMessage(PREFIX + "\u00a7fYour shards: \u00a7e" + shards + "\u00a7f/" + shardManager.getMaxShards());
            return true;
        }

        if (label.equalsIgnoreCase("spl")) {
            if (!(sender instanceof Player player)) {
                sender.sendMessage(ChatColor.RED + "Only players can use this.");
                return true;
            }
            Zombie zombie = (Zombie) player.getWorld().spawnEntity(player.getLocation(), EntityType.ZOMBIE);
            zombie.setCustomName("\u00a7cShard Dummy");
            zombie.setCustomNameVisible(true);
            zombie.setRemoveWhenFarAway(false);
            player.sendMessage(PREFIX + "\u00a7aShard Dummy spawned! Kill it to gain a shard.");
            return true;
        }

        if (label.equalsIgnoreCase("shardadmin")) {
            if (!sender.hasPermission("shardx.admin") && !sender.isOp()) {
                sender.sendMessage(PREFIX + "\u00a7cYou don't have permission.");
                return true;
            }
            if (args.length == 0) {
                sender.sendMessage(PREFIX + "\u00a7eUsage:");
                sender.sendMessage("\u00a77/shardadmin set <player> <amount>");
                sender.sendMessage("\u00a77/shardadmin give <player> <amount>");
                sender.sendMessage("\u00a77/shardadmin take <player> <amount>");
                sender.sendMessage("\u00a77/shardadmin reset <player>");
                sender.sendMessage("\u00a77/shardadmin check <player>");
                return true;
            }

            String sub = args[0].toLowerCase();

            if (sub.equals("check")) {
                if (args.length < 2) { sender.sendMessage(PREFIX + "\u00a7cUsage: /shardadmin check <player>"); return true; }
                Player target = Bukkit.getPlayer(args[1]);
                if (target == null) { sender.sendMessage(PREFIX + "\u00a7cPlayer not found."); return true; }
                sender.sendMessage(PREFIX + "\u00a7f" + target.getName() + " has \u00a7e" + shardManager.getShards(target) + "\u00a7f/" + shardManager.getMaxShards() + " shards.");
                return true;
            }

            if (sub.equals("reset")) {
                if (args.length < 2) { sender.sendMessage(PREFIX + "\u00a7cUsage: /shardadmin reset <player>"); return true; }
                Player target = Bukkit.getPlayer(args[1]);
                if (target == null) { sender.sendMessage(PREFIX + "\u00a7cPlayer not found."); return true; }
                shardManager.resetPlayer(target);
                sender.sendMessage(PREFIX + "\u00a7aReset " + target.getName() + "'s shards to 3.");
                target.sendMessage(PREFIX + "\u00a7cYour shards have been reset by an admin.");
                return true;
            }

            if (args.length < 3) {
                sender.sendMessage(PREFIX + "\u00a7cUsage: /shardadmin " + sub + " <player> <amount>");
                return true;
            }

            Player target = Bukkit.getPlayer(args[1]);
            if (target == null) { sender.sendMessage(PREFIX + "\u00a7cPlayer not found."); return true; }

            int amount;
            try {
                amount = Integer.parseInt(args[2]);
            } catch (NumberFormatException e) {
                sender.sendMessage(PREFIX + "\u00a7cInvalid number.");
                return true;
            }

            switch (sub) {
                case "set" -> {
                    shardManager.setShards(target, amount);
                    sender.sendMessage(PREFIX + "\u00a7aSet " + target.getName() + "'s shards to " + amount + ".");
                    target.sendMessage(PREFIX + "\u00a7eYour shards were set to \u00a76" + amount + "\u00a7e by an admin.");
                }
                case "give" -> {
                    shardManager.giveShard(target, amount);
                    sender.sendMessage(PREFIX + "\u00a7aGave " + amount + " shards to " + target.getName() + ".");
                    target.sendMessage(PREFIX + "\u00a7eYou received \u00a76" + amount + "\u00a7e shards from an admin.");
                }
                case "take" -> {
                    shardManager.takeShard(target, amount);
                    sender.sendMessage(PREFIX + "\u00a7aTook " + amount + " shards from " + target.getName() + ".");
                    target.sendMessage(PREFIX + "\u00a7c" + amount + " shards were taken by an admin.");
                }
                default -> sender.sendMessage(PREFIX + "\u00a7cUnknown subcommand.");
            }
            return true;
        }

        return true;
    }
}
