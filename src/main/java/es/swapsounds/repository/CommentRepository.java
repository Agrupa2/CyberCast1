package es.swapsounds.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import es.swapsounds.model.Comment;

public interface CommentRepository extends JpaRepository<Comment, Long> {
    
}
