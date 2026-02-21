package dev.samiel.shardsmp.manager;

import dev.samiel.shardsmp.ShardSMP;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.EquipmentSlotGroup;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class ShardManager {

    private final ShardSMP plugin;
    private final Map<UUID, Integer> playerShards = new HashMap<>();
    private File dataFile;
    private FileConfiguration dataConfig;

    private static final int STARTING_SHARDS = 3;
    private static final int MAX_SHARDS = 6;
    private static final String HEALTH_MODIFIER_NAME = "shardx_extra_hearts";
    private static final String ABILITY_ITEM_NAME = "\u00a76ShardX Ability";
    private static final String PREFIX = "\u00a76[\u00a7eShardX\u00a76] ";

    public ShardManager(ShardSMP plugin) {
        this.plugin = plugin;
        loadData();
    }

    private void loadData() {
        dataFile = new File(plugin.getDataFolder(), "shards.yml");
        if (!dataFile.exists()) {
            plugin.getDataFolder().mkdirs();
            try {
                dataFile.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        dataConfig = YamlConfiguration.loadConfiguration(dataFile);
        for (String key : dataConfig.getKeys(false)) {
            playerShards.put(UUID.fromString(key), dataConfig.getInt(key));
        }
    }

    public void saveData() {
        for (Map.Entry<UUID, Integer> entry : playerShards.entrySet()) {
            dataConfig.set(entry.getKey().toString(), entry.getValue());
        }
        try {
            dataConfig.save(dataFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void savePlayer(Player player) {
        dataConfig.set(player.getUniqueId().toString(), getShards(player));
        try {
            dataConfig.save(dataFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public int getShards(Player player) {
        return playerShards.getOrDefault(player.getUniqueId(), STARTING_SHARDS);
    }

    public void initPlayer(Player player) {
        if (!playerShards.containsKey(player.getUniqueId())) {
            playerShards.put(player.getUniqueId(), STARTING_SHARDS);
        }
    }

    public void setShards(Player player, int amount) {
        int clamped = Math.max(1, Math.min(amount, MAX_SHARDS));
        playerShards.put(player.getUniqueId(), clamped);
        onShardChange(player, clamped);
    }

    public void addShard(Player player) {
        int current = getShards(player);
        if (current >= MAX_SHARDS) return;
        int newAmount = current + 1;
        playerShards.put(player.getUniqueId(), newAmount);
        onShardChange(player, newAmount);
    }

    public void giveShard(Player player, int amount) {
        int current = getShards(player);
        int newAmount = Math.min(current + amount, MAX_SHARDS);
        playerShards.put(player.getUniqueId(), newAmount);
        onShardChange(player, newAmount);
    }

    public void takeShard(Player player, int amount) {
        int current = getShards(player);
        int newAmount = Math.max(1, current - amount);
        playerShards.put(player.getUniqueId(), newAmount);
        onShardChange(player, newAmount);
    }

    public void resetOnDeath(Player player) {
        playerShards.put(player.getUniqueId(), 1);
        onShardChange(player, 1);
    }

    public void resetPlayer(Player player) {
        playerShards.put(player.getUniqueId(), STARTING_SHARDS);
        onShardChange(player, STARTING_SHARDS);
    }

    private void onShardChange(Player player, int shards) {
        updateHealth(player, shards);
        updateAbilityItem(player, shards);
        player.sendMessage(PREFIX + "\u00a7fShards: \u00a7e" + shards + "\u00a7f/" + MAX_SHARDS);
        player.playSound(player.getLocation(), org.bukkit.Sound.BLOCK_NOTE_BLOCK_PLING, 1.0f, 1.0f);
        savePlayer(player);
    }

    private void updateHealth(Player player, int shards) {
        var attr = player.getAttribute(Attribute.GENERIC_MAX_HEALTH);
        if (attr == null) return;
        attr.getModifiers().stream()
                .filter(m -> m.getKey().getKey().equals(HEALTH_MODIFIER_NAME))
                .forEach(attr::removeModifier);
        if (shards >= 5) {
            NamespacedKey key = new NamespacedKey(plugin, HEALTH_MODIFIER_NAME);
            AttributeModifier mod = new AttributeModifier(
                    key,
                    4.0,
                    AttributeModifier.Operation.ADD_NUMBER,
                    EquipmentSlotGroup.HAND
            );
            attr.addModifier(mod);
        }
    }

    private void updateAbilityItem(Player player, int shards) {
        if (shards == MAX_SHARDS) {
            for (ItemStack item : player.getInventory().getContents()) {
                if (isAbilityItem(item)) return;
            }
            player.getInventory().addItem(createAbilityItem());
        } else {
            for (int i = 0; i < player.getInventory().getSize(); i++) {
                ItemStack item = player.getInventory().getItem(i);
                if (isAbilityItem(item)) {
                    player.getInventory().setItem(i, null);
                }
            }
        }
    }

    public void applyPassiveEffects(Player player) {
        int shards = getShards(player);
        player.removePotionEffect(PotionEffectType.SLOWNESS);
        player.removePotionEffect(PotionEffectType.SLOW_FALLING);
        if (shards == 1) {
            player.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, 40, 0, true, false));
            player.addPotionEffect(new PotionEffect(PotionEffectType.SLOW_FALLING, 40, 0, true, false));
        } else if (shards == 2) {
            player.addPotionEffect(new PotionEffect(PotionEffectType.SLOW_FALLING, 40, 0, true, false));
        }
    }

    public void useAbility(Player player) {
        player.getWorld().getNearbyEntities(player.getLocation(), 10, 10, 10).stream()
                .filter(e -> e instanceof org.bukkit.entity.LivingEntity && !e.equals(player))
                .forEach(e -> ((org.bukkit.entity.LivingEntity) e).addPotionEffect(
                        new PotionEffect(PotionEffectType.SLOWNESS, 60, 4, true, true)
                ));
        player.sendMessage(PREFIX + "\u00a7cAbility used! Slowness applied to nearby enemies.");
    }

    public boolean isAbilityItem(ItemStack item) {
        if (item == null || item.getType() != Material.BLAZE_ROD) return false;
        ItemMeta meta = item.getItemMeta();
        return meta != null && ABILITY_ITEM_NAME.equals(meta.getDisplayName());
    }

    private ItemStack createAbilityItem() {
        ItemStack item = new ItemStack(Material.BLAZE_ROD);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(ABILITY_ITEM_NAME);
        item.setItemMeta(meta);
        return item;
    }

    public int getMaxShards() {
        return MAX_SHARDS;
    }
}
