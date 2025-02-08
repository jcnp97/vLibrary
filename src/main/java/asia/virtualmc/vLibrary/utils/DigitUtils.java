package asia.virtualmc.vLibrary.utils;

public class DigitUtils {
    public static double roundToPrecision(double value, int decimals) {
        double scale = Math.pow(10, decimals);
        return Math.round(value * scale) / scale;
    }

    public static String formattedNumber(double value) {
        return String.format("%,.2f", value);
    }
}
