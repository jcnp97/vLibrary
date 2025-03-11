package asia.virtualmc.vLibrary.utils;

import java.time.Duration;

public class DigitUtils {
    public static double roundToPrecision(double value, int decimals) {
        double scale = Math.pow(10, decimals);
        return Math.round(value * scale) / scale;
    }

    public static String formattedTwoDecimals(double value) {
        return String.format("%,.2f", value);
    }

    public static String formattedNoDecimals(double value) {
        return String.format("%,d", (int) value);
    }

    public static String formatDuration(long seconds) {
        Duration duration = Duration.ofSeconds(seconds);

        long days = duration.toDays();
        long hours = duration.toHours() % 24;
        long minutes = duration.toMinutes() % 60;
        long secs = duration.getSeconds() % 60;

        StringBuilder formatted = new StringBuilder();
        if (days > 0) formatted.append(days).append("d ");
        if (hours > 0) formatted.append(hours).append("h ");
        if (minutes > 0) formatted.append(minutes).append("m ");
        if (secs > 0 || formatted.isEmpty()) formatted.append(secs).append("s");

        return formatted.toString().trim();
    }
}
