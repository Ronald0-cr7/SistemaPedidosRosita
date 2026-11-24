"use strict";
window.RositaProducts = [
  { id: 1, name: "Papas crocantes familiar", category: "Snacks", price: 6.50, stock: 18, sprite: "0% 0%" },
  { id: 2, name: "Gaseosa cola 1.5 L", category: "Gaseosas", price: 7.90, stock: 24, sprite: "50% 0%" },
  { id: 3, name: "Helado sabor clásico 1 L", category: "Helados", price: 15.90, stock: 9, sprite: "100% 0%" },
  { id: 4, name: "Cerveza rubia pack x6", category: "Cervezas y tragos", price: 28.90, stock: 12, sprite: "0% 100%" },
  { id: 5, name: "Arroz extra 5 kg", category: "Abarrotes", price: 21.50, stock: 20, sprite: "50% 100%" },
  { id: 6, name: "Jugo de naranja 1 L", category: "Bebidas", price: 6.90, stock: 15, sprite: "100% 100%" },
  { id: 7, name: "Pan integral familiar", category: "Abarrotes", price: 8.50, stock: 18, image: "images/product-thumbnail-1.png" },
  { id: 8, name: "Yogurt natural 1 L", category: "Lácteos", price: 6.90, stock: 12, image: "images/product-thumbnail-2.png" },
  { id: 9, name: "Tomate italiano por kilo", category: "Frutas y verduras", price: 4.80, stock: 24, image: "images/product-thumbnail-3.png" },
  { id: 10, name: "Espinaca fresca", category: "Frutas y verduras", price: 3.50, stock: 7, image: "images/product-thumbnail-4.png" }
];
const demoProducts = window.RositaProducts;
window.refreshRositaProducts = () => {
  try {
    const inventory = JSON.parse(localStorage.getItem("rosita_admin_demo_v1"));
    window.RositaProducts = Array.isArray(inventory?.productos)
      ? inventory.productos.filter((item) => item.activo && !item.eliminado && (!item.vencimiento || item.vencimiento >= new Intl.DateTimeFormat("sv-SE", { timeZone: "America/Lima" }).format(new Date()))).map((item) => ({
          id: item.id, name: item.nombre, category: item.categoria,
          price: item.precio, stock: item.stock,
          image: item.imagen || "../administracion/imagenes/producto.svg"
        }))
      : demoProducts;
  } catch {
    window.RositaProducts = demoProducts;
  }
};
window.refreshRositaProducts();
window.addEventListener("storage", (event) => {
  if (event.key === "rosita_admin_demo_v1" || event.key === null) {
    window.refreshRositaProducts();
    window.dispatchEvent(new Event("rosita-products-updated"));
  }
});
const productEscape = (value) => String(value).replace(/[&<>"']/g, (c) => ({ "&": "&amp;", "<": "&lt;", ">": "&gt;", '"': "&quot;", "'": "&#39;" })[c]);
window.formatPrice = (value) => new Intl.NumberFormat("es-PE", { style: "currency", currency: "PEN" }).format(value);
window.RositaConfig = {
  whatsapp: "51999999999",
  deliveryFee: 5
};

window.RositaCart = {
  get key() { return window.RositaProducts === demoProducts ? "rosita_cart" : "rosita_inventory_cart"; },
  historyKey: "rosita_orders",
  get() {
    try { return JSON.parse(localStorage.getItem(this.key)) || []; } catch { return []; }
  },
  save(items) {
    localStorage.setItem(this.key, JSON.stringify(items));
    this.updateBadges();
  },
  add(productId) {
    const product = window.RositaProducts.find((item) => item.id === Number(productId));
    if (!product || product.stock <= 0) return;
    const items = this.get();
    const current = items.find((item) => item.id === product.id);
    if (current) current.quantity = Math.min(current.quantity + 1, product.stock);
    else items.push({ id: product.id, quantity: 1 });
    this.save(items);
    this.notify(`${product.name} agregado al carrito`);
  },
  remove(productId) { this.save(this.get().filter((item) => item.id !== Number(productId))); },
  setQuantity(productId, quantity) {
    const product = window.RositaProducts.find((item) => item.id === Number(productId));
    const items = this.get();
    const current = items.find((item) => item.id === Number(productId));
    if (!current || !product) return;
    current.quantity = Math.max(1, Math.min(Number(quantity) || 1, product.stock));
    this.save(items);
  },
  detailed() {
    return this.get().map((item) => ({ ...window.RositaProducts.find((product) => product.id === item.id), quantity: item.quantity })).filter((item) => item.name);
  },
  updateBadges() {
    const amount = this.get().reduce((total, item) => total + item.quantity, 0);
    document.querySelectorAll("[data-cart-count]").forEach((badge) => { badge.textContent = amount; badge.hidden = amount === 0; });
  },
  notify(message) {
    const toast = document.querySelector("#cartToast");
    if (!toast) return;
    toast.querySelector("span").textContent = message;
    toast.classList.add("is-visible");
    clearTimeout(window.rositaToastTimer);
    window.rositaToastTimer = setTimeout(() => toast.classList.remove("is-visible"), 2400);
  }
};
window.productCard = (product) => {
  product = { ...product, name: productEscape(product.name), category: productEscape(product.category), image: productEscape(product.image || "../administracion/imagenes/producto.svg") };
  const media = product.sprite
    ? `<div class="product-sprite" role="img" aria-label="${product.name}" style="background-position:${product.sprite}"></div>`
    : `<img src="${product.image}" alt="${product.name}" loading="lazy">`;
  return `<div class="col-6 col-md-4 col-xl-3"><article class="product-card"><div class="product-image"><span class="stock-badge ${product.stock <= 7 ? "stock-low" : ""}">${product.stock} disponibles</span>${media}</div><div class="product-body"><span class="product-category">${product.category}</span><h3>${product.name}</h3><div class="product-meta"><span class="product-price">${window.formatPrice(product.price)}</span><button class="btn btn-sm btn-primary add-cart" type="button" data-add-cart="${product.id}" title="Agregar al carrito" aria-label="Agregar ${product.name} al carrito"><i class="bi bi-cart-plus"></i></button></div></div></article></div>`;
};
document.querySelectorAll("[data-current-year]").forEach((node) => { node.textContent = new Date().getFullYear(); });
document.addEventListener("click", (event) => {
  const button = event.target.closest("[data-add-cart]");
  if (button) window.RositaCart.add(button.dataset.addCart);
});

if (!document.body.classList.contains("auth-page")) {
  document.body.insertAdjacentHTML(
    "beforeend",
    `<div id="cartToast" class="cart-toast" role="status">
       <i class="bi bi-check-circle-fill"></i><span></span>
     </div>`
  );
  window.RositaCart.updateBadges();
}