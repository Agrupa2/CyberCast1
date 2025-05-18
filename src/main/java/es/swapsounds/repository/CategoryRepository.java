package es.swapsounds.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import es.swapsounds.model.Category;

@Repository
public interface CategoryRepository extends JpaRepository<Category, Long>{
    Optional<Category> findByNameIgnoreCase(String name); // Search for a category by name
    boolean existsByNameIgnoreCase(String name); // Verify if a category exists by name
    Optional<Category> findById(Long id); // Search for a category by ID
}
