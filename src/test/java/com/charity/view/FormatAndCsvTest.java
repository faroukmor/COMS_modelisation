package com.charity.view;

import static org.junit.jupiter.api.Assertions.*;

import java.math.BigDecimal;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class FormatAndCsvTest {
    @Test
    @DisplayName("money formatting")
    void money() {
        assertEquals("12,345.50 DZD", Format.money(new BigDecimal("12345.5")));
        assertEquals("0.00 DZD", Format.money(null));
        assertEquals("1,000,000.00", Format.number(new BigDecimal("1000000")));
    }

    @Test
    @DisplayName("amounts typed by people: spaces, comma decimals and thousands separators")
    void parseAmount() {
        assertEquals(0, new BigDecimal("1500.50").compareTo(Format.parseAmount("1 500,50")));
        assertEquals(0, new BigDecimal("1500.50").compareTo(Format.parseAmount("1,500.50")));
        assertEquals(0, new BigDecimal("2500").compareTo(Format.parseAmount(" 2500 ")));
        assertThrows(IllegalArgumentException.class, () -> Format.parseAmount("abc"));
        assertThrows(IllegalArgumentException.class, () -> Format.parseAmount(""));
        assertThrows(IllegalArgumentException.class, () -> Format.parseAmount(null));
    }

    @Test
    @DisplayName("enum labels are readable")
    void labels() {
        assertEquals("Bank transfer", Format.label(com.charity.model.PaymentMethod.BANK_TRANSFER));
        assertEquals("", Format.label(null));
    }

    @Test
    @DisplayName("CSV escaping handles commas, quotes, line breaks and spreadsheet formulas")
    void csv() {
        assertEquals("plain", Csv.escape("plain"));
        assertEquals("\"a,b\"", Csv.escape("a,b"));
        assertEquals("\"say \"\"hi\"\"\"", Csv.escape("say \"hi\""));
        assertEquals("\"line1\nline2\"", Csv.escape("line1\nline2"));
        assertEquals("\"'=SUM(A1)\"", Csv.escape("=SUM(A1)"));
        assertEquals("-50.00", Csv.escape("-50.00"));
        assertEquals("", Csv.escape(null));
    }
}
