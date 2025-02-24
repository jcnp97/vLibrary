package asia.virtualmc.vLibrary.utils;

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
}
