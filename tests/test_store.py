import unittest
from pathlib import Path


ROOT = Path(__file__).resolve().parents[1]


class BackendStructureTest(unittest.TestCase):
    def test_backend_has_mvc_layers_and_endpoints(self):
        card_controller = (ROOT / "src" / "main" / "java" / "br" / "com" / "financas" / "controller" / "CardController.java").read_text(encoding="utf-8")
        category_controller = (ROOT / "src" / "main" / "java" / "br" / "com" / "financas" / "controller" / "CategoryController.java").read_text(encoding="utf-8")
        transaction_controller = (ROOT / "src" / "main" / "java" / "br" / "com" / "financas" / "controller" / "TransactionController.java").read_text(encoding="utf-8")
        movement_controller = (ROOT / "src" / "main" / "java" / "br" / "com" / "financas" / "controller" / "MovementController.java").read_text(encoding="utf-8")
        invoice_controller = (ROOT / "src" / "main" / "java" / "br" / "com" / "financas" / "controller" / "InvoiceController.java").read_text(encoding="utf-8")

        self.assertIn('@RequestMapping("/api/cards")', card_controller)
        self.assertIn('@RequestMapping("/api/categories")', category_controller)
        self.assertIn('@RequestMapping("/api/transactions")', transaction_controller)
        self.assertIn('@RequestMapping("/api/movements")', movement_controller)
        self.assertIn('@RequestMapping("/api/invoices")', invoice_controller)

    def test_backend_initializes_required_tables(self):
        initializer = (ROOT / "src" / "main" / "java" / "br" / "com" / "financas" / "config" / "DatabaseInitializer.java").read_text(encoding="utf-8")
        self.assertIn("CREATE TABLE IF NOT EXISTS cards", initializer)
        self.assertIn("CREATE TABLE IF NOT EXISTS categories", initializer)
        self.assertIn("CREATE TABLE IF NOT EXISTS transactions", initializer)
        self.assertIn("CREATE TABLE IF NOT EXISTS transaction_splits", initializer)
        self.assertIn("CREATE TABLE IF NOT EXISTS movements", initializer)


if __name__ == "__main__":
    unittest.main()
