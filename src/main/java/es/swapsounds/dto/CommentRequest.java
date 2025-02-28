package es.swapsounds.dto;

public class CommentRequest {
    private String content; // Campo esperado en el JSON
    private String author;

    // Getters y Setters
    public String getContent() {
        return content;
    }
    
    public void setContent(String content) {
        this.content = content;
    }

    public String getAuthor() { // Se añade el getter
        return author;            
    }

    public void setAuthor(String author) { // Se añade el setter
        this.author = author;
    }
}
