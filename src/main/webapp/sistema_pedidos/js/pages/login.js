"use strict";
document.querySelectorAll("[data-password-toggle]").forEach((button) => button.addEventListener("click", () => { const input = document.querySelector(button.dataset.passwordToggle); const hidden = input.type === "password"; input.type = hidden ? "text" : "password"; button.querySelector("i").className = hidden ? "bi bi-eye-slash" : "bi bi-eye"; button.setAttribute("aria-label", hidden ? "Ocultar contraseña" : "Mostrar contraseña"); }));
const loginForm = document.querySelector("#loginForm");
loginForm.addEventListener("submit", (event) => {
  event.preventDefault();
  loginForm.classList.add("was-validated");
  const message = document.querySelector("#loginMessage");
  message.classList.add("d-none");
  if (!loginForm.checkValidity()) return;
  try {
    window.RositaAdminAuth.login(loginForm.elements.email.value, loginForm.elements.password.value);
    location.href = "../administracion/html/panel.html#dashboard";
  } catch (err) {
    message.textContent = err.message;
    message.classList.remove("d-none", "alert-success");
    message.classList.add("alert-danger");
  }
});
