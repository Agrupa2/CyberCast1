package es.swapsounds.repository;

import es.swapsounds.model.Category;
import es.swapsounds.model.Sound;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CategoryRepository extends JpaRepository <Category, Long> {

    

}


