"use strict";
const cartItems = document.querySelector("#cartItems");
const checkoutContent = document.querySelector("#checkoutContent");
const emptyCart = document.querySelector("#emptyCart");
const form = document.querySelector("#checkoutForm");
const deliveryFields = document.querySelector("#deliveryFields");
const addressInput = document.querySelector("#deliveryAddress");
let latestOrder = null;

const escapeHtml = (value) => String(value).replace(/[&<>'"]/g, (character) => ({ "&": "&amp;", "<": "&lt;", ">": "&gt;", "'": "&#39;", '"': "&quot;" })[character]);
const isDelivery = () => form.elements.tipo_entrega.value === "DELIVERY";
const getSubtotal = () => window.RositaCart.detailed().reduce((sum, item) => sum + item.price * item.quantity, 0);

function itemThumb(item) {
  if (item.sprite) return `<div class="cart-thumb sprite" style="background-image:url('images/productos-minimarket-grid.png');background-position:${item.sprite}" role="img" aria-label="${escapeHtml(item.name)}"></div>`;
  return `<div class="cart-thumb" style="background-image:url('${item.image}')" role="img" aria-label="${escapeHtml(item.name)}"></div>`;
}

function renderCart() {
  const items = window.RositaCart.detailed();
  const empty = items.length === 0;
  emptyCart.classList.toggle("d-none", !empty);
  checkoutContent.classList.toggle("d-none", empty);
  if (empty) return;
  cartItems.innerHTML = items.map((item) => `<article class="cart-item" data-cart-item="${item.id}">${itemThumb(item)}<div class="cart-info"><h3>${escapeHtml(item.name)}</h3><small>${escapeHtml(item.category)} · ${window.formatPrice(item.price)} c/u</small><button class="remove-item d-block mt-1" type="button" data-remove="${item.id}"><i class="bi bi-trash3"></i> Quitar</button></div><div class="quantity-control"><button type="button" data-decrease="${item.id}" aria-label="Disminuir cantidad"><i class="bi bi-dash"></i></button><input value="${item.quantity}" inputmode="numeric" aria-label="Cantidad de ${escapeHtml(item.name)}" data-quantity="${item.id}"><button type="button" data-increase="${item.id}" aria-label="Aumentar cantidad"><i class="bi bi-plus"></i></button></div><div class="cart-price">${window.formatPrice(item.price * item.quantity)}</div></article>`).join("");
  const units = items.reduce((sum, item) => sum + item.quantity, 0);
  document.querySelector("#cartItemCount").textContent = `${units} ${units === 1 ? "unidad" : "unidades"}`;
  renderTotals();
}

function renderTotals() {
  const subtotal = getSubtotal();
  const delivery = isDelivery() ? window.RositaConfig.deliveryFee : 0;
  document.querySelector("#subtotal").textContent = window.formatPrice(subtotal);
  document.querySelector("#deliveryCost").textContent = delivery ? window.formatPrice(delivery) : "Gratis";
  document.querySelector("#orderTotal").textContent = window.formatPrice(subtotal + delivery);
}

cartItems.addEventListener("click", (event) => {
  const remove = event.target.closest("[data-remove]");
  const increase = event.target.closest("[data-increase]");
  const decrease = event.target.closest("[data-decrease]");
  if (remove) window.RositaCart.remove(remove.dataset.remove);
  if (increase) { const item = window.RositaCart.get().find((current) => current.id === Number(increase.dataset.increase)); window.RositaCart.setQuantity(item.id, item.quantity + 1); }
  if (decrease) { const item = window.RositaCart.get().find((current) => current.id === Number(decrease.dataset.decrease)); if (item.quantity === 1) window.RositaCart.remove(item.id); else window.RositaCart.setQuantity(item.id, item.quantity - 1); }
  if (remove || increase || decrease) renderCart();
});

cartItems.addEventListener("change", (event) => { if (event.target.matches("[data-quantity]")) { window.RositaCart.setQuantity(event.target.dataset.quantity, event.target.value); renderCart(); } });

form.querySelectorAll("[name='tipo_entrega']").forEach((radio) => radio.addEventListener("change", () => {
  const delivery = isDelivery();
  deliveryFields.classList.toggle("d-none", !delivery);
  addressInput.required = delivery;
  renderTotals();
}));

document.querySelector("#getLocation").addEventListener("click", () => {
  const status = document.querySelector("#locationStatus");
  if (!navigator.geolocation) { status.textContent = "Tu navegador no admite ubicación."; return; }
  status.textContent = "Obteniendo ubicación...";
  navigator.geolocation.getCurrentPosition((position) => {
    const latitude = position.coords.latitude.toFixed(6);
    const longitude = position.coords.longitude.toFixed(6);
    document.querySelector("#latitude").value = latitude;
    document.querySelector("#longitude").value = longitude;
    status.innerHTML = `<i class="bi bi-check-circle-fill text-success"></i> Ubicación lista (${latitude}, ${longitude})`;
  }, () => { status.textContent = "No pudimos acceder. Activa el permiso o escribe tu dirección."; }, { enableHighAccuracy: true, timeout: 10000 });
});

function createOrder() {
  const data = new FormData(form);
  const items = window.RositaCart.detailed();
  const delivery = isDelivery() ? window.RositaConfig.deliveryFee : 0;
  const subtotal = getSubtotal();
  const latitude = data.get("latitud");
  const longitude = data.get("longitud");
  return {
    number: `ROS-${Date.now()}-${Math.random().toString(36).slice(2,6)}`,
    catalogSource: window.RositaCart.key === "rosita_inventory_cart" ? "inventory" : "demo",
    date: new Date().toISOString(),
    status: "PENDIENTE",
    customer: data.get("nombre_contacto").trim(),
    phone: data.get("telefono_contacto").trim(),
    deliveryType: data.get("tipo_entrega"),
    address: data.get("direccion_entrega")?.trim() || "Recojo en tienda",
    location: latitude && longitude ? { latitude, longitude, url: `https://maps.google.com/?q=${latitude},${longitude}` } : null,
    paymentMethod: data.get("metodo_pago"),
    notes: data.get("observaciones")?.trim() || "Sin indicaciones",
    items: items.map(({ id, name, price, quantity }) => ({ id, name, price, quantity, subtotal: price * quantity })),
    subtotal,
    deliveryFee: delivery,
    total: subtotal + delivery
  };
}

function whatsappMessage(order) {
  const lines = order.items.map((item) => `• ${item.quantity} x ${item.name} - ${window.formatPrice(item.subtotal)}`);
  return [
    "*NUEVO PEDIDO WEB - MINIMARKET ROSITA*", `Pedido: ${order.number}`, `Cliente: ${order.customer}`, `Celular: ${order.phone}`, "", "*PRODUCTOS*", ...lines, "", `Subtotal: ${window.formatPrice(order.subtotal)}`, `Delivery: ${window.formatPrice(order.deliveryFee)}`, `*TOTAL ESTIMADO: ${window.formatPrice(order.total)}*`, "", `Entrega: ${order.deliveryType}`, `Dirección: ${order.address}`, order.location ? `Ubicación: ${order.location.url}` : "", `Pago: ${order.paymentMethod}`, `Indicaciones: ${order.notes}`, "", "Por favor confirmar disponibilidad y total final."
  ].filter(Boolean).join("\n");
}

function saveOrder(order) {
  let history = [];
  try { history = JSON.parse(localStorage.getItem(window.RositaCart.historyKey)) || []; } catch { history = []; }
  history.unshift(order);
  localStorage.setItem(window.RositaCart.historyKey, JSON.stringify(history.slice(0, 30)));
}

function showVoucher(order) {
  document.querySelector("#voucherNumber").textContent = order.number;
  document.querySelector("#voucherDate").textContent = new Date(order.date).toLocaleString("es-PE");
  document.querySelector("#voucherCustomer").innerHTML = `<strong>Cliente:</strong> ${escapeHtml(order.customer)}<br><strong>Celular:</strong> ${escapeHtml(order.phone)}<br><strong>Entrega:</strong> ${escapeHtml(order.deliveryType)} - ${escapeHtml(order.address)}<br><strong>Pago:</strong> ${escapeHtml(order.paymentMethod)}`;
  document.querySelector("#voucherItems").innerHTML = order.items.map((item) => `<tr><td>${escapeHtml(item.name)}</td><td>${item.quantity}</td><td>${window.formatPrice(item.subtotal)}</td></tr>`).join("");
  document.querySelector("#voucherTotals").innerHTML = `<div class="summary-line"><span>Subtotal</span><strong>${window.formatPrice(order.subtotal)}</strong></div><div class="summary-line"><span>Delivery</span><strong>${window.formatPrice(order.deliveryFee)}</strong></div><div class="summary-total"><span>Total estimado</span><strong>${window.formatPrice(order.total)}</strong></div>`;
  document.querySelector("#voucher").classList.remove("d-none");
  document.querySelector("#voucher").scrollIntoView({ behavior: "smooth" });
}

document.querySelector("#placeOrder").addEventListener("click", () => {
  const button = document.querySelector("#placeOrder");
  if (button.disabled) return;
  form.classList.add("was-validated");
  if (!form.checkValidity()) { form.querySelector(":invalid")?.focus(); return; }
  if (!window.RositaCart.detailed().length) { window.RositaCart.notify("Agrega productos antes de enviar tu pedido."); return; }
  button.disabled = true;
  try {
  window.refreshRositaProducts();
  const items = window.RositaCart.get();
  if (items.some((item) => {
    const p = window.RositaProducts.find((p) => p.id === item.id);
    return !p || item.quantity > p.stock || p.stock <= 0;
  })) { window.RositaCart.notify("Cambió la disponibilidad. Revisa los productos de tu carrito."); renderCart(); return; }
  latestOrder = createOrder();
  saveOrder(latestOrder);
  showVoucher(latestOrder);
  const whatsappUrl = `https://wa.me/${window.RositaConfig.whatsapp}?text=${encodeURIComponent(whatsappMessage(latestOrder))}`;
  window.RositaCart.save([]);
  window.open(whatsappUrl, "_blank", "noopener");
  } catch (err) { window.RositaCart.notify("No se pudo guardar el pedido. " + err.message); }
  finally { button.disabled = false; }
});

document.querySelector("#printVoucher").addEventListener("click", () => window.print());
document.querySelector("#closeVoucher").addEventListener("click", () => { document.querySelector("#voucher").classList.add("d-none"); renderCart(); window.scrollTo({ top: 0, behavior: "smooth" }); });
renderCart();
