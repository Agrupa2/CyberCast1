package es.swapsounds.repository;

import es.swapsounds.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository <User, String> {

    
} 
