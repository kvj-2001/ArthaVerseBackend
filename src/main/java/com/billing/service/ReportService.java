package com.billing.service;

import com.billing.dto.ReportDto;
import com.billing.repository.InvoiceItemRepository;
import com.billing.repository.InvoiceRepository;
import com.billing.repository.ProductRepository;
import com.billing.security.UserPrincipal;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class ReportService {
    
    private static final Logger logger = LoggerFactory.getLogger(ReportService.class);
    
    @Autowired
    private InvoiceRepository invoiceRepository;
    
    @Autowired
    private InvoiceItemRepository invoiceItemRepository;
    
    @Autowired
    private ProductRepository productRepository;
    
    private Long getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof UserPrincipal) {
            return ((UserPrincipal) authentication.getPrincipal()).getId();
        }
        throw new RuntimeException("User not authenticated");
    }
    
    public ReportDto getDailySalesReport(LocalDate date) {
        Long userId = getCurrentUserId();
        
        LocalDate startDate = date;
        LocalDate endDate = date;
        
        BigDecimal totalRevenue = invoiceRepository.getRevenueByDateRange(userId, startDate, endDate);
        if (totalRevenue == null) totalRevenue = BigDecimal.ZERO;
        
        ReportDto report = new ReportDto();
        report.setReportType("Daily Sales Report");
        report.setStartDate(startDate);
        report.setEndDate(endDate);
        report.setTotalRevenue(totalRevenue);
        
        Map<String, Object> additionalData = new HashMap<>();
        additionalData.put("date", date.toString());
        report.setAdditionalData(additionalData);
        
        logger.info("Daily sales report generated for date: {} by user: {}", date, userId);
        
        return report;
    }
    
    public ReportDto getMonthlySalesReport(int year, int month) {
        Long userId = getCurrentUserId();
        
        YearMonth yearMonth = YearMonth.of(year, month);
        LocalDate startDate = yearMonth.atDay(1);
        LocalDate endDate = yearMonth.atEndOfMonth();
        
        BigDecimal totalRevenue = invoiceRepository.getRevenueByDateRange(userId, startDate, endDate);
        if (totalRevenue == null) totalRevenue = BigDecimal.ZERO;
        
        ReportDto report = new ReportDto();
        report.setReportType("Monthly Sales Report");
        report.setStartDate(startDate);
        report.setEndDate(endDate);
        report.setTotalRevenue(totalRevenue);
        
        Map<String, Object> additionalData = new HashMap<>();
        additionalData.put("year", year);
        additionalData.put("month", month);
        additionalData.put("monthName", yearMonth.getMonth().name());
        report.setAdditionalData(additionalData);
        
        logger.info("Monthly sales report generated for {}-{} by user: {}", year, month, userId);
        
        return report;
    }
    
    public ReportDto getYearlySalesReport(int year) {
        Long userId = getCurrentUserId();
        
        LocalDate startDate = LocalDate.of(year, 1, 1);
        LocalDate endDate = LocalDate.of(year, 12, 31);
        
        BigDecimal totalRevenue = invoiceRepository.getRevenueByDateRange(userId, startDate, endDate);
        if (totalRevenue == null) totalRevenue = BigDecimal.ZERO;
        
        // Get monthly breakdown
        List<Object[]> monthlyData = invoiceRepository.getMonthlyRevenue(userId);
        
        ReportDto report = new ReportDto();
        report.setReportType("Yearly Sales Report");
        report.setStartDate(startDate);
        report.setEndDate(endDate);
        report.setTotalRevenue(totalRevenue);
        
        Map<String, Object> additionalData = new HashMap<>();
        additionalData.put("year", year);
        additionalData.put("monthlyBreakdown", monthlyData);
        report.setAdditionalData(additionalData);
        
        logger.info("Yearly sales report generated for {} by user: {}", year, userId);
        
        return report;
    }
    
    public ReportDto getCustomDateRangeReport(LocalDate startDate, LocalDate endDate) {
        Long userId = getCurrentUserId();
        
        BigDecimal totalRevenue = invoiceRepository.getRevenueByDateRange(userId, startDate, endDate);
        if (totalRevenue == null) totalRevenue = BigDecimal.ZERO;
        
        ReportDto report = new ReportDto();
        report.setReportType("Custom Date Range Report");
        report.setStartDate(startDate);
        report.setEndDate(endDate);
        report.setTotalRevenue(totalRevenue);
        
        logger.info("Custom date range report generated for {} to {} by user: {}", 
                   startDate, endDate, userId);
        
        return report;
    }
    
    public ReportDto getProductPerformanceReport() {
        Long userId = getCurrentUserId();
        
        List<Object[]> productPerformance = invoiceItemRepository.getProductPerformance(userId);
        
        ReportDto report = new ReportDto();
        report.setReportType("Product Performance Report");
        report.setTotalRevenue(BigDecimal.ZERO);
        
        Map<String, Object> additionalData = new HashMap<>();
        additionalData.put("productPerformance", productPerformance);
        report.setAdditionalData(additionalData);
        
        logger.info("Product performance report generated by user: {}", userId);
        
        return report;
    }
    
    public ReportDto getDashboardReport(LocalDate startDate, LocalDate endDate, String status, String customer) {
        Long userId = getCurrentUserId();
        
        BigDecimal totalRevenue = invoiceRepository.getRevenueByDateRange(userId, startDate, endDate);
        if (totalRevenue == null) totalRevenue = BigDecimal.ZERO;
        
        // Get invoice counts by status
        Map<String, Long> statusCounts = new HashMap<>();
        List<Object[]> statusData = invoiceRepository.getInvoiceCountsByStatus(userId, startDate, endDate);
        for (Object[] row : statusData) {
            // row[0] is InvoiceStatus enum, convert to string
            String statusName = row[0].toString();
            statusCounts.put(statusName, ((Number) row[1]).longValue());
        }
        
        // Get top products
        List<Object[]> topProductsData = invoiceItemRepository.getTopSellingProducts(userId, startDate, endDate, 5);
        
        ReportDto report = new ReportDto();
        report.setReportType("Dashboard Report");
        report.setStartDate(startDate);
        report.setEndDate(endDate);
        report.setTotalRevenue(totalRevenue);
        
        // Create status distribution list
        List<ReportDto.StatusDistributionItem> statusDistribution = new ArrayList<>();
        for (Map.Entry<String, Long> entry : statusCounts.entrySet()) {
            statusDistribution.add(new ReportDto.StatusDistributionItem(entry.getKey(), entry.getValue()));
        }
        report.setStatusDistribution(statusDistribution);
        
        // Create top products list
        List<ReportDto.TopProductItem> topProducts = new ArrayList<>();
        for (Object[] row : topProductsData) {
            String productName = (String) row[0];
            Long quantity = ((Number) row[1]).longValue();
            BigDecimal revenue = (BigDecimal) row[2];
            topProducts.add(new ReportDto.TopProductItem(productName, revenue, quantity));
        }
        report.setTopProducts(topProducts);
        
        // Create sales trend (daily data for the period)
        List<ReportDto.SalesTrendItem> salesTrend = new ArrayList<>();
        List<Object[]> dailyRevenue = invoiceRepository.getDailyRevenue(userId, startDate, endDate);
        for (Object[] row : dailyRevenue) {
            LocalDate date = (LocalDate) row[0];
            BigDecimal amount = (BigDecimal) row[1];
            if (amount == null) amount = BigDecimal.ZERO;
            salesTrend.add(new ReportDto.SalesTrendItem(date.toString(), amount));
        }
        report.setSalesTrend(salesTrend);
        
        Map<String, Object> additionalData = new HashMap<>();
        additionalData.put("statusCounts", statusCounts);
        additionalData.put("totalInvoices", statusCounts.values().stream().mapToLong(Long::longValue).sum());
        
        // Filter by customer if provided
        if (customer != null && !customer.trim().isEmpty()) {
            additionalData.put("customerFilter", customer);
        }
        
        // Filter by status if provided
        if (status != null && !status.trim().isEmpty()) {
            additionalData.put("statusFilter", status);
        }
        
        report.setAdditionalData(additionalData);
        
        // Create summary object for frontend
        Long totalInvoices = statusCounts.values().stream().mapToLong(Long::longValue).sum();
        Long pendingInvoices = statusCounts.getOrDefault("PENDING", 0L);
        BigDecimal averageInvoiceValue = totalInvoices > 0 ? totalRevenue.divide(BigDecimal.valueOf(totalInvoices), 2, java.math.RoundingMode.HALF_UP) : BigDecimal.ZERO;
        
        ReportDto.Summary summary = new ReportDto.Summary(totalInvoices, totalRevenue, averageInvoiceValue, pendingInvoices);
        report.setSummary(summary);
        
        logger.info("Dashboard report generated for period: {} to {} by user: {}", startDate, endDate, userId);
        
        return report;
    }
    
    public byte[] exportReportToPdf(ReportDto report) throws IOException {
        // For now, return a simple message - you can implement full PDF generation later
        // This requires additional PDF libraries and complex formatting
        String content = "PDF Export for " + report.getReportType() + "\n" +
                        "Period: " + report.getStartDate() + " to " + report.getEndDate() + "\n" +
                        "Total Revenue: $" + report.getTotalRevenue();
        
        logger.info("Report exported to PDF: {}", report.getReportType());
        
        return content.getBytes();
    }
    
    public byte[] exportReportToExcel(ReportDto report) throws IOException {
        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet(report.getReportType());
            
            // Create header style
            CellStyle headerStyle = workbook.createCellStyle();
            Font headerFont = workbook.createFont();
            headerFont.setBold(true);
            headerStyle.setFont(headerFont);
            
            int rowNum = 0;
            
            // Report title
            Row titleRow = sheet.createRow(rowNum++);
            Cell titleCell = titleRow.createCell(0);
            titleCell.setCellValue(report.getReportType());
            titleCell.setCellStyle(headerStyle);
            
            // Report period
            if (report.getStartDate() != null && report.getEndDate() != null) {
                Row periodRow = sheet.createRow(rowNum++);
                periodRow.createCell(0).setCellValue("Period:");
                periodRow.createCell(1).setCellValue(report.getStartDate().toString() + " to " + report.getEndDate().toString());
            }
            
            // Total revenue
            Row revenueRow = sheet.createRow(rowNum++);
            revenueRow.createCell(0).setCellValue("Total Revenue:");
            revenueRow.createCell(1).setCellValue(report.getTotalRevenue().doubleValue());
            
            // Empty row
            rowNum++;
            
            // Additional data
            if (report.getAdditionalData() != null) {
                for (Map.Entry<String, Object> entry : report.getAdditionalData().entrySet()) {
                    Row dataRow = sheet.createRow(rowNum++);
                    dataRow.createCell(0).setCellValue(entry.getKey());
                    dataRow.createCell(1).setCellValue(entry.getValue().toString());
                }
            }
            
            // Auto-size columns
            for (int i = 0; i < 5; i++) {
                sheet.autoSizeColumn(i);
            }
            
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            workbook.write(outputStream);
            
            logger.info("Report exported to Excel: {}", report.getReportType());
            
            return outputStream.toByteArray();
        }
    }
}
