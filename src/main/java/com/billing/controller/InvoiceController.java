package com.billing.controller;

import com.billing.dto.InvoiceDto;
import com.billing.entity.Invoice;
import com.billing.service.InvoiceService;
import com.billing.service.PdfService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/invoices")
@Tag(name = "Invoices", description = "Invoice management endpoints")
public class InvoiceController {
    
    @Autowired
    private InvoiceService invoiceService;
    
    @Autowired
    private PdfService pdfService;
    
    @Operation(summary = "Create a new invoice")
    @PostMapping
    public ResponseEntity<InvoiceDto> createInvoice(@Valid @RequestBody InvoiceDto invoiceDto) {
        InvoiceDto createdInvoice = invoiceService.createInvoice(invoiceDto);
        return new ResponseEntity<>(createdInvoice, HttpStatus.CREATED);
    }
    
    @Operation(summary = "Update an existing invoice")
    @PutMapping("/{id}")
    public ResponseEntity<InvoiceDto> updateInvoice(@PathVariable Long id, @Valid @RequestBody InvoiceDto invoiceDto) {
        InvoiceDto updatedInvoice = invoiceService.updateInvoice(id, invoiceDto);
        return ResponseEntity.ok(updatedInvoice);
    }
    
    @Operation(summary = "Delete an invoice")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteInvoice(@PathVariable Long id) {
        invoiceService.deleteInvoice(id);
        return ResponseEntity.noContent().build();
    }
    
    @Operation(summary = "Get an invoice by ID")
    @GetMapping("/{id}")
    public ResponseEntity<InvoiceDto> getInvoice(@PathVariable Long id) {
        InvoiceDto invoice = invoiceService.getInvoice(id);
        return ResponseEntity.ok(invoice);
    }
    
    @Operation(summary = "Get all invoices with pagination")
    @GetMapping
    public ResponseEntity<Page<InvoiceDto>> getAllInvoices(Pageable pageable) {
        Page<InvoiceDto> invoices = invoiceService.getAllInvoices(pageable);
        return ResponseEntity.ok(invoices);
    }
    
    @Operation(summary = "Search invoices by keyword")
    @GetMapping("/search")
    public ResponseEntity<Page<InvoiceDto>> searchInvoices(@RequestParam String keyword, Pageable pageable) {
        Page<InvoiceDto> invoices = invoiceService.searchInvoices(keyword, pageable);
        return ResponseEntity.ok(invoices);
    }
    
    @Operation(summary = "Get invoices by status")
    @GetMapping("/status/{status}")
    public ResponseEntity<List<InvoiceDto>> getInvoicesByStatus(@PathVariable Invoice.InvoiceStatus status) {
        List<InvoiceDto> invoices = invoiceService.getInvoicesByStatus(status);
        return ResponseEntity.ok(invoices);
    }
    
    @Operation(summary = "Get overdue invoices")
    @GetMapping("/overdue")
    public ResponseEntity<List<InvoiceDto>> getOverdueInvoices() {
        List<InvoiceDto> invoices = invoiceService.getOverdueInvoices();
        return ResponseEntity.ok(invoices);
    }
    
    @Operation(summary = "Update invoice status")
    @PatchMapping("/{id}/status")
    public ResponseEntity<InvoiceDto> updateInvoiceStatus(@PathVariable Long id, @RequestParam Invoice.InvoiceStatus status) {
        InvoiceDto updatedInvoice = invoiceService.updateInvoiceStatus(id, status);
        return ResponseEntity.ok(updatedInvoice);
    }
    
    @Operation(summary = "Generate and download invoice PDF")
    @GetMapping("/{id}/pdf")
    public ResponseEntity<byte[]> generateInvoicePdf(@PathVariable Long id) {
        try {
            byte[] pdfData = invoiceService.generateInvoicePdf(id);
            
            InvoiceDto invoiceDto = invoiceService.getInvoice(id);
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_PDF);
            headers.setContentDispositionFormData("attachment", "invoice-" + invoiceDto.getInvoiceNumber() + ".pdf");
            
            return new ResponseEntity<>(pdfData, headers, HttpStatus.OK);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }
}
