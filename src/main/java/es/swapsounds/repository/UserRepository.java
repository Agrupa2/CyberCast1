package es.swapsounds.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import es.swapsounds.model.User;

public interface UserRepository extends JpaRepository<User, Long> {
    
}
