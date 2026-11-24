"use strict";
const historyList = document.querySelector("#historyList");
const emptyHistory = document.querySelector("#emptyHistory");
const escapeHistory = (value) => String(value).replace(/[&<>'"]/g, (character) => ({ "&": "&amp;", "<": "&lt;", ">": "&gt;", "'": "&#39;", '"': "&quot;" })[character]);

function getHistory() {
  try {
    const history = JSON.parse(localStorage.getItem(window.RositaCart.historyKey)) || [];
    const admin = JSON.parse(localStorage.getItem("rosita_admin_demo_v1"));
    return history.map((order) => {
      const p = admin?.pedidos?.find((p) => p.numeroWeb === order.number);
      if (!p) return { ...order, status: admin?.pedidosImportados?.includes(order.number) ? "Eliminado" : "Pendiente" };
      const items = p.items.map((i) => ({ id:i.producto, name:i.nombre, quantity:i.cantidad, price:i.precio, subtotal:i.cantidad*i.precio }));
      const subtotal = items.reduce((s,i) => s+i.subtotal,0);
      return { ...order, status:p.estado, customer:p.cliente, phone:p.telefono, address:p.direccion, paymentMethod:p.pago, items, subtotal, total:subtotal+(p.delivery||0), deliveryFee:p.delivery||0 };
    });
  } catch { return []; }
}
function historyMessage(order) {
  const items = order.items.map((item) => `• ${item.quantity} x ${item.name} - ${window.formatPrice(item.subtotal)}`).join("\n");
  return `*REENVÍO DE PEDIDO - MINIMARKET ROSITA*\nPedido: ${order.number}\nCliente: ${order.customer}\n\n${items}\n\n*Total estimado: ${window.formatPrice(order.total)}*\nEntrega: ${order.deliveryType}\nDirección: ${order.address}${order.location ? `\nUbicación: ${order.location.url}` : ""}\nPago: ${order.paymentMethod}`;
}

function renderHistory() {
  const orders = getHistory();
  emptyHistory.classList.toggle("d-none", orders.length > 0);
  historyList.innerHTML = "";
  orders.forEach((order, index) => {
    const fragment = document.querySelector("#historyTemplate").content.cloneNode(true);
    const record = fragment.querySelector(".order-record");
    const collapse = fragment.querySelector(".collapse");
    const collapseId = `order-${index}`;
    collapse.id = collapseId;
    fragment.querySelector(".order-header").dataset.bsTarget = `#${collapseId}`;
    fragment.querySelector("[data-order-number]").textContent = order.number;
    fragment.querySelector("[data-order-date]").textContent = new Date(order.date).toLocaleDateString("es-PE", { dateStyle: "medium" });
    fragment.querySelector("[data-order-total]").textContent = window.formatPrice(order.total);
    const badge = fragment.querySelector(".status-badge");
    badge.textContent = order.status;
    badge.dataset.status = order.status;
    fragment.querySelector("[data-order-items]").innerHTML = order.items.map((item) => `<div class="history-item"><span>${item.quantity} x ${escapeHistory(item.name)}</span><strong>${window.formatPrice(item.subtotal)}</strong></div>`).join("");
    fragment.querySelector(".order-delivery").innerHTML = `<strong>${escapeHistory(order.deliveryType)}</strong><br>${escapeHistory(order.address)}<br>Pago: ${escapeHistory(order.paymentMethod)}`;
    const resend = fragment.querySelector("[data-resend]");
    resend.disabled = ["Cancelado", "Eliminado", "Atendido"].includes(order.status);
    resend.addEventListener("click", () => window.open(`https://wa.me/${window.RositaConfig.whatsapp}?text=${encodeURIComponent(historyMessage(order))}`, "_blank", "noopener"));
    fragment.querySelector("[data-delete]").addEventListener("click", () => { const current = getHistory(); current.splice(index, 1); localStorage.setItem(window.RositaCart.historyKey, JSON.stringify(current)); renderHistory(); });
    historyList.appendChild(fragment);
  });
}
renderHistory();
window.addEventListener("storage", (event) => {
  if (["rosita_admin_demo_v1", window.RositaCart.historyKey, null].includes(event.key)) renderHistory();
});
window.addEventListener("pageshow", renderHistory);
