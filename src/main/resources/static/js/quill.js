document.addEventListener("DOMContentLoaded", function () {
    const quill = new Quill("#editor", {
        theme: "snow",
        modules: {
            toolbar: [
                [{ header: [1, 2, false] }],
                ["bold", "italic", "underline"],
                ["link", "blockquote", "code-block"],
                [{ list: "ordered" }, { list: "bullet" }],
                ["clean"],
            ],
        },
        placeholder: "Escribe tu comentario...",
    });

    const purifyConfig = {
        ALLOWED_TAGS: ["p", "br", "strong", "em", "u", "a", "ul", "ol", "li", "h1", "h2"],
        ALLOWED_ATTR: ["href", "target"],
        ALLOWED_URI_REGEXP: /^(https?:\/\/[a-zA-Z0-9.-]+\.[a-zA-Z]{2,}(?:\/.*)?|\/[^\\s]*)$/i,
        RETURN_TRUSTED_TYPE: false
    };

    document.getElementById("comment-form").addEventListener("submit", function (event) {
        event.preventDefault();
        try {
            const html = quill.root.innerHTML;
            if (html.length > 150) {
                alert("El comentario es demasiado largo. Máximo 10,000 caracteres.");
                return;
            }
            const clean = DOMPurify.sanitize(html, purifyConfig);
            if (!clean) {
                alert("Error al sanitizar el contenido. Por favor, intenta de nuevo.");
                return;
            }
            document.getElementById("content").value = clean;
            this.submit();
        } catch (error) {
            console.error("Error al procesar el comentario:", error);
            alert("Ocurrió un error al enviar el comentario.");
        }
    });
});