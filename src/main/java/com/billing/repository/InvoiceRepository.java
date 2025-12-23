package com.billing.repository;

import com.billing.entity.Invoice;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface InvoiceRepository extends JpaRepository<Invoice, Long> {
    
    List<Invoice> findByUserId(Long userId);
    
    Page<Invoice> findByUserId(Long userId, Pageable pageable);
    
    Optional<Invoice> findByInvoiceNumberAndUserId(String invoiceNumber, Long userId);
    
    List<Invoice> findByUserIdAndStatus(Long userId, Invoice.InvoiceStatus status);
    
    List<Invoice> findByUserIdAndInvoiceDateBetween(Long userId, LocalDate startDate, LocalDate endDate);
    
    @Query("SELECT i FROM Invoice i WHERE i.userId = :userId AND " +
           "(LOWER(i.customerName) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(i.invoiceNumber) LIKE LOWER(CONCAT('%', :keyword, '%')))")
    Page<Invoice> searchInvoices(@Param("userId") Long userId, @Param("keyword") String keyword, Pageable pageable);
    
    @Query("SELECT SUM(i.totalAmount) FROM Invoice i WHERE i.userId = :userId AND i.status = 'PAID'")
    BigDecimal getTotalRevenue(@Param("userId") Long userId);
    
    @Query("SELECT SUM(i.totalAmount) FROM Invoice i WHERE i.userId = :userId AND i.status = 'PAID' AND " +
           "i.invoiceDate BETWEEN :startDate AND :endDate")
    BigDecimal getRevenueByDateRange(@Param("userId") Long userId, @Param("startDate") LocalDate startDate, 
                                   @Param("endDate") LocalDate endDate);
    
    @Query("SELECT COUNT(i) FROM Invoice i WHERE i.userId = :userId")
    Long countByUserId(@Param("userId") Long userId);
    
    @Query("SELECT COUNT(i) FROM Invoice i WHERE i.userId = :userId AND i.status = :status")
    Long countByUserIdAndStatus(@Param("userId") Long userId, @Param("status") Invoice.InvoiceStatus status);
    
    @Query("SELECT i FROM Invoice i WHERE i.userId = :userId AND i.status = 'SENT' AND i.dueDate < :currentDate")
    List<Invoice> findOverdueInvoices(@Param("userId") Long userId, @Param("currentDate") LocalDate currentDate);
    
    @Query("SELECT i.status, COUNT(i) FROM Invoice i WHERE i.userId = :userId AND " +
           "i.invoiceDate BETWEEN :startDate AND :endDate GROUP BY i.status")
    List<Object[]> getInvoiceCountsByStatus(@Param("userId") Long userId, @Param("startDate") LocalDate startDate, 
                                          @Param("endDate") LocalDate endDate);
    
    @Query("SELECT i.invoiceDate, SUM(i.totalAmount) FROM Invoice i WHERE i.userId = :userId AND i.status = 'PAID' AND " +
           "i.invoiceDate BETWEEN :startDate AND :endDate GROUP BY i.invoiceDate ORDER BY i.invoiceDate")
    List<Object[]> getDailyRevenue(@Param("userId") Long userId, @Param("startDate") LocalDate startDate, 
                                   @Param("endDate") LocalDate endDate);
    
    @Query("SELECT EXTRACT(MONTH FROM i.invoiceDate) as month, EXTRACT(YEAR FROM i.invoiceDate) as year, " +
           "SUM(i.totalAmount) as total FROM Invoice i WHERE i.userId = :userId AND i.status = 'PAID' " +
           "GROUP BY EXTRACT(YEAR FROM i.invoiceDate), EXTRACT(MONTH FROM i.invoiceDate) " +
           "ORDER BY year DESC, month DESC")
    List<Object[]> getMonthlyRevenue(@Param("userId") Long userId);
}
