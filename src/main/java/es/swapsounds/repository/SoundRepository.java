package es.swapsounds.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import es.swapsounds.model.Sound;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface SoundRepository extends JpaRepository<Sound, Long> {
    List<Sound> findByUserId(Long userId);
    List<Sound> findByTitleContaining(String query);
    List<Sound> findByTitleContainingAndCategoriesNameIn(String query, List<String> categories);
    List<Sound> findByUserUserIdOrderByUploadDateDesc(int userId); // Search for all sounds that belong to the user with the ID, and sort them by upload date in descending order (newest first).
    @Modifying
    @Query(value = "DELETE FROM Sound s WHERE s.user_id = :userId", nativeQuery = true)
    public void deleteByUserId(@Param("userId") Long userId);
}
