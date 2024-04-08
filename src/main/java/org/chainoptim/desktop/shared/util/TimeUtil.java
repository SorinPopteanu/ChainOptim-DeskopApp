package org.chainoptim.desktop.shared.util;

public class TimeUtil {

    private static final float HOUR_SECONDS = 3600;
    private static final float DAY_SECONDS = HOUR_SECONDS * 24;
    private static final float WEEK_SECONDS = DAY_SECONDS * 7;
    private static final float MONTH_SECONDS = WEEK_SECONDS * 28; // To be modified
    private static final float YEAR_SECONDS = MONTH_SECONDS * 12;

    public static float getSeconds(Float time, String duration) {
        return switch (duration) {
            case "Hours" -> time * HOUR_SECONDS;
            case "Days" -> time * DAY_SECONDS;
            case "Weeks" -> time * WEEK_SECONDS;
            case "Months" -> time * MONTH_SECONDS;
            case "Years" -> time * YEAR_SECONDS;
            default -> -1.0f;
        };
    }

    public static float getDuration(Float timeSeconds, String timePeriod) {
        return switch (timePeriod) {
            case "Hours" -> timeSeconds / HOUR_SECONDS;
            case "Days" -> timeSeconds / DAY_SECONDS;
            case "Weeks" -> timeSeconds / WEEK_SECONDS;
            case "Months" -> timeSeconds / MONTH_SECONDS;
            case "Years" -> timeSeconds / YEAR_SECONDS;
            default -> -1.0f;
        };
    }

    public static String formatDuration(Float durationDays) {
        if (Math.abs(durationDays) < 1) {
            return String.format("%.0f hours", durationDays * 24);
        } else if (Math.abs(durationDays) < 7) {
            return String.format("%.0f days", durationDays);
        } else if (Math.abs(durationDays) < 28) {
            return String.format("%.0f weeks", durationDays / 7);
        } else if (Math.abs(durationDays) < 365) {
            return String.format("%.0f months", durationDays / 28);
        } else {
            return String.format("%.0f years", durationDays / 365);
        }
    }
}
