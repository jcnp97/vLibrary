package asia.virtualmc.vLibrary.var;

import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;

public class GlobalVariables {
    public static Plugin archPlugin;
    public static Plugin fishPlugin;
    public static Plugin minePlugin;
    public static Plugin invPlugin;
    public static Plugin vlib;

    static {
        archPlugin = Bukkit.getPluginManager().getPlugin("vArchaeology");
        fishPlugin = Bukkit.getPluginManager().getPlugin("vFishing");
        minePlugin = Bukkit.getPluginManager().getPlugin("vMining");
        invPlugin = Bukkit.getPluginManager().getPlugin("vInvention");
    }
}
