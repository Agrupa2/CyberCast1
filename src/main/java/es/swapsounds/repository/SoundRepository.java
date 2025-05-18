package es.swapsounds.repository;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import es.swapsounds.model.Sound;
import org.springframework.stereotype.Repository;

@Repository
public interface SoundRepository extends JpaRepository<Sound, Long>, JpaSpecificationExecutor<Sound> {
    List<Sound> findByUserId(long userId);

    //Next section is for dynamic queries.

    @Query("SELECT DISTINCT s FROM Sound s ORDER BY s.title ASC")
    public List<Sound> findSoundByTitle();

    @Query("""
        SELECT DISTINCT s FROM Sound s 
        LEFT JOIN s.categories c 
        WHERE (:title IS NULL OR LOWER(s.title) LIKE LOWER(CONCAT('%', :title, '%')))
        AND (:category IS NULL OR LOWER(c.name) = LOWER(:category))
        AND (:duration IS NULL OR s.duration = :duration)
        AND (:userId IS NULL OR s.userId = :userId)
        ORDER BY s.title ASC
    """)
    List<Sound> searchByFilters(
        @Param("title") String title,
        @Param("category") String category,
        @Param("duration") String duration,
        @Param("userId") Long userId
    );


}
