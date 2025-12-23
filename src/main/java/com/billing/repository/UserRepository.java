package com.billing.repository;

import com.billing.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    
    Optional<User> findByUsername(String username);
    
    Optional<User> findByEmail(String email);
    
    Optional<User> findByEmailVerificationToken(String token);
    
    boolean existsByUsername(String username);
    
    boolean existsByEmail(String email);
    
    List<User> findByStatus(User.UserStatus status);
    
    List<User> findByRole(User.Role role);
    
    @Query("SELECT u FROM User u WHERE u.status = :status AND u.role = :role")
    List<User> findByStatusAndRole(@Param("status") User.UserStatus status, @Param("role") User.Role role);
    
    @Query("SELECT u FROM User u WHERE u.emailVerified = true AND u.status = 'APPROVED'")
    List<User> findActiveUsers();
}
