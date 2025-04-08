package es.swapsounds.storage;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import es.swapsounds.model.User;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
}
