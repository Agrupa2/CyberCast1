document.addEventListener("DOMContentLoaded", () => {
    // Línea de tiempo GSAP para animar todo de forma sincronizada
    const tl = gsap.timeline({ defaults: { duration: 1, ease: "power2.out" } });

    tl.from(".navbar", { y: -50, opacity: 0 }) // Animar barra de navegación
      .from(".welcome-content h1", { y: 30, opacity: 0 }, "-=0.5") // Título
      .from(".welcome-content p", { y: 30, opacity: 0 }, "-=0.4") // Párrafo
      .from(".cta-button", { scale: 0.8, opacity: 0 }, "-=0.3"); // Botón

    // Eventos para botones de navegación
    document.getElementById("loginBtn").addEventListener("click", () => {
        window.location.href = "/login"; // Redirigir a la página de login
    });

    document.getElementById("signupBtn").addEventListener("click", () => {
        window.location.href = "/signup"; // Redirigir a la página de registro
    });

    document.getElementById("contactBtn").addEventListener("click", () => {
        window.location.href = "/contact"; // Redirigir a la página de contacto
    });
});
