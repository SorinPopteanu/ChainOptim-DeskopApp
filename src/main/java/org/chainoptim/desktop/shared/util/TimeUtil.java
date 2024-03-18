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
}
