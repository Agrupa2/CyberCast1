package es.swapsounds.RestController;

import java.net.URI;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import es.swapsounds.DTO.CategoryDTO;
import es.swapsounds.DTO.SoundDTO;
import es.swapsounds.DTO.SoundMapper;
import es.swapsounds.model.Category;
import es.swapsounds.service.CategoryService;

@RestController
@RequestMapping("/api/categories")
public class CategoryRestController {

    private final CategoryService categoryService;
    private final SoundMapper soundMapper;

    public CategoryRestController(CategoryService categoryService, SoundMapper soundMapper) {
        this.categoryService = categoryService;
        this.soundMapper = soundMapper;
    }

     /**
     * Obtiene todas las categorías (sin sonidos).
     */
    @GetMapping
    public ResponseEntity<List<CategoryDTO>> getAll() {
        List<CategoryDTO> dtos = categoryService.getAllCategories().stream()
                .map(c -> new CategoryDTO(c.getId(), c.getName(), List.of()))
                .collect(Collectors.toList());
        return ResponseEntity.ok(dtos);
    }

    /**
     * Crea o devuelve una categoría.
     */
    @PostMapping
    public ResponseEntity<CategoryDTO> create(@RequestParam String name) {
        Category cat = categoryService.findOrCreateCategory(name);
        CategoryDTO dto = new CategoryDTO(cat.getId(), cat.getName(), List.of());
        URI location = URI.create(String.format("/api/categories/%d", cat.getId()));
        return ResponseEntity.created(location).body(dto);
    }

    /**
     * Busca (o crea) una categoría y devuelve sus datos con sonidos.
     */
    @GetMapping("/{id}")
    public ResponseEntity<CategoryDTO> getById(@PathVariable Long id) {
        Category cat = categoryService.getCategoryById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Categoría no encontrada"));
        List<SoundDTO> sounds = cat.getSounds().stream()
                .map(soundMapper::toDTO)
                .collect(Collectors.toList());
        CategoryDTO dto = new CategoryDTO(cat.getId(), cat.getName(), sounds);
        return ResponseEntity.ok(dto);
    }

    /**
     * Busca o crea por nombre.
     */
    @GetMapping("/search")
    public ResponseEntity<CategoryDTO> search(@RequestParam String name) {
        Category cat = categoryService.findOrCreateCategory(name);
        CategoryDTO dto = new CategoryDTO(cat.getId(), cat.getName(), List.of());
        return ResponseEntity.ok(dto);
    }
}
