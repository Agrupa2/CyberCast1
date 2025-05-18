package es.swapsounds.repository;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import es.swapsounds.model.Sound;
import org.springframework.stereotype.Repository;

@Repository
public interface SoundRepository extends JpaRepository<Sound, Long> {
    List<Sound> findByUserId(long userId);

    @Query("""
               SELECT DISTINCT s
                 FROM Sound s
            LEFT JOIN s.categories c
                WHERE (:query IS NULL
                       OR LOWER(s.title) LIKE LOWER(CONCAT('%', :query, '%')))
                  AND (:category = 'all'
                       OR c.name = :category)
               """)
    Page<Sound> findFiltered(
            @Param("query") String query,
            @Param("category") String category,
            Pageable pageable);
}


