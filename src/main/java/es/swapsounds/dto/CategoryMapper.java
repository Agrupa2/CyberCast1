package es.swapsounds.DTO;

import java.util.List;

import org.mapstruct.Mapper;

import es.swapsounds.model.Category;

@Mapper(componentModel = "spring")
public interface CategoryMapper {
    CategoryDTO tDto(Category category);

    CategorySimpleDTO toSimpleDto(Category c);

    List<CategorySimpleDTO> toSimpleDtoList(List<Category> cats);
}
