const temaGuardado = localStorage.getItem("temaRosita") || "light";

document.documentElement.setAttribute("data-bs-theme", temaGuardado);

document.addEventListener("DOMContentLoaded", function () {

    const boton = document.createElement("button");

    boton.id = "btnTema";
    boton.type = "button";

    document.body.appendChild(boton);

    function aplicarTema(tema) {

        document.documentElement.setAttribute("data-bs-theme", tema);

        localStorage.setItem("temaRosita", tema);

        boton.innerHTML = tema === "dark"
                ? '<i class="bi bi-sun-fill"></i>'
                : '<i class="bi bi-moon-stars-fill"></i>';

        boton.setAttribute("aria-label", 
                tema === "dark" ? "Activar modo claro" : "Activar modo oscuro");
    }

    aplicarTema(temaGuardado);

    boton.addEventListener("click", function () {

        const actual = document.documentElement
                .getAttribute("data-bs-theme");

        aplicarTema(actual === "dark" ? "light" : "dark");

    });
});