package com.charity.view;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

/**
 * Display formatting for money and dates, kept in one place so every screen looks the same.
 */
public final class Format {
    private static final DateTimeFormatter DATE_TIME = DateTimeFormatter.ofPattern("dd MMM yyyy, HH:mm", Locale.ENGLISH);
    private static final DateTimeFormatter DATE = DateTimeFormatter.ofPattern("dd MMM yyyy", Locale.ENGLISH);

    private Format() {
    }

    /** {@code 12345.5 -> "12,345.50 DZD"}; null is shown as zero. */
    public static String money(BigDecimal amount) {
        DecimalFormat df = new DecimalFormat("#,##0.00", DecimalFormatSymbols.getInstance(Locale.US));
        return df.format(amount == null ? BigDecimal.ZERO : amount) + " DZD";
    }

    /** Amount without the currency suffix, for table cells whose header already says DZD. */
    public static String number(BigDecimal amount) {
        DecimalFormat df = new DecimalFormat("#,##0.00", DecimalFormatSymbols.getInstance(Locale.US));
        return df.format(amount == null ? BigDecimal.ZERO : amount);
    }

    public static String dateTime(LocalDateTime t) {
        return t == null ? "" : DATE_TIME.format(t);
    }

    public static String date(LocalDateTime t) {
        return t == null ? "" : DATE.format(t);
    }

    /** Human readable name for an enum constant: {@code BANK_TRANSFER -> "Bank transfer"}. */
    public static String label(Object enumValue) {
        if (enumValue == null) {
            return "";
        }
        String s = enumValue.toString().replace('_', ' ').toLowerCase(Locale.ROOT);
        return Character.toUpperCase(s.charAt(0)) + s.substring(1);
    }

    /** Parses a money amount typed by a person (accepts "1 000,50" and "1,000.50"). */
    public static BigDecimal parseAmount(String text) {
        if (text == null || text.trim().isEmpty()) {
            throw new IllegalArgumentException("Amount is required");
        }
        String t = text.trim().replace(" ", "").replace("\u00A0", "");
        if (t.contains(",") && !t.contains(".")) {
            t = t.replace(',', '.');
        } else {
            t = t.replace(",", "");
        }
        try {
            return new BigDecimal(t);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("'" + text.trim() + "' is not a valid amount");
        }
    }
}
