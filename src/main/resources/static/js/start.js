document.addEventListener("DOMContentLoaded", () => {
    // Animación con GSAP
    gsap.from(".hero-content h1", { duration: 1.2, y: -20, opacity: 0, ease: "power2.out" });
    gsap.from(".hero-content p", { duration: 1.5, y: -20, opacity: 0, delay: 0.3, ease: "power2.out" });

    // Evento para el botón de búsqueda
    document.getElementById("searchButton").addEventListener("click", () => {
        let query = document.getElementById("searchInput").value;
        if (query.trim() !== "") {
            searchImages(query);
        }
    });
});

// Función simulada para buscar imágenes (se puede conectar a Pixabay API)
function searchImages(query) {
    let resultsContainer = document.getElementById("resultsContainer");
    resultsContainer.innerHTML = ""; // Limpia resultados anteriores

    for (let i = 0; i < 6; i++) {
        let img = document.createElement("img");
        img.src = `https://source.unsplash.com/400x300/?${query},nature`; // Imagen aleatoria relacionada
        img.alt = query;
        resultsContainer.appendChild(img);
    }
}
