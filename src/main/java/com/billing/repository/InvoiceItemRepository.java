package com.billing.repository;

import com.billing.entity.InvoiceItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface InvoiceItemRepository extends JpaRepository<InvoiceItem, Long> {
    
    List<InvoiceItem> findByInvoiceId(Long invoiceId);
    
    @Query("SELECT ii FROM InvoiceItem ii WHERE ii.invoice.userId = :userId AND " +
           "ii.invoice.invoiceDate BETWEEN :startDate AND :endDate")
    List<InvoiceItem> findByUserIdAndDateRange(@Param("userId") Long userId, 
                                              @Param("startDate") LocalDate startDate, 
                                              @Param("endDate") LocalDate endDate);
    
    @Query("SELECT ii.product.name, SUM(ii.quantity) as totalQuantity, SUM(ii.totalPrice) as totalRevenue " +
           "FROM InvoiceItem ii WHERE ii.invoice.userId = :userId AND ii.invoice.status = 'PAID' " +
           "GROUP BY ii.product.id, ii.product.name ORDER BY totalRevenue DESC")
    List<Object[]> getProductPerformance(@Param("userId") Long userId);
    
    @Query("SELECT ii.product.name, SUM(ii.quantity) as totalQuantity, SUM(ii.totalPrice) as totalRevenue " +
           "FROM InvoiceItem ii WHERE ii.invoice.userId = :userId AND ii.invoice.status = 'PAID' " +
           "AND ii.invoice.invoiceDate BETWEEN :startDate AND :endDate " +
           "GROUP BY ii.product.id, ii.product.name ORDER BY totalRevenue DESC")
    List<Object[]> getProductPerformanceByDateRange(@Param("userId") Long userId, 
                                                   @Param("startDate") LocalDate startDate, 
                                                   @Param("endDate") LocalDate endDate);
    
    @Query(value = "SELECT p.name, SUM(ii.quantity) as totalQuantity, SUM(ii.total_price) as totalRevenue " +
           "FROM invoice_items ii " +
           "JOIN invoices i ON ii.invoice_id = i.id " +
           "JOIN products p ON ii.product_id = p.id " +
           "WHERE i.user_id = :userId AND i.status = 'PAID' " +
           "AND i.invoice_date BETWEEN :startDate AND :endDate " +
           "GROUP BY p.id, p.name ORDER BY totalRevenue DESC LIMIT :limit", nativeQuery = true)
    List<Object[]> getTopSellingProducts(@Param("userId") Long userId, 
                                        @Param("startDate") LocalDate startDate, 
                                        @Param("endDate") LocalDate endDate,
                                        @Param("limit") int limit);
}
