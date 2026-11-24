"use strict";
const featuredContainer = document.querySelector("#featuredProducts");
if (featuredContainer) featuredContainer.innerHTML = window.RositaProducts.slice(0, 4).map(window.productCard).join("");
