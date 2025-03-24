package es.swapsounds.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import es.swapsounds.model.User;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByUsername(String username);
    Optional<User> findByEmail(String email);
    boolean existsByUsername(String username);
    boolean existsByEmail(String email);
    Optional<User> findByEmailOrUsernameAndPassword(String email, String username, String password);

    @Modifying
    @Query("DELETE FROM User u WHERE u.id = :userId AND u.username = :username") // Delete a user by ID and username
    void deleteByIdAndUsername(@Param("userId") Long userId, @Param("username") String username);

    @Modifying
    @Query("UPDATE User u SET u.email = :email WHERE u.id = :userId AND u.username = :username") // Update a user's email by ID and username
    void updateEmailByIdAndUsername(
            @Param("userId") Long userId,
            @Param("username") String username,
            @Param("email") String email);
}
