package es.swapsounds.dto;

import org.mapstruct.Mapper;

import es.swapsounds.model.Comment;

@Mapper(componentModel = "spring")
public interface CommentMapper {
    CommentDTO toDto(Comment comment);
}
