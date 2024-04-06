package org.chainoptim.desktop.shared.util;

import java.time.LocalDate;
import java.time.format.TextStyle;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class ChartUtils {

    public static List<String> generateMonthLabels(LocalDate start, LocalDate end) {
        List<String> labels = new ArrayList<>();
        LocalDate current = start;
        int previousYear = -1;

        while (!current.isAfter(end)) {
            int currentYear = current.getYear();
            String label = current.getMonth().getDisplayName(TextStyle.SHORT, Locale.getDefault());
            if (currentYear != previousYear) {
                label += " " + currentYear;
                previousYear = currentYear;
            }
            labels.add(label);
            current = current.plusMonths(1).withDayOfMonth(1);
        }

        return labels;
    }
}
