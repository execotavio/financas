const YEAR = 2026;
const MONTH_NAMES = ["Janeiro", "Fevereiro", "Março", "Abril", "Maio", "Junho", "Julho", "Agosto", "Setembro", "Outubro", "Novembro", "Dezembro"];

const state = {
  cards: [],
  categories: [],
  invoices: [],
  monthTransactions: [],
  yearTransactions: [],
  useSplits: false,
  movementEditingId: null,
};

const fmtCurrency = new Intl.NumberFormat("pt-BR", { style: "currency", currency: "BRL" });

function currentMonth() {
  return new Date().toISOString().slice(0, 7);
}

function money(value) {
  return fmtCurrency.format(Number(value || 0));
}

function parseMoney(value) {
  if (typeof value === "number") return value;
  if (!value) return 0;
  return Number(String(value).replace(/\./g, "").replace(",", ".")) || 0;
}

function formatCellNumber(value) {
  const n = Number(value || 0);
  if (n === 0) return "0,00";
  return n.toLocaleString("pt-BR", { minimumFractionDigits: 2, maximumFractionDigits: 2 });
}

function monthToIso(monthIndex) {
  return `${YEAR}-${String(monthIndex + 1).padStart(2, "0")}`;
}

function isoDay(dateStr) {
  if (!dateStr) return "";
  return String(dateStr).slice(8, 10);
}

function paymentLabelFromTx(tx) {
  if (tx.payment_method === "pix") return "Pix";
  if (tx.payment_method === "boleto") return "Boleto";
  if (tx.card_name) return tx.card_name;
  return "Pix/Boleto";
}

async function api(path, options = {}) {
  const response = await fetch(path, {
    ...options,
    headers: options.body instanceof FormData ? options.headers : { "Content-Type": "application/json", ...(options.headers || {}) },
  });
  const contentType = response.headers.get("content-type") || "";
  const payload = contentType.includes("application/json") ? await response.json() : await response.text();
  if (!response.ok) {
    const message = typeof payload === "string" ? payload : payload?.message || payload?.error;
    throw new Error(message || `Erro HTTP ${response.status}`);
  }
  return payload;
}

function toast(message) {
  const el = document.querySelector("#toast");
  el.textContent = message;
  el.classList.remove("hidden");
  setTimeout(() => el.classList.add("hidden"), 3200);
}

function formDataObject(form) {
  return Object.fromEntries(new FormData(form).entries());
}

function setActiveTab(tabId) {
  const sheetActive = tabId === "sheet";
  document.querySelector("#tabSheet").classList.toggle("hidden", !sheetActive);
  document.querySelector("#tabApp").classList.toggle("hidden", sheetActive);
  document.querySelector("#tabButtonSheet").classList.toggle("active", sheetActive);
  document.querySelector("#tabButtonApp").classList.toggle("active", !sheetActive);
  document.querySelector("#tabButtonSheet").setAttribute("aria-selected", String(sheetActive));
  document.querySelector("#tabButtonApp").setAttribute("aria-selected", String(!sheetActive));
}

function movementIdValue(id) {
  if (typeof id === "string" && id.startsWith("m-")) {
    const value = Number(id.slice(2));
    return Number.isFinite(value) ? value : null;
  }
  return null;
}

function isMovementTransaction(tx) {
  return movementIdValue(tx.id) !== null;
}

function prepareMovementPayload(tx) {
  const movementType = tx.movement_type === "entrada" ? "entrada" : "saida";
  const amountNumber = Math.abs(parseMoney(tx.amount));
  const amount = amountNumber.toLocaleString("pt-BR", { minimumFractionDigits: 2, maximumFractionDigits: 2 });
  return {
    tx_date: tx.tx_date || new Date().toISOString().slice(0, 10),
    movement_type: movementType,
    payment_method: tx.payment_method || "pix",
    description: tx.description || "",
    amount,
  };
}

function startMovementEdit(movementId, payload) {
  const form = document.querySelector("#movementForm");
  state.movementEditingId = movementId;
  form.tx_date.value = payload.tx_date;
  form.movement_type.value = payload.movement_type;
  form.payment_method.value = payload.payment_method || "pix";
  form.description.value = payload.description;
  form.amount.value = payload.amount;
  form.querySelector('button[type="submit"]').textContent = "Atualizar movimentação";
}

function resetMovementForm() {
  const form = document.querySelector("#movementForm");
  form.reset();
  form.tx_date.value = new Date().toISOString().slice(0, 10);
  state.movementEditingId = null;
  form.querySelector('button[type="submit"]').textContent = "Salvar movimentação";
}

async function handleMovementAction(event) {
  const button = event.target.closest("button[data-movement-action]");
  if (!button) return;
  const action = button.dataset.movementAction;
  const movementId = Number(button.dataset.movementId || 0);
  if (!Number.isFinite(movementId) || movementId <= 0) return;
  if (action === "edit") {
    const tx = state.monthTransactions.find((item) => movementIdValue(item.id) === movementId);
    if (!tx) {
      toast("Movimentação não encontrada.");
      return;
    }
    startMovementEdit(movementId, prepareMovementPayload(tx));
    toast("Edição carregada no formulário.");
    return;
  }
  if (action === "delete") {
    const confirmed = window.confirm("Deseja excluir esta movimentação?");
    if (!confirmed) return;
    await api(`/api/movements/${movementId}`, { method: "DELETE" });
    toast("Movimentação excluída.");
    if (state.movementEditingId === movementId) resetMovementForm();
    await refreshEverything();
  }
}

function buildYearMovements() {
  return state.yearTransactions.map((tx) => ({
    tx_date: tx.tx_date,
    movement_type: tx.movement_type || "saida",
    amount: Number(tx.amount || 0),
    description: tx.description || "",
    category_name: tx.has_splits ? "Detalhada" : tx.category_name || "",
    payment_method: tx.payment_method ? tx.payment_method : paymentLabelFromTx(tx),
  }));
}

function renderSpreadsheet() {
  const movements = buildYearMovements();
  const byMonthDay = new Map();
  for (const m of movements) {
    const d = new Date(`${m.tx_date}T00:00:00`);
    if (Number.isNaN(d.getTime()) || d.getFullYear() !== YEAR) continue;
    const key = `${d.getMonth()}-${d.getDate()}`;
    if (!byMonthDay.has(key)) byMonthDay.set(key, { entrada: 0, saida: 0 });
    const slot = byMonthDay.get(key);
    const amount = parseMoney(m.amount);
    if (m.movement_type === "entrada") slot.entrada += amount;
    else slot.saida += Math.abs(amount);
  }

  const table = document.querySelector("#spreadsheetTable");
  const header = MONTH_NAMES.map((name) => `<th>${name}</th><th>Entradas</th><th>Saídas</th><th>Saldo</th>`).join("");

  const balances = new Array(12).fill(0);
  const bodyRows = [];
  for (let day = 1; day <= 31; day += 1) {
    const cols = [];
    for (let month = 0; month < 12; month += 1) {
      const date = new Date(YEAR, month, day);
      if (date.getMonth() !== month) {
        cols.push("<td></td><td></td><td></td><td></td>");
        continue;
      }
      const key = `${month}-${day}`;
      const slot = byMonthDay.get(key) || { entrada: 0, saida: 0 };
      balances[month] += slot.entrada - slot.saida;
      const saldoClass = balances[month] >= 0 ? "saldo-positive" : "saldo-negative";
      cols.push(
        `<td>${String(day).padStart(2, "0")}/${String(month + 1).padStart(2, "0")}</td>` +
          `<td>${formatCellNumber(slot.entrada)}</td>` +
          `<td>${formatCellNumber(slot.saida)}</td>` +
          `<td class="${saldoClass}">${formatCellNumber(balances[month])}</td>`
      );
    }
    bodyRows.push(`<tr>${cols.join("")}</tr>`);
  }

  table.innerHTML = `<thead><tr>${header}</tr></thead><tbody>${bodyRows.join("")}</tbody>`;
}

function populateCategorySelects() {
  const active = state.categories.filter((c) => c.active !== 0);
  document.querySelectorAll('select[name="category_id"]').forEach((select) => {
    select.innerHTML = '<option value="">Sem categoria</option>';
    for (const item of active) {
      const option = document.createElement("option");
      option.value = item.id;
      option.textContent = item.name;
      select.append(option);
    }
  });
}

function populatePaymentMethodSelects() {
  const all = [
    { value: "pix", label: "Pix" },
    { value: "boleto", label: "Boleto" },
    ...state.cards.filter((c) => c.active !== 0).map((c) => ({ value: `card:${c.id}`, label: c.name })),
  ];
  ["#paymentMethod", "#movementPaymentMethod"].forEach((selector) => {
    const select = document.querySelector(selector);
    if (!select) return;
    select.innerHTML = all.map((opt) => `<option value="${opt.value}">${opt.label}</option>`).join("");
  });
  const invoiceCard = document.querySelector('#invoiceForm select[name="card_id"]');
  if (invoiceCard) {
    invoiceCard.innerHTML = state.cards
      .filter((c) => c.active !== 0)
      .map((c) => `<option value="${c.id}">${c.name}</option>`)
      .join("");
  }
}

function addSplitRow(values = {}) {
  const row = document.createElement("div");
  row.className = "split-row";
  row.innerHTML = `
    <select name="category_id" required></select>
    <input name="description" placeholder="Descrição do item" value="${values.description || ""}" />
    <input name="amount" inputmode="decimal" placeholder="0,00" value="${values.amount || ""}" required />
    <button class="danger" type="button">×</button>
  `;
  const select = row.querySelector('select[name="category_id"]');
  select.innerHTML = document.querySelector('select[name="category_id"]').innerHTML;
  if (values.category_id) select.value = values.category_id;
  row.querySelector("button").addEventListener("click", () => row.remove());
  document.querySelector("#splitRows").append(row);
}

function toggleSplits(force) {
  state.useSplits = typeof force === "boolean" ? force : !state.useSplits;
  document.querySelector("#splitEditor").classList.toggle("hidden", !state.useSplits);
  document.querySelector("#singleCategoryWrap").classList.toggle("hidden", state.useSplits);
  document.querySelector("#toggleSplits").textContent = state.useSplits ? "Categoria única" : "Detalhar";
  if (state.useSplits && !document.querySelector("#splitRows").children.length) addSplitRow();
}

async function loadMonthTransactions() {
  const month = document.querySelector("#appMonthFilter").value;
  state.monthTransactions = await api(`/api/transactions?month=${month}`);
}

async function loadYearTransactions() {
  const all = await Promise.all(MONTH_NAMES.map((_, i) => api(`/api/transactions?month=${monthToIso(i)}`)));
  state.yearTransactions = all.flat();
}

function buildMonthRows() {
  const rows = state.monthTransactions.map((tx) => ({
    id: tx.id,
    movement_id: movementIdValue(tx.id),
    tx_date: tx.tx_date,
    movement_type: tx.movement_type || "saida",
    payment_method: tx.payment_method ? tx.payment_method : paymentLabelFromTx(tx),
    category_name: tx.has_splits ? "Detalhada" : tx.category_name || "",
    description: tx.description,
    amount: Number(tx.amount || 0),
    is_movement: isMovementTransaction(tx),
  }));
  rows.sort((a, b) => String(a.tx_date).localeCompare(String(b.tx_date)));
  return rows;
}

function renderMonthTable() {
  const rows = buildMonthRows();
  const body = document.querySelector("#movementsBody");
  body.replaceChildren();

  if (!rows.length) {
    const tr = document.createElement("tr");
    const td = document.createElement("td");
    td.colSpan = 7;
    td.className = "hint";
    td.textContent = "Sem movimentações no mês.";
    tr.append(td);
    body.append(tr);
    return;
  }

  rows.forEach((row) => {
    const tr = document.createElement("tr");

    const tdDate = document.createElement("td");
    tdDate.textContent = row.tx_date || "";
    tr.append(tdDate);

    const tdType = document.createElement("td");
    tdType.textContent = row.movement_type || "";
    tr.append(tdType);

    const tdPayment = document.createElement("td");
    tdPayment.textContent = row.payment_method || "";
    tr.append(tdPayment);

    const tdCategory = document.createElement("td");
    tdCategory.textContent = row.category_name || "";
    tr.append(tdCategory);

    const tdDescription = document.createElement("td");
    tdDescription.textContent = row.description || "";
    tr.append(tdDescription);

    const tdAmount = document.createElement("td");
    tdAmount.textContent = money(row.movement_type === "entrada" ? Math.abs(parseMoney(row.amount)) : -Math.abs(parseMoney(row.amount)));
    tr.append(tdAmount);

    const tdActions = document.createElement("td");
    if (row.is_movement) {
      const editButton = document.createElement("button");
      editButton.type = "button";
      editButton.className = "ghost table-action";
      editButton.textContent = "Editar";
      editButton.setAttribute("data-movement-action", "edit");
      editButton.setAttribute("data-movement-id", String(row.movement_id || ""));
      tdActions.append(editButton);

      const deleteButton = document.createElement("button");
      deleteButton.type = "button";
      deleteButton.className = "danger table-action";
      deleteButton.textContent = "Excluir";
      deleteButton.setAttribute("data-movement-action", "delete");
      deleteButton.setAttribute("data-movement-id", String(row.movement_id || ""));
      tdActions.append(deleteButton);
    }
    tr.append(tdActions);

    body.append(tr);
  });
}

function bindModals() {
  document.querySelectorAll("[data-modal-open]").forEach((btn) => {
    btn.addEventListener("click", () => {
      const dialog = document.querySelector(`#${btn.dataset.modalOpen}`);
      if (dialog?.showModal) dialog.showModal();
    });
  });
  document.querySelectorAll("[data-modal-close]").forEach((btn) => {
    btn.addEventListener("click", () => {
      const dialog = document.querySelector(`#${btn.dataset.modalClose}`);
      if (dialog?.close) dialog.close();
    });
  });
}

async function loadBaseData() {
  const [cards, categories, invoices] = await Promise.all([api("/api/cards"), api("/api/categories"), api("/api/invoices")]);
  state.cards = cards;
  state.categories = categories;
  state.invoices = invoices;
  populateCategorySelects();
  populatePaymentMethodSelects();
}

async function refreshEverything() {
  await loadBaseData();
  await Promise.all([loadMonthTransactions(), loadYearTransactions()]);
  renderMonthTable();
  renderSpreadsheet();
}

document.querySelector('input[name="tx_date"]').value = new Date().toISOString().slice(0, 10);
document.querySelector('#movementForm input[name="tx_date"]').value = new Date().toISOString().slice(0, 10);
document.querySelector('input[name="statement_month"]').value = currentMonth();
document.querySelector("#appMonthFilter").value = currentMonth();

bindModals();
setActiveTab("sheet");

const tabSheet = document.querySelector("#tabButtonSheet");
const tabApp = document.querySelector("#tabButtonApp");
tabSheet.addEventListener("click", () => setActiveTab("sheet"));
tabApp.addEventListener("click", () => setActiveTab("app"));

document.querySelector("#toggleSplits").addEventListener("click", () => toggleSplits());
document.querySelector("#addSplit").addEventListener("click", () => addSplitRow());

document.querySelector("#appMonthFilter").addEventListener("change", async () => {
  await loadMonthTransactions();
  renderMonthTable();
});

document.querySelector("#cardForm").addEventListener("submit", async (event) => {
  event.preventDefault();
  await api("/api/cards", { method: "POST", body: JSON.stringify(formDataObject(event.currentTarget)) });
  event.currentTarget.reset();
  event.currentTarget.closing_day.value = 28;
  event.currentTarget.due_day.value = 10;
  document.querySelector("#cardModal").close();
  toast("Cartão salvo.");
  await refreshEverything();
});

document.querySelector("#categoryForm").addEventListener("submit", async (event) => {
  event.preventDefault();
  await api("/api/categories", { method: "POST", body: JSON.stringify(formDataObject(event.currentTarget)) });
  event.currentTarget.reset();
  event.currentTarget.color.value = "#2563eb";
  document.querySelector("#categoryModal").close();
  toast("Categoria criada.");
  await refreshEverything();
});

document.querySelector("#transactionForm").addEventListener("submit", async (event) => {
  event.preventDefault();
  const form = event.currentTarget;
  const data = formDataObject(form);
  const payment = data.payment_method || "pix";
  data.card_id = payment.startsWith("card:") ? payment.replace("card:", "") : "";
  delete data.payment_method;
  if (state.useSplits) {
    data.category_id = "";
    data.splits = [...document.querySelectorAll(".split-row")].map((row) => ({
      category_id: row.querySelector('select[name="category_id"]').value,
      description: row.querySelector('input[name="description"]').value,
      amount: row.querySelector('input[name="amount"]').value,
    }));
  }
  await api("/api/transactions", { method: "POST", body: JSON.stringify(data) });
  const dateValue = form.tx_date.value;
  form.reset();
  form.tx_date.value = dateValue;
  form.installment_total.value = 1;
  document.querySelector("#splitRows").innerHTML = "";
  toggleSplits(false);
  toast("Gasto registrado.");
  await refreshEverything();
});

document.querySelector("#movementForm").addEventListener("submit", async (event) => {
  event.preventDefault();
  const data = formDataObject(event.currentTarget);
  if (state.movementEditingId) {
    await api(`/api/movements/${state.movementEditingId}`, { method: "PUT", body: JSON.stringify(data) });
    toast("Movimentação atualizada.");
  } else {
    await api("/api/movements", { method: "POST", body: JSON.stringify(data) });
    toast("Movimentação salva.");
  }
  resetMovementForm();
  await refreshEverything();
});

document.querySelector("#movementsBody").addEventListener("click", (event) => {
  handleMovementAction(event).catch((error) => toast(error.message));
});

document.querySelector("#invoiceForm").addEventListener("submit", async (event) => {
  event.preventDefault();
  const data = new FormData(event.currentTarget);
  const invoice = await api("/api/invoices/upload", { method: "POST", body: data });
  document.querySelector("#invoiceResult").innerHTML = `<p class="hint">${invoice.matched_lines}/${invoice.total_lines} linhas conciliadas.</p>`;
  toast("Fatura importada.");
  await refreshEverything();
});

refreshEverything().catch((error) => toast(error.message));
