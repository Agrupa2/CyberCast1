package es.swapsounds.DTO;

public record CommentDTO(
                long commentId,
                UserDTO user,
                String content,
                long soundId,
                String soundTitle,
                String created,
                String modified) {
}
