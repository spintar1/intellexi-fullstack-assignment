package com.intellexi.query.repo;

import com.intellexi.query.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface UserRepository extends JpaRepository<User, UUID> {
    Optional<User> findByEmail(String email);
    
    List<User> findByRole(User.Role role);
    
    @Query("SELECT u FROM User u ORDER BY u.lastName ASC, u.firstName ASC")
    List<User> findAllOrderedByName();
    
    boolean existsByEmail(String email);
}
