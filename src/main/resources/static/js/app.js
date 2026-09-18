const API_URL = "/api/trades";
const form = document.querySelector("#tradeForm");
const assetType = document.querySelector("#assetType");
const assetFilter = document.querySelector("#assetFilter");
const formMessage = document.querySelector("#formMessage");
const tableMessage = document.querySelector("#tableMessage");
const tableBody = document.querySelector("#tradeTableBody");
const emptyState = document.querySelector("#emptyState");
const commandmentNotes = document.querySelector("#commandmentNotes");
const notesSaveStatus = document.querySelector("#notesSaveStatus");
const NOTES_STORAGE_KEY = "tradeJournal.commandments";
let trades = [];
let notesSaveTimer;
let csrfToken = "";
let csrfHeader = "X-XSRF-TOKEN";

document.addEventListener("DOMContentLoaded", async () => {
  document.querySelector("#tradeDate").value = todayLocal();
  updateAssetFields();
  commandmentNotes.value = localStorage.getItem(NOTES_STORAGE_KEY) || "";
  showView(getViewFromHash());
  try {
    await loadCurrentUser();
    await refreshCsrf();
    await loadTrades();
  } catch (error) {
    if (error.message === "UNAUTHORIZED") {
      window.location.replace("/login.html");
      return;
    }
    showMessage(tableMessage, error.message, "error");
  }
});

document.querySelectorAll("[data-view-target]").forEach(link => {
  link.addEventListener("click", event => {
    event.preventDefault();
    const view = link.dataset.viewTarget;
    history.replaceState(null, "", `#${view}`);
    showView(view);
  });
});

document.querySelector(".brand").addEventListener("click", event => {
  event.preventDefault();
  history.replaceState(null, "", "#dashboard");
  showView("dashboard");
});

assetType.addEventListener("change", updateAssetFields);
assetFilter.addEventListener("change", renderTrades);
document.querySelector("#refreshButton").addEventListener("click", loadTrades);
document.querySelector("#logoutButton").addEventListener("click", async () => {
  try {
    await apiFetch("/api/auth/logout", { method: "POST" });
  } finally {
    window.location.replace("/login.html");
  }
});
commandmentNotes.addEventListener("input", () => {
  notesSaveStatus.textContent = "Saving...";
  window.clearTimeout(notesSaveTimer);
  notesSaveTimer = window.setTimeout(() => {
    localStorage.setItem(NOTES_STORAGE_KEY, commandmentNotes.value);
    notesSaveStatus.textContent = "Saved locally";
  }, 350);
});
document.querySelector("#clearNotesButton").addEventListener("click", () => {
  if (!window.confirm("Clear all Trading Commandments notes from this device?")) return;
  commandmentNotes.value = "";
  localStorage.removeItem(NOTES_STORAGE_KEY);
  notesSaveStatus.textContent = "Notes cleared";
  commandmentNotes.focus();
});
document.querySelector("#resetButton").addEventListener("click", () => setTimeout(() => {
  document.querySelector("#tradeDate").value = todayLocal();
  assetType.value = "OPTION";
  updateAssetFields();
}, 0));

form.addEventListener("submit", async event => {
  event.preventDefault();
  hideMessage(formMessage);
  const trade = buildTradeFromForm();

  try {
    const response = await apiFetch(API_URL, {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify(trade)
    });
    const body = await response.json();
    if (!response.ok) throw new Error(formatApiError(body));

    showMessage(formMessage, `Trade #${body.id} saved successfully.`, "success");
    form.reset();
    assetType.value = "OPTION";
    document.querySelector("#tradeDate").value = todayLocal();
    updateAssetFields();
    await loadTrades();
  } catch (error) {
    showMessage(formMessage, error.message, "error");
  }
});

function updateAssetFields() {
  const type = assetType.value;
  document.querySelectorAll(".option-field").forEach(element => element.classList.toggle("hidden", type !== "OPTION"));
  document.querySelectorAll(".derivative-field").forEach(element => element.classList.toggle("hidden", type === "STOCK"));
  document.querySelector("#optionType").required = type === "OPTION";
  document.querySelector("#strikePrice").required = type === "OPTION";
  document.querySelector("#expirationDate").required = type === "OPTION";
  document.querySelector("#contractMultiplier").value = type === "OPTION" ? "100" : "1";
}

function getViewFromHash() {
  const requestedView = window.location.hash.slice(1);
  return ["dashboard", "new-trade", "trade-history", "commandments"].includes(requestedView)
    ? requestedView
    : "dashboard";
}

function showView(view) {
  document.querySelectorAll(".view-section").forEach(section => {
    section.classList.toggle("hidden", section.dataset.view !== view);
  });
  document.querySelectorAll("[data-view-target]").forEach(link => {
    link.classList.toggle("active", link.dataset.viewTarget === view);
  });
  window.scrollTo({ top: 0, behavior: "smooth" });
}

function buildTradeFromForm() {
  const value = id => document.querySelector(`#${id}`).value.trim();
  const numberOrNull = id => value(id) === "" ? null : Number(value(id));
  const type = value("assetType");
  return {
    symbol: value("symbol"), assetType: type, direction: value("direction"), status: value("status"),
    optionType: type === "OPTION" ? value("optionType") : null,
    strikePrice: type === "OPTION" ? numberOrNull("strikePrice") : null,
    expirationDate: type === "STOCK" ? null : value("expirationDate") || null,
    contractCode: type === "STOCK" ? null : value("contractCode") || null,
    quantity: numberOrNull("quantity"), contractMultiplier: numberOrNull("contractMultiplier"),
    tradeDate: value("tradeDate"), entryTime: value("entryTime"), exitTime: value("exitTime") || null,
    entryPrice: numberOrNull("entryPrice"), exitPrice: numberOrNull("exitPrice"), fees: numberOrNull("fees"),
    stopLoss: numberOrNull("stopLoss"), targetPrice: numberOrNull("targetPrice"),
    setup: value("setup") || null, timeframe: value("timeframe") || null, notes: value("notes") || null
  };
}

async function loadTrades() {
  hideMessage(tableMessage);
  try {
    const response = await apiFetch(API_URL);
    if (!response.ok) throw new Error("Could not load trades.");
    trades = await response.json();
    renderTrades();
    renderStats();
  } catch (error) {
    showMessage(tableMessage, error.message, "error");
  }
}

function renderTrades() {
  const filter = assetFilter.value;
  const visibleTrades = filter === "ALL" ? trades : trades.filter(trade => trade.assetType === filter);
  tableBody.innerHTML = visibleTrades.map(trade => {
    const pnl = trade.profitLoss;
    const pnlClass = pnl > 0 ? "profit" : pnl < 0 ? "loss" : "";
    return `<tr>
      <td>${escapeHtml(trade.tradeDate)}</td><td><strong>${escapeHtml(trade.symbol)}</strong></td>
      <td>${escapeHtml(trade.assetType)}</td><td>${escapeHtml(trade.direction)}</td><td>${trade.quantity}</td>
      <td>${money(trade.entryPrice)}</td><td>${trade.exitPrice == null ? "—" : money(trade.exitPrice)}</td>
      <td>${escapeHtml(trade.status)}</td><td class="${pnlClass}">${pnl == null ? "—" : money(pnl)}</td>
      <td><button class="delete-button" type="button" onclick="deleteTrade(${trade.id})">Delete</button></td>
    </tr>`;
  }).join("");
  emptyState.classList.toggle("hidden", visibleTrades.length !== 0);
}

function renderStats() {
  const closed = trades.filter(trade => trade.status === "CLOSED" && trade.profitLoss != null);
  const wins = closed.filter(trade => Number(trade.profitLoss) > 0).length;
  const net = trades.reduce((sum, trade) => sum + Number(trade.profitLoss || 0), 0);
  document.querySelector("#totalTrades").textContent = trades.length;
  document.querySelector("#winRate").textContent = closed.length ? `${Math.round(wins / closed.length * 100)}%` : "0%";
  document.querySelector("#netProfitLoss").textContent = money(net);
  document.querySelector("#netProfitLoss").className = net > 0 ? "profit" : net < 0 ? "loss" : "";
  document.querySelector("#openTrades").textContent = trades.filter(trade => trade.status === "OPEN").length;
}

async function deleteTrade(id) {
  if (!window.confirm(`Delete trade #${id}?`)) return;
  try {
    const response = await apiFetch(`${API_URL}/${id}`, { method: "DELETE" });
    if (!response.ok) throw new Error("The trade could not be deleted.");
    await loadTrades();
  } catch (error) { showMessage(tableMessage, error.message, "error"); }
}

function money(value) { return new Intl.NumberFormat("en-US", { style: "currency", currency: "USD" }).format(Number(value)); }
function todayLocal() { const now = new Date(); now.setMinutes(now.getMinutes() - now.getTimezoneOffset()); return now.toISOString().slice(0, 10); }
function escapeHtml(value) { const element = document.createElement("div"); element.textContent = value ?? ""; return element.innerHTML; }
function showMessage(element, text, type) { element.textContent = text; element.className = `message ${type}`; }
function hideMessage(element) { element.textContent = ""; element.className = "message hidden"; }
function formatApiError(error) {
  const fields = error.validationErrors ? Object.entries(error.validationErrors).map(([field, message]) => `${field}: ${message}`).join("; ") : "";
  return fields || error.message || "The request could not be completed.";
}

async function loadCurrentUser() {
  const response = await fetch("/api/auth/me");
  if (response.status === 401) throw new Error("UNAUTHORIZED");
  if (!response.ok) throw new Error("Could not load your account.");
  const user = await response.json();
  document.querySelector("#currentUserName").textContent = user.displayName;
}

async function refreshCsrf() {
  const response = await fetch("/api/auth/csrf");
  if (!response.ok) throw new Error("Could not initialize request security.");
  const body = await response.json();
  csrfToken = body.token;
  csrfHeader = body.headerName;
}

async function apiFetch(url, options = {}) {
  const method = (options.method || "GET").toUpperCase();
  const headers = new Headers(options.headers || {});
  if (!["GET", "HEAD", "OPTIONS"].includes(method)) {
    headers.set(csrfHeader, csrfToken);
  }
  const response = await fetch(url, { ...options, headers });
  if (response.status === 401) {
    window.location.replace("/login.html");
    throw new Error("UNAUTHORIZED");
  }
  return response;
}
