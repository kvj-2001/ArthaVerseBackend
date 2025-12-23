package com.billing.repository;

import com.billing.entity.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {
    
    List<Product> findByUserId(Long userId);
    
    Page<Product> findByUserId(Long userId, Pageable pageable);
    
    Page<Product> findByUserIdAndActive(Long userId, boolean active, Pageable pageable);
    
    Optional<Product> findByCodeAndUserId(String code, Long userId);
    
    List<Product> findByUserIdAndCategory(Long userId, String category);
    
    List<Product> findByUserIdAndActive(Long userId, boolean active);
    
    @Query("SELECT p FROM Product p WHERE p.userId = :userId AND p.quantity <= p.minStockLevel")
    List<Product> findLowStockProducts(@Param("userId") Long userId);
    
    @Query("SELECT p FROM Product p WHERE p.userId = :userId AND " +
           "(LOWER(p.name) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(p.code) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(p.description) LIKE LOWER(CONCAT('%', :keyword, '%')))")
    Page<Product> searchProducts(@Param("userId") Long userId, @Param("keyword") String keyword, Pageable pageable);
    
    @Query("SELECT DISTINCT p.category FROM Product p WHERE p.userId = :userId")
    List<String> findCategoriesByUserId(@Param("userId") Long userId);
    
    @Query("SELECT COUNT(p) FROM Product p WHERE p.userId = :userId")
    Long countByUserId(@Param("userId") Long userId);
    
    @Query("SELECT COUNT(p) FROM Product p WHERE p.userId = :userId AND p.active = true")
    Long countActiveProductsByUserId(@Param("userId") Long userId);
}
