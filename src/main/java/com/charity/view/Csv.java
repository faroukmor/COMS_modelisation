package com.charity.view;

/** Minimal RFC 4180 CSV escaping (descriptions often contain commas, quotes or line breaks). */
public final class Csv {
    private Csv() {
    }

    public static String escape(String value) {
        if (value == null) {
            return "";
        }
        boolean quote = value.contains(",") || value.contains("\"") || value.contains("\n") || value.contains("\r");
        String v = value.replace("\"", "\"\"");
        // spreadsheet formula injection: a cell starting with = + - @ would be executed by Excel
        if (!v.isEmpty() && "=+@".indexOf(v.charAt(0)) >= 0) {
            v = "'" + v;
            quote = true;
        }
        return quote ? "\"" + v + "\"" : v;
    }
}
