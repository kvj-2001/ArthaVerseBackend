package com.billing.controller;

import com.billing.dto.ReportDto;
import com.billing.service.ReportService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.time.LocalDate;

@RestController
@RequestMapping("/reports")
@Tag(name = "Reports", description = "Reporting and analytics endpoints")
public class ReportController {
    
    @Autowired
    private ReportService reportService;
    
    @Operation(summary = "Get dashboard report data")
    @GetMapping("/dashboard")
    public ResponseEntity<ReportDto> getDashboardReport(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String customer) {
        
        // Use default date range if not provided (last 30 days)
        if (startDate == null) {
            startDate = LocalDate.now().minusDays(30);
        }
        if (endDate == null) {
            endDate = LocalDate.now();
        }
        
        ReportDto report = reportService.getDashboardReport(startDate, endDate, status, customer);
        return ResponseEntity.ok(report);
    }
    
    @Operation(summary = "Export dashboard report to PDF")
    @GetMapping("/export/pdf")
    public ResponseEntity<byte[]> exportDashboardReportToPdf(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String customer) {
        try {
            // Use default date range if not provided (last 30 days)
            if (startDate == null) {
                startDate = LocalDate.now().minusDays(30);
            }
            if (endDate == null) {
                endDate = LocalDate.now();
            }
            
            ReportDto report = reportService.getDashboardReport(startDate, endDate, status, customer);
            byte[] pdfData = reportService.exportReportToPdf(report);
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_PDF);
            headers.setContentDispositionFormData("attachment", "dashboard-report-" + startDate + "-to-" + endDate + ".pdf");
            
            return new ResponseEntity<>(pdfData, headers, HttpStatus.OK);
        } catch (IOException e) {
            return ResponseEntity.internalServerError().build();
        }
    }
    
    @Operation(summary = "Export dashboard report to Excel")
    @GetMapping("/export/excel")
    public ResponseEntity<byte[]> exportDashboardReportToExcel(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String customer) {
        try {
            // Use default date range if not provided (last 30 days)
            if (startDate == null) {
                startDate = LocalDate.now().minusDays(30);
            }
            if (endDate == null) {
                endDate = LocalDate.now();
            }
            
            ReportDto report = reportService.getDashboardReport(startDate, endDate, status, customer);
            byte[] excelData = reportService.exportReportToExcel(report);
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
            headers.setContentDispositionFormData("attachment", "dashboard-report-" + startDate + "-to-" + endDate + ".xlsx");
            
            return new ResponseEntity<>(excelData, headers, HttpStatus.OK);
        } catch (IOException e) {
            return ResponseEntity.internalServerError().build();
        }
    }
    
    @Operation(summary = "Get daily sales report")
    @GetMapping("/daily/{date}")
    public ResponseEntity<ReportDto> getDailySalesReport(
            @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        ReportDto report = reportService.getDailySalesReport(date);
        return ResponseEntity.ok(report);
    }
    
    @Operation(summary = "Get monthly sales report")
    @GetMapping("/monthly/{year}/{month}")
    public ResponseEntity<ReportDto> getMonthlySalesReport(
            @PathVariable int year, 
            @PathVariable int month) {
        ReportDto report = reportService.getMonthlySalesReport(year, month);
        return ResponseEntity.ok(report);
    }
    
    @Operation(summary = "Get yearly sales report")
    @GetMapping("/yearly/{year}")
    public ResponseEntity<ReportDto> getYearlySalesReport(@PathVariable int year) {
        ReportDto report = reportService.getYearlySalesReport(year);
        return ResponseEntity.ok(report);
    }
    
    @Operation(summary = "Get custom date range report")
    @GetMapping("/custom")
    public ResponseEntity<ReportDto> getCustomDateRangeReport(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        ReportDto report = reportService.getCustomDateRangeReport(startDate, endDate);
        return ResponseEntity.ok(report);
    }
    
    @Operation(summary = "Get product performance report")
    @GetMapping("/product-performance")
    public ResponseEntity<ReportDto> getProductPerformanceReport() {
        ReportDto report = reportService.getProductPerformanceReport();
        return ResponseEntity.ok(report);
    }
    
    @Operation(summary = "Export daily report to Excel")
    @GetMapping("/daily/{date}/excel")
    public ResponseEntity<byte[]> exportDailyReportToExcel(
            @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        try {
            ReportDto report = reportService.getDailySalesReport(date);
            byte[] excelData = reportService.exportReportToExcel(report);
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
            headers.setContentDispositionFormData("attachment", "daily-report-" + date + ".xlsx");
            
            return new ResponseEntity<>(excelData, headers, HttpStatus.OK);
        } catch (IOException e) {
            return ResponseEntity.internalServerError().build();
        }
    }
    
    @Operation(summary = "Export monthly report to Excel")
    @GetMapping("/monthly/{year}/{month}/excel")
    public ResponseEntity<byte[]> exportMonthlyReportToExcel(
            @PathVariable int year, 
            @PathVariable int month) {
        try {
            ReportDto report = reportService.getMonthlySalesReport(year, month);
            byte[] excelData = reportService.exportReportToExcel(report);
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
            headers.setContentDispositionFormData("attachment", "monthly-report-" + year + "-" + month + ".xlsx");
            
            return new ResponseEntity<>(excelData, headers, HttpStatus.OK);
        } catch (IOException e) {
            return ResponseEntity.internalServerError().build();
        }
    }
    
    @Operation(summary = "Export yearly report to Excel")
    @GetMapping("/yearly/{year}/excel")
    public ResponseEntity<byte[]> exportYearlyReportToExcel(@PathVariable int year) {
        try {
            ReportDto report = reportService.getYearlySalesReport(year);
            byte[] excelData = reportService.exportReportToExcel(report);
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
            headers.setContentDispositionFormData("attachment", "yearly-report-" + year + ".xlsx");
            
            return new ResponseEntity<>(excelData, headers, HttpStatus.OK);
        } catch (IOException e) {
            return ResponseEntity.internalServerError().build();
        }
    }
    
    @Operation(summary = "Export custom range report to Excel")
    @GetMapping("/custom/excel")
    public ResponseEntity<byte[]> exportCustomRangeReportToExcel(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        try {
            ReportDto report = reportService.getCustomDateRangeReport(startDate, endDate);
            byte[] excelData = reportService.exportReportToExcel(report);
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
            headers.setContentDispositionFormData("attachment", "custom-report-" + startDate + "-to-" + endDate + ".xlsx");
            
            return new ResponseEntity<>(excelData, headers, HttpStatus.OK);
        } catch (IOException e) {
            return ResponseEntity.internalServerError().build();
        }
    }
}
