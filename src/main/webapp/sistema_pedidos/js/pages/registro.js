"use strict";
document.querySelectorAll("[data-password-toggle]").forEach((button) => button.addEventListener("click", () => { const input = document.querySelector(button.dataset.passwordToggle); const hidden = input.type === "password"; input.type = hidden ? "text" : "password"; button.querySelector("i").className = hidden ? "bi bi-eye-slash" : "bi bi-eye"; }));
const registerForm = document.querySelector("#registerForm");
registerForm.addEventListener("submit", (event) => { event.preventDefault(); registerForm.classList.add("was-validated"); if (registerForm.checkValidity()) document.querySelector("#registerMessage").classList.remove("d-none"); });
