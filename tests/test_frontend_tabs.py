import unittest
from pathlib import Path


ROOT = Path(__file__).resolve().parents[1]


class FrontendTabsTest(unittest.TestCase):
    def test_index_has_tabs_and_new_app_layout(self):
        html = (ROOT / "static" / "index.html").read_text(encoding="utf-8")
        self.assertIn('id="tabButtonSheet"', html)
        self.assertIn('id="tabButtonApp"', html)
        self.assertIn('id="tabSheet"', html)
        self.assertIn('id="tabApp"', html)
        self.assertIn('id="transactionForm"', html)
        self.assertIn('id="movementForm"', html)
        self.assertIn('id="appMonthFilter"', html)
        self.assertIn('id="movementsBody"', html)

    def test_index_has_required_modals_and_payment_selects(self):
        html = (ROOT / "static" / "index.html").read_text(encoding="utf-8")
        self.assertIn('data-modal-open="cardModal"', html)
        self.assertIn('data-modal-open="categoryModal"', html)
        self.assertIn('data-modal-open="invoiceModal"', html)
        self.assertIn('id="cardModal"', html)
        self.assertIn('id="categoryModal"', html)
        self.assertIn('id="invoiceModal"', html)
        self.assertIn('id="paymentMethod"', html)
        self.assertIn('id="movementPaymentMethod"', html)

    def test_styles_has_spreadsheet_vertical_lines_and_balance_colors(self):
        css = (ROOT / "static" / "styles.css").read_text(encoding="utf-8")
        self.assertIn('.spreadsheet-table th,', css)
        self.assertIn('border-right: 1px solid var(--line);', css)
        self.assertIn('.saldo-positive', css)
        self.assertIn('.saldo-negative', css)

    def test_app_js_builds_sheet_from_api_transactions(self):
        js = (ROOT / "static" / "app.js").read_text(encoding="utf-8")
        self.assertIn('function buildYearMovements()', js)
        self.assertIn('state.yearTransactions.map', js)
        self.assertIn('/api/movements', js)
        self.assertIn('movement_type === "entrada"', js)
        self.assertIn('saldo-positive', js)
        self.assertIn('saldo-negative', js)


if __name__ == "__main__":
    unittest.main()
