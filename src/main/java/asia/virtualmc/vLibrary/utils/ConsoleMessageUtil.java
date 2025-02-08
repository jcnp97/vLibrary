package asia.virtualmc.vLibrary.utils;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.Component;

public class ConsoleMessageUtil {

    private static final MiniMessage miniMessage = MiniMessage.miniMessage();

    public static void pluginPrint(String miniMessageText) {
        CommandSender console = Bukkit.getConsoleSender();
        Component message = miniMessage.deserialize("<#00FFA>[vLibrary] " + miniMessageText);
        console.sendMessage(message);
    }

    public static void print(String miniMessageText) {
        CommandSender console = Bukkit.getConsoleSender();
        Component message = miniMessage.deserialize("<#08FBBA>" + miniMessageText);
        console.sendMessage(message);
    }

    public static void printSevere(String miniMessageText) {
        CommandSender console = Bukkit.getConsoleSender();
        Component message = miniMessage.deserialize("<#FF0000>" + miniMessageText);
        console.sendMessage(message);
    }
}
