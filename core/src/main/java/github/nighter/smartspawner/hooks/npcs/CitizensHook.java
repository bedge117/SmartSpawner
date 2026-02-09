package github.nighter.smartspawner.hooks.npcs;

import github.nighter.smartspawner.SmartSpawner;
import github.nighter.smartspawner.spawner.lootgen.PlayerRangeWrapper;
import net.citizensnpcs.api.CitizensAPI;
import net.citizensnpcs.api.npc.NPC;
import net.citizensnpcs.api.npc.NPCRegistry;
import org.bukkit.Location;

import java.util.ArrayList;
import java.util.List;

/**
 * Hook for Citizens API to include NPC positions in spawner activation range checks.
 * Only NPCs whose name ends with the configured suffix (default: "-AFK") are counted,
 * allowing ShadowAFK shadow NPCs to activate nearby spawners.
 */
public class CitizensHook {
    private final SmartSpawner plugin;
    private boolean enabled = false;
    private String npcNameSuffix;

    public CitizensHook(SmartSpawner plugin) {
        this.plugin = plugin;
        initialize();
    }

    private void initialize() {
        try {
            if (plugin.getServer().getPluginManager().getPlugin("Citizens") == null) {
                plugin.debug("Citizens plugin not found");
                return;
            }

            // Verify Citizens API is accessible
            CitizensAPI.getNPCRegistry();

            // Load config
            npcNameSuffix = plugin.getConfig().getString("citizens_npc_activation.name_suffix", "-AFK");

            enabled = true;
            plugin.getLogger().info("Citizens integration initialized! NPC suffix filter: \"" + npcNameSuffix + "\"");
        } catch (NoClassDefFoundError | NullPointerException e) {
            plugin.debug("Citizens API not available: " + e.getMessage());
            enabled = false;
        } catch (Exception e) {
            plugin.getLogger().warning("Error initializing Citizens integration: " + e.getMessage());
            enabled = false;
        }
    }

    public boolean isEnabled() {
        return enabled;
    }

    /**
     * Get all spawned Citizens NPCs matching the name suffix filter as PlayerRangeWrappers.
     * These are included in the spawner activation range check alongside real players.
     *
     * @return list of NPC location wrappers, empty if Citizens is unavailable
     */
    public List<PlayerRangeWrapper> getSpawnedNPCWrappers() {
        if (!enabled) {
            return List.of();
        }

        try {
            NPCRegistry registry = CitizensAPI.getNPCRegistry();
            List<PlayerRangeWrapper> wrappers = new ArrayList<>();

            for (NPC npc : registry) {
                if (!npc.isSpawned()) continue;

                String name = npc.getName();
                if (name == null || !name.endsWith(npcNameSuffix)) continue;

                Location loc = npc.getStoredLocation();
                if (loc == null || loc.getWorld() == null) continue;

                wrappers.add(new PlayerRangeWrapper(
                        loc.getWorld().getUID(),
                        loc.getX(), loc.getY(), loc.getZ(),
                        true // NPCs always count as valid for activation
                ));
            }

            return wrappers;
        } catch (Exception e) {
            plugin.debug("Error getting Citizens NPC locations: " + e.getMessage());
            return List.of();
        }
    }

    /**
     * Reload configuration (called on plugin reload).
     */
    public void reload() {
        npcNameSuffix = plugin.getConfig().getString("citizens_npc_activation.name_suffix", "-AFK");
        plugin.debug("Citizens hook reloaded - suffix: \"" + npcNameSuffix + "\"");
    }
}
