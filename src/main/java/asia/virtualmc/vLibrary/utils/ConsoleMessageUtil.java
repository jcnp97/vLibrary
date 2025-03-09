package asia.virtualmc.vLibrary.utils;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.Component;

public class ConsoleMessageUtil {

    private static final MiniMessage miniMessage = MiniMessage.miniMessage();

    public static void pluginPrint(String componentMessage) {
        CommandSender console = Bukkit.getConsoleSender();
        Component message = miniMessage.deserialize("<#00FFA>[vLibrary] " + componentMessage);
        console.sendMessage(message);
    }

    public static void pluginPrint(String prefix, String componentMessage) {
        CommandSender console = Bukkit.getConsoleSender();
        Component message = miniMessage.deserialize("<#00FFA>" + prefix + componentMessage);
        console.sendMessage(message);
    }

    public static void print(String componentMessage) {
        CommandSender console = Bukkit.getConsoleSender();
        Component message = miniMessage.deserialize(componentMessage);
        console.sendMessage(message);
    }

    public static void printLegacy(String message) {
        CommandSender console = Bukkit.getConsoleSender();
        console.sendMessage(message);
    }

    public static void printSevere(String componentMessage) {
        CommandSender console = Bukkit.getConsoleSender();
        Component message = miniMessage.deserialize("<#FF0000>" + componentMessage);
        console.sendMessage(message);
    }
}
