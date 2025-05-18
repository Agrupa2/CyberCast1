package es.swapsounds.service;

import es.swapsounds.DTO.CategoryDTO;
import es.swapsounds.DTO.CategoryMapper;
import es.swapsounds.DTO.CategorySimpleDTO;
import es.swapsounds.model.Category;
import es.swapsounds.model.Sound;
import es.swapsounds.repository.CategoryRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

@Service
public class CategoryService {

    private final CategoryRepository categoryRepository;

    @Autowired
    private CategoryMapper categoryMapper;

    public CategoryService(CategoryRepository categoryRepository) {
        this.categoryRepository = categoryRepository;
    }

    @Transactional
    public void addCategoryIfNotExists(String name) {
        String normalizedName = name.trim();
        if (!categoryRepository.existsByNameIgnoreCase(normalizedName)) {
            Category category = new Category(normalizedName);
            categoryRepository.save(category);
        }
    }

    @Transactional
    public Category findOrCreateCategory(String name) {
        return categoryRepository.findByNameIgnoreCase(name.trim())
                .orElseGet(() -> {
                    Category newCategory = new Category(name.trim());
                    return categoryRepository.save(newCategory);
                });
    }

    @Transactional
    public List<Category> getAllCategories() {
        return categoryRepository.findAll();
    }

    public Optional<Category> getCategoryById(Long id) {
        return categoryRepository.findById(id);
    }

    //funciones para editar y eliminar categorias
    public CategorySimpleDTO editCategory(Long id, CategorySimpleDTO updatedCategoryDTO){
        if(categoryRepository.existsById(id)){
            Category updatedCategory = categoryMapper.toDomain(updatedCategoryDTO);
            updatedCategory.setId(id);

            categoryRepository.save(updatedCategory);
            return categoryMapper.toSimpleDto(updatedCategory);

        }else{
            throw new NoSuchElementException();
        }
    }

    public CategoryDTO deleteCategory(Long id){
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Category not found"));
                
        // Remove the association with sounds
        for (Sound sound : category.getSounds()) {
            sound.getCategories().remove(category);
        }

        categoryRepository.delete(category);
        return categoryMapper.tDto(category);        
    }

} 
