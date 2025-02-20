document.addEventListener("DOMContentLoaded", function() {
    console.log("JS cargado correctamente");

    // Verificar si Swiper está definido
    if (typeof Swiper === "undefined") {
        console.error("Swiper no está cargado correctamente.");
        return;
    }

    // Verificar si existen slides
    if (document.querySelectorAll('.swiper-slide').length === 0) {
        console.error("No hay slides en .swiper-wrapper");
        return;
    }

    // Inicializar Swiper
    var swiper = new Swiper(".mySwiper", {
        loop:false,
        cssMode: true,
        navigation: {
            nextEl: ".swiper-button-next",
            prevEl: ".swiper-button-prev",
        },
        pagination: {
            el: ".swiper-pagination",
            clickable: true,
        },
        mousewheel: true,
        keyboard: true,
    });

    console.log("Swiper inicializado correctamente.");
});
