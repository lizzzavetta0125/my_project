const titles = {
  dashboard: ["Дашборд", "Сводка за текущий месяц"],
  transactions: ["Операции", "Все доходы и расходы"],
  budgets: ["Бюджеты", "Лимиты по категориям"],
  reports: ["Отчёты", "Один интерфейс — разные стратегии"],
};

const state = {
  categories: [],
  transactions: [],
};

document.querySelectorAll(".nav-btn").forEach((button) => {
  button.addEventListener("click", () => showPage(button.dataset.page));
});

document.getElementById("open-tx-dialog").addEventListener("click", () => {
  fillCategorySelect("tx-category", document.getElementById("tx-type").value);
  document.getElementById("tx-dialog").showModal();
});
document.getElementById("close-tx-dialog").addEventListener("click", () => {
  document.getElementById("tx-dialog").close();
});
document.getElementById("tx-type").addEventListener("change", (event) => {
  fillCategorySelect("tx-category", event.target.value);
});
document.getElementById("tx-form").addEventListener("submit", onCreateTransaction);

document.getElementById("close-budget-dialog")?.addEventListener("click", () => {
  document.getElementById("budget-dialog").close();
});
document.getElementById("budget-form").addEventListener("submit", onSaveBudget);

init();

async function init() {
  await loadCategories();
  showPage("dashboard");
}

function showPage(page) {
  document.querySelectorAll(".nav-btn").forEach((btn) => {
    btn.classList.toggle("active", btn.dataset.page === page);
  });
  document.querySelectorAll(".page").forEach((section) => {
    section.classList.toggle("active", section.id === `page-${page}`);
  });
  const [title, subtitle] = titles[page];
  document.getElementById("page-title").textContent = title;
  document.getElementById("page-subtitle").textContent = subtitle;

  if (page === "dashboard") renderDashboard();
  if (page === "transactions") renderTransactions();
  if (page === "budgets") renderBudgets();
  if (page === "reports") renderReports("byCategory");
}

async function loadCategories() {
  state.categories = await api("/api/categories");
}

async function renderDashboard() {
  const data = await api("/api/dashboard");
  const root = document.getElementById("page-dashboard");
  const maxChart = Math.max(...data.chart.map((row) => row.total), 1);

  root.innerHTML = `
    <div class="cards">
      ${stat("Баланс", money(data.balance))}
      ${stat("Доходы", money(data.income), "income")}
      ${stat("Расходы", money(data.expense), "expense")}
      ${stat("Операций", data.transactionCount)}
    </div>
    <div class="split">
      <article class="card">
        <h3>Расходы по категориям</h3>
        <div class="chart" style="margin-top:16px">
          ${
            data.chart.length
              ? data.chart
                  .map(
                    (row) => `
                <div class="chart-row">
                  <span>${escapeHtml(row.label)}</span>
                  <div class="track"><span style="width:${Math.round((row.total / maxChart) * 100)}%"></span></div>
                  <strong>${money(row.total)}</strong>
                </div>`
                  )
                  .join("")
              : `<p class="empty">Пока нет расходов</p>`
          }
        </div>
      </article>
      <div>
        <article class="card" style="margin-bottom:16px">
          <h3>Предупреждения</h3>
          <div style="margin-top:12px">
            ${
              data.alerts.length
                ? data.alerts.map((item) => `<div class="alert">${escapeHtml(item.message)}</div>`).join("")
                : `<p class="empty">Лимиты в порядке. Добавьте крупный расход по «Еде», чтобы увидеть Observer.</p>`
            }
          </div>
        </article>
        <article class="card">
          <h3>Последние операции</h3>
          <div style="margin-top:8px">${data.recent.map((tx) => txRow(tx)).join("")}</div>
        </article>
      </div>
    </div>
  `;
}

async function renderTransactions() {
  state.transactions = await api("/api/transactions");
  const root = document.getElementById("page-transactions");
  root.innerHTML = `
    <article class="card">
      ${
        state.transactions.length
          ? state.transactions.map((tx) => txRow(tx, true)).join("")
          : `<p class="empty">Операций ещё нет</p>`
      }
    </article>
  `;
  root.querySelectorAll("[data-del]").forEach((button) => {
    button.addEventListener("click", async () => {
      await api(`/api/transactions/${button.dataset.del}`, { method: "DELETE" });
      toast("Операция удалена");
      renderTransactions();
    });
  });
}

async function renderBudgets() {
  const budgets = await api("/api/budgets");
  const root = document.getElementById("page-budgets");
  root.innerHTML = `
    <article class="card">
      ${
        budgets.length
          ? budgets
              .map(
                (item) => `
            <div class="budget-row">
              <div class="icon-pill"><span class="material-symbols-outlined">${item.icon}</span></div>
              <div>
                <strong>${escapeHtml(item.categoryName)}</strong>
                <div class="muted">${money(item.spent)} из ${money(item.limit)}</div>
                <div class="bar ${item.over ? "over" : ""}"><span style="width:${Math.min(item.percent, 100)}%"></span></div>
              </div>
              <span>${item.percent}%</span>
              <span class="${item.over ? "amount expense" : "muted"}">${item.over ? "сверх" : money(item.left)}</span>
            </div>`
              )
              .join("")
          : `<p class="empty">Лимиты ещё не заданы</p>`
      }
      <button class="btn filled ghost-btn" id="open-budget-dialog">
        <span class="material-symbols-outlined">tune</span>
        Задать лимит
      </button>
    </article>
  `;
  document.getElementById("open-budget-dialog").addEventListener("click", () => {
    fillCategorySelect("budget-category", "expense");
    document.getElementById("budget-dialog").showModal();
  });
}

async function renderReports(strategy) {
  const data = await api(`/api/reports?strategy=${strategy}`);
  const root = document.getElementById("page-reports");
  root.innerHTML = `
    <div class="chip-row">
      <button class="chip ${strategy === "byCategory" ? "active" : ""}" data-s="byCategory">По категориям</button>
      <button class="chip ${strategy === "byDay" ? "active" : ""}" data-s="byDay">По дням</button>
      <button class="chip ${strategy === "byType" ? "active" : ""}" data-s="byType">По типу</button>
    </div>
    <article class="card">
      <table>
        <thead>
          <tr>
            <th>Группа</th>
            <th>Значение</th>
          </tr>
        </thead>
        <tbody>
          ${data.rows
            .map((row) => {
              const extra =
                row.income != null
                  ? `${money(row.income)} / ${money(row.expense)}`
                  : money(row.total);
              return `<tr><td>${escapeHtml(row.label)}</td><td>${extra}</td></tr>`;
            })
            .join("")}
        </tbody>
      </table>
    </article>
  `;
  root.querySelectorAll(".chip").forEach((chip) => {
    chip.addEventListener("click", () => renderReports(chip.dataset.s));
  });
}

async function onCreateTransaction(event) {
  event.preventDefault();
  const form = event.target;
  const payload = Object.fromEntries(new FormData(form).entries());
  payload.amount = Number(payload.amount);
  try {
    await api("/api/transactions", { method: "POST", body: payload });
    form.reset();
    document.getElementById("tx-dialog").close();
    toast("Операция добавлена");
    const active = document.querySelector(".page.active").id.replace("page-", "");
    showPage(active);
  } catch (error) {
    toast(error.message);
  }
}

async function onSaveBudget(event) {
  event.preventDefault();
  const payload = Object.fromEntries(new FormData(event.target).entries());
  payload.limit = Number(payload.limit);
  await api("/api/budgets", { method: "PUT", body: payload });
  document.getElementById("budget-dialog").close();
  toast("Бюджет сохранён");
  renderBudgets();
}

function fillCategorySelect(selectId, type) {
  const select = document.getElementById(selectId);
  const items = state.categories.filter((item) => item.type === type);
  select.innerHTML = items
    .map((item) => `<option value="${item.id}">${escapeHtml(item.name)}</option>`)
    .join("");
}

function txRow(tx, withDelete = false) {
  const sign = tx.type === "income" ? "+" : "−";
  return `
    <div class="tx-row">
      <div class="icon-pill"><span class="material-symbols-outlined">${tx.categoryIcon}</span></div>
      <div>
        <strong>${escapeHtml(tx.categoryName)}</strong>
        <div class="muted">${escapeHtml(tx.note || "Без комментария")} · ${formatDate(tx.date)}</div>
      </div>
      <span class="amount ${tx.type}">${sign}${money(tx.amount)}</span>
      ${
        withDelete
          ? `<button class="btn danger icon-btn" data-del="${tx.id}" aria-label="Удалить">
              <span class="material-symbols-outlined">delete</span>
            </button>`
          : "<span></span>"
      }
    </div>
  `;
}

function stat(label, value, extra = "") {
  return `<article class="card stat ${extra}"><div class="label">${label}</div><div class="value">${value}</div></article>`;
}

function money(value) {
  return `${Number(value).toLocaleString("ru-RU")} ₽`;
}

function formatDate(iso) {
  return new Date(iso).toLocaleDateString("ru-RU");
}

function escapeHtml(value) {
  return String(value)
    .replaceAll("&", "&amp;")
    .replaceAll("<", "&lt;")
    .replaceAll(">", "&gt;")
    .replaceAll('"', "&quot;");
}

function toast(message) {
  const node = document.getElementById("toast");
  node.hidden = false;
  node.textContent = message;
  setTimeout(() => {
    node.hidden = true;
  }, 2400);
}

async function api(url, options = {}) {
  const response = await fetch(url, {
    headers: { "Content-Type": "application/json" },
    method: options.method || "GET",
    body: options.body ? JSON.stringify(options.body) : undefined,
  });
  const data = await response.json();
  if (!response.ok) {
    throw new Error(data.error || "Ошибка запроса");
  }
  return data;
}
