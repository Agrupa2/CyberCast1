package es.swapsounds.dto;

public record CommentSimpleDTO(
        long commentId,
        UserDTO user,
        String content,
        String created) {
}