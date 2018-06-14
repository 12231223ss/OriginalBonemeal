package nl.ghosthost.originalbonemeal;

import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

public class OriginalBonemeal extends JavaPlugin {
    public static boolean use_permissions;
    public static double mega_tree_chance;

    @Override
    public void onEnable() {
        saveResource("config.yml", false);

        use_permissions = getConfig().getBoolean("uses_permissions");
        mega_tree_chance = getConfig().getDouble("chance.mega_tree_regular");

        PluginManager pm = getServer().getPluginManager();
        pm.registerEvents(new EventListener(this), this);
    }

    @Override
    public void onDisable() {

    }
}
