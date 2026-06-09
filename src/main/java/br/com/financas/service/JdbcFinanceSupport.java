package br.com.financas.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import org.springframework.jdbc.core.JdbcTemplate;

abstract class JdbcFinanceSupport {
    protected final JdbcTemplate jdbc;

    protected JdbcFinanceSupport(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    protected String text(Object value) {
        return value == null ? "" : String.valueOf(value).trim();
    }

    protected String now() {
        return LocalDateTime.now().format(DateTimeFormatter.ISO_DATE_TIME);
    }

    protected long lastInsertId() {
        Long id = jdbc.queryForObject("SELECT last_insert_rowid()", Long.class);
        return id == null ? 0 : id;
    }

    protected long parseMoneyToCents(Object value) {
        String raw = text(value);
        if (raw.isBlank()) return 0;
        String normalized;
        if (raw.contains(",")) normalized = raw.replace(".", "").replace(",", ".");
        else normalized = raw;
        BigDecimal decimal = new BigDecimal(normalized).setScale(2, RoundingMode.HALF_UP);
        return decimal.movePointRight(2).longValue();
    }

    protected double centsToAmount(long cents) {
        return BigDecimal.valueOf(cents).movePointLeft(2).doubleValue();
    }

    protected Integer intValue(Object value, int fallback) {
        String raw = text(value);
        if (raw.isBlank()) return fallback;
        return Integer.valueOf(raw);
    }

    protected Long nullableLong(Object value) {
        String raw = text(value);
        if (raw.isBlank()) return null;
        return Long.valueOf(raw);
    }

    protected String normalize(String value) {
        return value == null ? "" : value.trim().toLowerCase();
    }
}
