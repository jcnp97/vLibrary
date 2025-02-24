package asia.virtualmc.vLibrary.core;

import asia.virtualmc.vLibrary.VLibrary;
import asia.virtualmc.vLibrary.utils.EffectsUtil;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

public class EconomyLib {
    private final VLibrary plugin;
    private final Economy economy;
    private final CoreManager coreManager;

    public EconomyLib(@NotNull CoreManager coreManager) {
        this.coreManager = coreManager;
        this.plugin = coreManager.getVLibrary();
        this.economy = plugin.getEconomy();
    }

    public void addEconomy(@NotNull Player player, double totalValue) {
        if (economy != null && totalValue > 0) {
            economy.depositPlayer(player, totalValue);
            EffectsUtil.sendPlayerMessage(player, "You have received $" + totalValue + ". You now have $"
            + economy.getBalance(player) + ".");
        }
    }

    public void removeEconomy(@NotNull Player player, double totalValue) {
        if (economy != null && totalValue > 0) {
            economy.withdrawPlayer(player, totalValue);
            EffectsUtil.sendPlayerMessage(player, "<gold>$" + totalValue +
                    " <red>was taken from your balance. You now have <green>$"
                    + economy.getBalance(player) + ".");
        }
    }

    public double taxDeduction(@NotNull Player player, double taxData, double totalValue) {
        UUID uuid = player.getUniqueId();
        double taxPaid = Math.round(totalValue * taxData * 100.0) / 100.0;
        economy.withdrawPlayer(player, taxPaid);
        EffectsUtil.sendPlayerMessage(player, "<red>You have paid <gold>$" + totalValue +
                " <red>to taxes.");
        return taxPaid;
    }
}
