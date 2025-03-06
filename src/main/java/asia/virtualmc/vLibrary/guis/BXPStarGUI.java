package asia.virtualmc.vLibrary.guis;

import asia.virtualmc.vLibrary.configs.GUIConfig;
import asia.virtualmc.vLibrary.utils.DigitUtils;
import asia.virtualmc.vLibrary.utils.EffectsUtil;
import com.github.stefvanschie.inventoryframework.gui.GuiItem;
import com.github.stefvanschie.inventoryframework.gui.type.ChestGui;
import com.github.stefvanschie.inventoryframework.pane.StaticPane;
import net.kyori.adventure.sound.Sound;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.function.BiConsumer;

public class BXPStarGUI {

    public static void openBXPStarGUI(@NotNull Player player,
                                     int initialAmount,
                                     double initialXP,
                                     BiConsumer<Player, Double> xpAction) {
        ChestGui gui = new ChestGui(3, GUIConfig.CONFIRM_TITLE);
        gui.setOnGlobalClick(event -> event.setCancelled(true));

        StaticPane staticPane = new StaticPane(0, 0, 9, 3);

        for (int x = 1; x <= 3; x++) {
            ItemStack confirmButton = createConfirmButtonXP(initialXP, initialAmount);
            staticPane.addItem(new GuiItem(confirmButton, event -> xpAction.accept(player, initialXP)), x, 1);
        }

        for (int x = 5; x <= 7; x++) {
            ItemStack closeButton = createCloseButton();
            staticPane.addItem(new GuiItem(closeButton, event -> event.getWhoClicked().closeInventory()), x, 1);
        }

        gui.addPane(staticPane);
        gui.show(player);
    }

    private static ItemStack createConfirmButtonXP(double exp, int amount) {
        String formattedXP = DigitUtils.formattedNoDecimals(amount * exp);
        ItemStack button = new ItemStack(Material.PAPER);
        ItemMeta meta = button.getItemMeta();
        if (meta != null) {
            meta.setDisplayName("§eUse §a" + amount + "x §eBonus XP Stars?");
            meta.setCustomModelData(GUIConfig.INVISIBLE_ITEM);
            meta.setLore(List.of("§7You will receive §6" + formattedXP + " Bonus XP§7.",
                    "", "§8Note: §7Bonus XP doubles your", "§7EXP gain in ratio of 1:1."
            ));
            button.setItemMeta(meta);
        }
        return button;
    }

    private static ItemStack createCloseButton() {
        ItemStack button = new ItemStack(Material.PAPER);
        ItemMeta meta = button.getItemMeta();
        if (meta != null) {
            meta.setDisplayName("§cClose");
            meta.setCustomModelData(GUIConfig.INVISIBLE_ITEM);
            button.setItemMeta(meta);
        }
        return button;
    }

    public static void sendEffects(@NotNull Player player, double exp, String skillName) {
        String formattedEXP = DigitUtils.formattedNoDecimals(exp);
        EffectsUtil.sendPlayerMessage(player, "<green>You have received " + formattedEXP + " " + skillName + " bonus XP!");
        EffectsUtil.playSound(player, "minecraft:entity.player.levelup", Sound.Source.PLAYER, 1.0f, 1.0f);
    }
}
