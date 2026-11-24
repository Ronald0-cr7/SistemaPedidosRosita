"use strict";
const grid = document.querySelector("#productGrid");
const search = document.querySelector("#productSearch");
const category = document.querySelector("#categoryFilter");
const sort = document.querySelector("#sortProducts");
const count = document.querySelector("#resultCount");
const empty = document.querySelector("#emptyProducts");
const normalize = (value) => value.normalize("NFD").replace(/[\u0300-\u036f]/g, "").toLowerCase();

function refreshCategories() {
  const selected = category.value;
  category.replaceChildren(new Option("Todas las categorías", ""));
  [...new Set(window.RositaProducts.map((item) => item.category))].sort().forEach((name) => category.add(new Option(name, name)));
  if ([...category.options].some((option) => option.value === selected)) category.value = selected;
}
refreshCategories();
window.addEventListener("rosita-products-updated", () => {
  refreshCategories();
  renderProducts();
});

function renderProducts() {
  const term = normalize(search.value.trim());
  let products = window.RositaProducts.filter((item) => (!term || normalize(`${item.name} ${item.category}`).includes(term)) && (!category.value || item.category === category.value));
  if (sort.value === "price-asc") products.sort((a, b) => a.price - b.price);
  if (sort.value === "price-desc") products.sort((a, b) => b.price - a.price);
  if (sort.value === "name") products.sort((a, b) => a.name.localeCompare(b.name, "es"));
  grid.innerHTML = products.map(window.productCard).join("");
  count.textContent = `${products.length} ${products.length === 1 ? "producto" : "productos"}`;
  empty.classList.toggle("d-none", products.length > 0);
}

const params = new URLSearchParams(location.search);
search.value = params.get("q") || "";
const requestedCategory = normalize(params.get("categoria") || "");
const match = [...category.options].find((option) => normalize(option.value).includes(requestedCategory) && requestedCategory);
if (match) category.value = match.value;
[search, category, sort].forEach((control) => control.addEventListener(control === search ? "input" : "change", renderProducts));
document.querySelector("#clearFilters").addEventListener("click", () => { search.value = ""; category.value = ""; sort.value = "default"; renderProducts(); });
renderProducts();
