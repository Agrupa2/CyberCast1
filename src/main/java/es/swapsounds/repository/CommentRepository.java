package es.swapsounds.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

import es.swapsounds.model.Comment;

public interface CommentRepository extends JpaRepository<Comment, Long> {  
    List<Comment> findBySoundId(Long soundId); // Find all comments of a sound by sound ID
    Optional<Comment> findByIdAndSoundId(Long id, Long soundId); // Find a comment by ID and sound ID
    Optional<Comment> findByIdAndAuthorId(Long id, Long authorId); // Find a comment by ID and author ID

    @Modifying
    @Query("DELETE FROM Comment c WHERE c.id = :commentId AND c.authorId = :authorId") // Delete a comment by ID and author ID
    void deleteByIdAndAuthorId(@Param("commentId") Long commentId, @Param("authorId") Long authorId);

    @Modifying
    @Query("UPDATE Comment c SET c.content = :content WHERE c.id = :commentId AND c.authorId = :authorId") // Update a comment by ID and author ID
    void updateContentByIdAndAuthorId(
            @Param("commentId") Long commentId,
            @Param("authorId") Long authorId,
            @Param("content") String content);
}



