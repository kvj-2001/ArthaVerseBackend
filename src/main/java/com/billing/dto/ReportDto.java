package com.billing.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

public class ReportDto {
    
    private String reportType;
    private LocalDate startDate;
    private LocalDate endDate;
    private BigDecimal totalRevenue = BigDecimal.ZERO;
    private Map<String, Object> additionalData;
    
    // Frontend-specific fields
    private List<SalesTrendItem> salesTrend;
    private List<StatusDistributionItem> statusDistribution;
    private List<TopProductItem> topProducts;
    private Summary summary;
    
    // Constructors
    public ReportDto() {}
    
    public ReportDto(String reportType, LocalDate startDate, LocalDate endDate, BigDecimal totalRevenue) {
        this.reportType = reportType;
        this.startDate = startDate;
        this.endDate = endDate;
        this.totalRevenue = totalRevenue;
    }
    
    // Getters and Setters
    public String getReportType() {
        return reportType;
    }
    
    public void setReportType(String reportType) {
        this.reportType = reportType;
    }
    
    public LocalDate getStartDate() {
        return startDate;
    }
    
    public void setStartDate(LocalDate startDate) {
        this.startDate = startDate;
    }
    
    public LocalDate getEndDate() {
        return endDate;
    }
    
    public void setEndDate(LocalDate endDate) {
        this.endDate = endDate;
    }
    
    public BigDecimal getTotalRevenue() {
        return totalRevenue;
    }
    
    public void setTotalRevenue(BigDecimal totalRevenue) {
        this.totalRevenue = totalRevenue;
    }
    
    public Map<String, Object> getAdditionalData() {
        return additionalData;
    }
    
    public void setAdditionalData(Map<String, Object> additionalData) {
        this.additionalData = additionalData;
    }
    
    public List<SalesTrendItem> getSalesTrend() {
        return salesTrend;
    }
    
    public void setSalesTrend(List<SalesTrendItem> salesTrend) {
        this.salesTrend = salesTrend;
    }
    
    public List<StatusDistributionItem> getStatusDistribution() {
        return statusDistribution;
    }
    
    public void setStatusDistribution(List<StatusDistributionItem> statusDistribution) {
        this.statusDistribution = statusDistribution;
    }
    
    public List<TopProductItem> getTopProducts() {
        return topProducts;
    }

    public void setTopProducts(List<TopProductItem> topProducts) {
        this.topProducts = topProducts;
    }

    public Summary getSummary() {
        return summary;
    }

    public void setSummary(Summary summary) {
        this.summary = summary;
    }    // Inner classes for structured data
    public static class SalesTrendItem {
        private String date;
        private BigDecimal amount;
        
        public SalesTrendItem() {}
        
        public SalesTrendItem(String date, BigDecimal amount) {
            this.date = date;
            this.amount = amount;
        }
        
        public String getDate() {
            return date;
        }
        
        public void setDate(String date) {
            this.date = date;
        }
        
        public BigDecimal getAmount() {
            return amount;
        }
        
        public void setAmount(BigDecimal amount) {
            this.amount = amount;
        }
    }
    
    public static class StatusDistributionItem {
        private String status;
        private Long count;
        
        public StatusDistributionItem() {}
        
        public StatusDistributionItem(String status, Long count) {
            this.status = status;
            this.count = count;
        }
        
        public String getStatus() {
            return status;
        }
        
        public void setStatus(String status) {
            this.status = status;
        }
        
        public Long getCount() {
            return count;
        }
        
        public void setCount(Long count) {
            this.count = count;
        }
    }
    
    public static class TopProductItem {
        private String productName;
        private BigDecimal revenue;
        private Long quantity;
        
        public TopProductItem() {}
        
        public TopProductItem(String productName, BigDecimal revenue, Long quantity) {
            this.productName = productName;
            this.revenue = revenue;
            this.quantity = quantity;
        }
        
        public String getProductName() {
            return productName;
        }
        
        public void setProductName(String productName) {
            this.productName = productName;
        }
        
        public BigDecimal getRevenue() {
            return revenue;
        }
        
        public void setRevenue(BigDecimal revenue) {
            this.revenue = revenue;
        }
        
        public Long getQuantity() {
            return quantity;
        }
        
        public void setQuantity(Long quantity) {
            this.quantity = quantity;
        }
    }
    
    public static class Summary {
        private Long totalInvoices;
        private BigDecimal totalRevenue;
        private BigDecimal averageInvoiceValue;
        private Long pendingInvoices;
        
        public Summary() {}
        
        public Summary(Long totalInvoices, BigDecimal totalRevenue, BigDecimal averageInvoiceValue, Long pendingInvoices) {
            this.totalInvoices = totalInvoices;
            this.totalRevenue = totalRevenue;
            this.averageInvoiceValue = averageInvoiceValue;
            this.pendingInvoices = pendingInvoices;
        }
        
        public Long getTotalInvoices() {
            return totalInvoices;
        }
        
        public void setTotalInvoices(Long totalInvoices) {
            this.totalInvoices = totalInvoices;
        }
        
        public BigDecimal getTotalRevenue() {
            return totalRevenue;
        }
        
        public void setTotalRevenue(BigDecimal totalRevenue) {
            this.totalRevenue = totalRevenue;
        }
        
        public BigDecimal getAverageInvoiceValue() {
            return averageInvoiceValue;
        }
        
        public void setAverageInvoiceValue(BigDecimal averageInvoiceValue) {
            this.averageInvoiceValue = averageInvoiceValue;
        }
        
        public Long getPendingInvoices() {
            return pendingInvoices;
        }
        
        public void setPendingInvoices(Long pendingInvoices) {
            this.pendingInvoices = pendingInvoices;
        }
    }
}
