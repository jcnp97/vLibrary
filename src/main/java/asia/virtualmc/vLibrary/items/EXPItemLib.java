package asia.virtualmc.vLibrary.items;

import asia.virtualmc.vLibrary.enums.EnumsLib;
import asia.virtualmc.vLibrary.utils.DigitUtils;
import asia.virtualmc.vLibrary.utils.EffectsUtil;
import net.kyori.adventure.sound.Sound;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

public class EXPItemLib {

    public static int getLampOrStarXP(int skillLevel, int type, int amount) {
        switch (type) {
            case 1 -> type = 500;
            case 2 -> type = 1000;
            case 3 -> type = 2000;
            case 4 -> type = 4000;
            default -> type = 0;
        }
        return (int) (Math.pow((double) Math.min(skillLevel, 99) / 20, 2) * type * amount);
    }
}
