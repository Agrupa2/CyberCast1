package es.swapsounds.storage;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import es.swapsounds.model.Category;

@Repository
public interface CategoryRepository extends JpaRepository<Category, Long>{
    Optional<Category> findByNameIgnoreCase(String name); // Buscar categoría por nombre (ignorar mayúsculas/minúsculas)
    boolean existsByNameIgnoreCase(String name); // Verificar si existe una categoría por nombre
}
