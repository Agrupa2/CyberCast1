package es.swapsounds.dto;

import java.util.List;

import org.mapstruct.Mapper;

import es.swapsounds.model.Comment;

@Mapper(componentModel = "spring")
public interface CommentMapper {
    CommentDTO toDto(Comment comment);

    CommentSimpleDTO toSimpleDto(Comment c);

    List<CommentSimpleDTO> toSimpleDtoList(List<Comment> comments);
}
