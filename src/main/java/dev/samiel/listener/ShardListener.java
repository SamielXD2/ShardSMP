package dev.samiel.shardsmp.listener;

import dev.samiel.shardsmp.ShardSMP;
import dev.samiel.shardsmp.manager.ShardManager;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.EquipmentSlot;

public class ShardListener implements Listener {

    private final ShardSMP plugin;
    private final ShardManager shardManager;

    public ShardListener(ShardSMP plugin, ShardManager shardManager) {
        this.plugin = plugin;
        this.shardManager = shardManager;
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        shardManager.initPlayer(event.getPlayer());
    }

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event) {
        Player killed = event.getEntity();
        shardManager.resetOnDeath(killed);
        if (killed.getKiller() != null) {
            shardManager.addShard(killed.getKiller());
        }
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (event.getHand() != EquipmentSlot.HAND) return;
        Player player = event.getPlayer();
        if (shardManager.getShards(player) < 6) return;
        if (!shardManager.isAbilityItem(player.getInventory().getItemInMainHand())) return;
        shardManager.useAbility(player);
    }
}
