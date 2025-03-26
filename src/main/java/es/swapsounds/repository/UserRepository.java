package es.swapsounds.repository;

import es.swapsounds.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository <User, Integer> { //It should be an integer but whit Long dont make it

    
} 
