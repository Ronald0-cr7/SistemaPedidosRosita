"use strict";
function renderStoreInfo() {
  const info = window.RositaStore.read();
  window.RositaConfig.whatsapp = info.whatsapp;
  document.querySelectorAll("[data-store-image]").forEach((el) => {
    el.hidden = !info.imagen;
    if (info.imagen) el.src = info.imagen;
    else el.removeAttribute("src");
    el.alt = "Foto de " + info.nombre;
  });
  document.querySelectorAll("[data-store-name]").forEach((el) => { el.textContent = info.nombre; });
  document.querySelectorAll("[data-store-address]").forEach((el) => { el.textContent = info.direccion; });
  document.querySelectorAll("[data-store-phone]").forEach((el) => { el.textContent = "+" + info.whatsapp; });
  document.querySelectorAll("[data-store-maps]").forEach((el) => { el.href = RositaStore.mapsUrl(info); });
  document.querySelectorAll("[data-store-whatsapp]").forEach((el) => { el.href = "https://wa.me/" + info.whatsapp; });
  document.querySelectorAll(".brand").forEach((el) => {
    const name = el.querySelector("strong");
    const detail = el.querySelector("small");
    if (name) name.textContent = info.nombre;
    if (detail) detail.hidden = true;
    el.setAttribute("aria-label", info.nombre + ", inicio");
  });
}
renderStoreInfo();
window.addEventListener("storage", (event) => {
  if (event.key === RositaStore.key || event.key === null) renderStoreInfo();
});
window.addEventListener("pageshow", renderStoreInfo);
