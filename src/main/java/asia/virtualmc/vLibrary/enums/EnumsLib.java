package asia.virtualmc.vLibrary.enums;

public class EnumsLib {
    public enum UpdateType {
        ADD, SUBTRACT, SET;
    }

    public enum FishingStatistics {
        RANK_ID("rank_idF", 1),
        COMMON_CAUGHT("common_caught", 2),
        UNCOMMON_CAUGHT("uncommon_caught", 3),
        RARE_CAUGHT("rare_caught", 4),
        UNIQUE_CAUGHT("unique_caught", 5),
        SPECIAL_CAUGHT("special_caught", 6),
        MYTHICAL_CAUGHT("mythical_caught", 7),
        EXOTIC_CAUGHT("exotic_caught", 8),
        LEGENDARY_CAUGHT("legendary_caught", 9),
        FISH_CAUGHT("fish_caught", 10),
        DELIVERIES_COMPLETED("deliveries_completed", 11),
        TOURNAMENTS_WON("tournaments_won", 12),
        MONEY_EARNED("money_earned", 13),
        TAXES_PAID("taxes_paid", 14),
        GAMES_PLAYED("games_played", 15),
        GAMES_WON("games_won", 16),
        CURRENT_STREAK("current_streak", 17),
        HIGHEST_STREAK("highest_streak", 18);

        private final String name;
        private final int id;

        FishingStatistics(String name, int id) {
            this.name = name;
            this.id = id;
        }

        public String getName() {
            return name;
        }

        public int getId() {
            return id;
        }

        public static FishingStatistics getById(int id) {
            for (FishingStatistics stat : values()) {
                if (stat.id == id) {
                    return stat;
                }
            }
            return null;
        }

        public static FishingStatistics getByName(String name) {
            for (FishingStatistics stat : values()) {
                if (stat.name.equalsIgnoreCase(name)) {
                    return stat;
                }
            }
            return null;
        }

        public static String getNameByEnum(FishingStatistics stat) {
            return (stat != null) ? stat.getName() : null;
        }

        public static String getNameById(int id) {
            FishingStatistics stat = getById(id);
            return (stat != null) ? stat.getName() : null;
        }
    }
}
