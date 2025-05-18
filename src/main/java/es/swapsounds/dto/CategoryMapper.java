package es.swapsounds.DTO;

import java.util.List;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import es.swapsounds.model.Category;

@Mapper(componentModel = "spring")
public interface CategoryMapper {
    @Mapping(target = "sounds", ignore = true) //it doesn´t touch the sounds when you change a category.
    
    CategoryDTO tDto(Category category);

    CategorySimpleDTO toSimpleDto(Category c);

    List<CategorySimpleDTO> toSimpleDtoList(List<Category> cats);

    List<CategoryDTO> toDtoList(List<Category> cats);

    Category toDomain (CategoryDTO categoryDTO);    

    Category toDomain (CategorySimpleDTO categorySimpleDTO);
}
