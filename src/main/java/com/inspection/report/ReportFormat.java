package com.inspection.report;

import java.util.Locale;

/** Small helpers so that all reports look the same. */
public final class ReportFormat {

    private static final String LINE = "=".repeat(66);

    private ReportFormat() {
    }

    public static String header(String title) {
        return LINE + "\n " + title + "\n" + LINE + "\n";
    }

    /** Formats a number with two decimals and a dot, whatever the computer language is. */
    public static String number(double value) {
        return String.format(Locale.US, "%.2f", value);
    }
}
