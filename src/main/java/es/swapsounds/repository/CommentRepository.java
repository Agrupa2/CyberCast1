package es.swapsounds.repository;

import es.swapsounds.model.Comment;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CommentRepository extends JpaRepository <Comment, Long> {

}
