const authMessage = document.querySelector("#authMessage");
const loginForm = document.querySelector("#loginForm");
const registerForm = document.querySelector("#registerForm");
let csrfToken = "";
let csrfHeader = "X-XSRF-TOKEN";

document.addEventListener("DOMContentLoaded", async () => {
  await refreshCsrf();
  const response = await fetch("/api/auth/me");
  if (response.ok) window.location.replace("/");
});

document.querySelector("#loginTab").addEventListener("click", () => showForm("login"));
document.querySelector("#registerTab").addEventListener("click", () => showForm("register"));

loginForm.addEventListener("submit", async event => {
  event.preventDefault();
  hideMessage();
  const body = new URLSearchParams({
    username: document.querySelector("#loginEmail").value.trim(),
    password: document.querySelector("#loginPassword").value
  });

  try {
    const response = await fetch("/api/auth/login", {
      method: "POST",
      headers: { "Content-Type": "application/x-www-form-urlencoded", [csrfHeader]: csrfToken },
      body
    });
    const result = await response.json();
    if (!response.ok) throw new Error(result.message || "Unable to sign in.");
    window.location.replace("/");
  } catch (error) {
    showMessage(error.message, "error");
    await refreshCsrf();
  }
});

registerForm.addEventListener("submit", async event => {
  event.preventDefault();
  hideMessage();
  const email = document.querySelector("#registerEmail").value.trim();
  const password = document.querySelector("#registerPassword").value;

  try {
    const response = await fetch("/api/auth/register", {
      method: "POST",
      headers: { "Content-Type": "application/json", [csrfHeader]: csrfToken },
      body: JSON.stringify({
        displayName: document.querySelector("#registerName").value.trim(),
        email,
        password
      })
    });
    const result = await response.json();
    if (!response.ok) throw new Error(formatApiError(result));

    await refreshCsrf();
    const loginResponse = await fetch("/api/auth/login", {
      method: "POST",
      headers: { "Content-Type": "application/x-www-form-urlencoded", [csrfHeader]: csrfToken },
      body: new URLSearchParams({ username: email, password })
    });
    if (!loginResponse.ok) throw new Error("Account created. Please sign in.");
    window.location.replace("/");
  } catch (error) {
    showMessage(error.message, "error");
    await refreshCsrf();
  }
});

function showForm(name) {
  const login = name === "login";
  loginForm.classList.toggle("hidden", !login);
  registerForm.classList.toggle("hidden", login);
  document.querySelector("#loginTab").classList.toggle("active", login);
  document.querySelector("#registerTab").classList.toggle("active", !login);
  hideMessage();
}

async function refreshCsrf() {
  const response = await fetch("/api/auth/csrf");
  if (!response.ok) throw new Error("Security token could not be loaded.");
  const body = await response.json();
  csrfToken = body.token;
  csrfHeader = body.headerName;
}

function showMessage(text, type) {
  authMessage.textContent = text;
  authMessage.className = `message ${type}`;
}

function hideMessage() {
  authMessage.textContent = "";
  authMessage.className = "message hidden";
}

function formatApiError(error) {
  const fields = error.validationErrors
    ? Object.values(error.validationErrors).join("; ")
    : "";
  return fields || error.detail || error.message || "The request could not be completed.";
}
