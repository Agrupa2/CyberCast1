package es.swapsounds.repository;

import es.swapsounds.model.Comment;
import es.swapsounds.model.Sound;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CommentRepository extends JpaRepository <Comment, Long> {

}
