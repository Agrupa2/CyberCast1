package es.swapsounds.DTO;

import org.mapstruct.Mapper;

import es.swapsounds.model.Category;

@Mapper(componentModel = "spring")
public interface CategoryMapper {
    CategoryDTO tDto(Category category);
}
