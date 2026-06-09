package br.com.financas.config;

import jakarta.annotation.PostConstruct;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

@Component
public class DatabaseInitializer {
    private final JdbcTemplate jdbc;

    public DatabaseInitializer(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    @PostConstruct
    public void init() {
        jdbc.execute("""
                CREATE TABLE IF NOT EXISTS cards (
                  id INTEGER PRIMARY KEY AUTOINCREMENT,
                  name TEXT NOT NULL,
                  bank TEXT NOT NULL DEFAULT '',
                  last_digits TEXT,
                  closing_day INTEGER NOT NULL,
                  due_day INTEGER NOT NULL,
                  active INTEGER NOT NULL DEFAULT 1,
                  created_at TEXT NOT NULL
                )
                """);
        jdbc.execute("""
                CREATE TABLE IF NOT EXISTS categories (
                  id INTEGER PRIMARY KEY AUTOINCREMENT,
                  name TEXT NOT NULL,
                  color TEXT NOT NULL DEFAULT '#2563eb',
                  active INTEGER NOT NULL DEFAULT 1,
                  created_at TEXT NOT NULL
                )
                """);
        jdbc.execute("""
                CREATE TABLE IF NOT EXISTS transactions (
                  id INTEGER PRIMARY KEY AUTOINCREMENT,
                  tx_date TEXT NOT NULL,
                  card_id INTEGER,
                  source TEXT NOT NULL DEFAULT 'manual',
                  merchant_id INTEGER,
                  description TEXT NOT NULL,
                  normalized_description TEXT NOT NULL,
                  amount_cents INTEGER NOT NULL,
                  category_id INTEGER,
                  installment_number INTEGER,
                  installment_total INTEGER,
                  parent_group TEXT,
                  status TEXT NOT NULL DEFAULT 'open',
                  created_at TEXT NOT NULL
                )
                """);
        jdbc.execute("""
                CREATE TABLE IF NOT EXISTS transaction_splits (
                  id INTEGER PRIMARY KEY AUTOINCREMENT,
                  transaction_id INTEGER NOT NULL,
                  category_id INTEGER NOT NULL,
                  description TEXT NOT NULL DEFAULT '',
                  amount_cents INTEGER NOT NULL
                )
                """);
        jdbc.execute("""
                CREATE TABLE IF NOT EXISTS invoice_imports (
                  id INTEGER PRIMARY KEY AUTOINCREMENT,
                  card_id INTEGER NOT NULL,
                  statement_month TEXT NOT NULL,
                  filename TEXT NOT NULL,
                  stored_path TEXT NOT NULL,
                  total_lines INTEGER NOT NULL DEFAULT 0,
                  matched_lines INTEGER NOT NULL DEFAULT 0,
                  status TEXT NOT NULL DEFAULT 'processed',
                  created_at TEXT NOT NULL
                )
                """);
        jdbc.execute("""
                CREATE TABLE IF NOT EXISTS movements (
                  id INTEGER PRIMARY KEY AUTOINCREMENT,
                  tx_date TEXT NOT NULL,
                  movement_type TEXT NOT NULL,
                  payment_method TEXT NOT NULL,
                  description TEXT NOT NULL,
                  amount_cents INTEGER NOT NULL,
                  created_at TEXT NOT NULL
                )
                """);
    }
}
