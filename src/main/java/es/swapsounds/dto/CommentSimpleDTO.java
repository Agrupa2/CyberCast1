package es.swapsounds.DTO;

public record CommentSimpleDTO(
                long commentId,
                UserDTO user,
                String content,
                String created) {
}