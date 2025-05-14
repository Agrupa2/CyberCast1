document.addEventListener('DOMContentLoaded', function () {
    // Inicializar Quill
    const quill = new Quill('#editor', {
        theme: 'snow',
        modules: {
            toolbar: [
                [{ 'header': [1, 2, false] }],
                ['bold', 'italic', 'underline'],
                ['link', 'blockquote', 'code-block'],
                [{ 'list': 'ordered'}, { 'list': 'bullet' }],
                ['clean']
            ]
        },
        placeholder: 'Escribe tu comentario...'
    });

    // Configurar DOMPurify
    const purifyConfig = {
        ALLOWED_TAGS: ['p', 'br', 'strong', 'em', 'u', 'a', 'blockquote', 'code', 'ul', 'ol', 'li', 'h1', 'h2'],
        ALLOWED_ATTR: ['href', 'target', 'class'],
        RETURN_TRUSTED_TYPE: false
    };

    // Sanitizar el contenido al enviar el formulario
    document.getElementById('comment-form').addEventListener('submit', function(event) {
        const htmlContent = quill.root.innerHTML;
        const sanitizedContent = DOMPurify.sanitize(htmlContent, purifyConfig);
        document.getElementById('content').value = sanitizedContent;
    });
});