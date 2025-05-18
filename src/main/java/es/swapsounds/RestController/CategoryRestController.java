package es.swapsounds.RestController;

import java.net.URI;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import es.swapsounds.dto.CategoryDTO;
import es.swapsounds.dto.CategorySimpleDTO;
import es.swapsounds.dto.SoundDTO;
import es.swapsounds.dto.SoundMapper;
import es.swapsounds.model.Category;
import es.swapsounds.service.CategoryService;

@RestController
@RequestMapping("/api/categories")
public class CategoryRestController {

    private final CategoryService categoryService;
    private final SoundMapper soundMapper;

    public CategoryRestController(CategoryService categoryService, @Qualifier("soundMapperImpl") SoundMapper mapper) {
        this.categoryService = categoryService;
        this.soundMapper = mapper;
    }

    /**
     * Gets all categories (without sounds).
     */
    @GetMapping
    public ResponseEntity<List<CategoryDTO>> getAll() {
        List<CategoryDTO> dtos = categoryService.getAllCategories().stream()
                .map(c -> new CategoryDTO(c.getId(), c.getName(), List.of()))
                .collect(Collectors.toList());
        return ResponseEntity.ok(dtos);
    }

    /**
     * Creates or returns a category.
     */
    @PostMapping
    public ResponseEntity<CategoryDTO> create(@RequestParam String name) {
        Category cat = categoryService.findOrCreateCategory(name);
        CategoryDTO dto = new CategoryDTO(cat.getId(), cat.getName(), List.of());
        URI location = URI.create(String.format("/api/categories/%d", cat.getId()));
        return ResponseEntity.created(location).body(dto);
    }

    /**
     * Finds (or creates) a category and returns its data with sounds.
     */
    @GetMapping("/{id}")
    public ResponseEntity<CategoryDTO> getById(@PathVariable Long id) {
        Category cat = categoryService.getCategoryById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Category not found"));
        List<SoundDTO> sounds = cat.getSounds().stream()
                .map(soundMapper::toDTO)
                .collect(Collectors.toList());
        CategoryDTO dto = new CategoryDTO(cat.getId(), cat.getName(), sounds);
        return ResponseEntity.ok(dto);
    }

    /**
     * Finds or creates by name.
     */
    @GetMapping("/search")
    public ResponseEntity<CategoryDTO> search(@RequestParam String name) {
        Category cat = categoryService.findOrCreateCategory(name);
        CategoryDTO dto = new CategoryDTO(cat.getId(), cat.getName(), List.of());
        return ResponseEntity.ok(dto);
    }

    //eliminar y editar una categoría
    @PutMapping("/{id}")
    public CategorySimpleDTO editCategory(@PathVariable Long id, @RequestBody CategorySimpleDTO updatedCategoryDTO){
        return categoryService.editCategory(id, updatedCategoryDTO);
    }

    @DeleteMapping("/{id}")
    public CategoryDTO deleteCategory(@PathVariable Long id){
        return categoryService.deleteCategory(id);
    }
}
