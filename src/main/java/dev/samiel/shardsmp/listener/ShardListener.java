package dev.samiel.shardsmp.listener;

import dev.samiel.shardsmp.ShardSMP;
import dev.samiel.shardsmp.manager.ShardManager;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.EquipmentSlot;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class ShardListener implements Listener {

    private final ShardSMP plugin;
    private final ShardManager shardManager;
    private final Map<UUID, Long> abilityCooldowns = new HashMap<>();
    private static final long ABILITY_COOLDOWN_MS = 5000;

    public ShardListener(ShardSMP plugin, ShardManager shardManager) {
        this.plugin = plugin;
        this.shardManager = shardManager;
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        shardManager.initPlayer(event.getPlayer());
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        shardManager.savePlayer(event.getPlayer());
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
    public void onEntityDeath(EntityDeathEvent event) {
        if (event.getEntity().getKiller() == null) return;
        String name = event.getEntity().getCustomName();
        if (name != null && name.equals("\u00a7cShard Dummy")) {
            shardManager.addShard(event.getEntity().getKiller());
        }
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (event.getHand() != EquipmentSlot.HAND) return;
        Player player = event.getPlayer();
        if (shardManager.getShards(player) < 6) return;
        if (!shardManager.isAbilityItem(player.getInventory().getItemInMainHand())) return;

        long now = System.currentTimeMillis();
        Long last = abilityCooldowns.get(player.getUniqueId());
        if (last != null && now - last < ABILITY_COOLDOWN_MS) {
            long remaining = (ABILITY_COOLDOWN_MS - (now - last)) / 1000;
            player.sendMessage("\u00a76[\u00a7eShardSMP\u00a76] \u00a7cAbility on cooldown for " + remaining + "s");
            return;
        }

        abilityCooldowns.put(player.getUniqueId(), now);
        shardManager.useAbility(player);
    }
}
