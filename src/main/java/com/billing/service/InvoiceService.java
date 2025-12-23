package com.billing.service;

import com.billing.dto.InvoiceDto;
import com.billing.dto.InvoiceItemDto;
import com.billing.entity.Invoice;
import com.billing.entity.InvoiceItem;
import com.billing.entity.Product;
import com.billing.exception.BadRequestException;
import com.billing.exception.ResourceNotFoundException;
import com.billing.repository.InvoiceRepository;
import com.billing.repository.ProductRepository;
import com.billing.security.UserPrincipal;
import org.modelmapper.ModelMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.Year;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class InvoiceService {
    
    private static final Logger logger = LoggerFactory.getLogger(InvoiceService.class);
    
    @Autowired
    private InvoiceRepository invoiceRepository;
    
    @Autowired
    private ProductRepository productRepository;
    
    @Autowired
    private ModelMapper modelMapper;
    
    @Autowired
    private PdfService pdfService;
    
    private Long getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof UserPrincipal) {
            return ((UserPrincipal) authentication.getPrincipal()).getId();
        }
        throw new RuntimeException("User not authenticated");
    }
    
    public InvoiceDto createInvoice(InvoiceDto invoiceDto) {
        Long userId = getCurrentUserId();
        
        Invoice invoice = new Invoice();
        invoice.setInvoiceNumber(generateInvoiceNumber(userId));
        invoice.setInvoiceDate(invoiceDto.getInvoiceDate());
        invoice.setDueDate(invoiceDto.getDueDate());
        invoice.setCustomerName(invoiceDto.getCustomerName());
        invoice.setCustomerEmail(invoiceDto.getCustomerEmail());
        invoice.setCustomerPhone(invoiceDto.getCustomerPhone());
        invoice.setCustomerAddress(invoiceDto.getCustomerAddress());
        invoice.setTaxAmount(invoiceDto.getTaxAmount());
        invoice.setDiscountAmount(invoiceDto.getDiscountAmount());
        invoice.setNotes(invoiceDto.getNotes());
        invoice.setStatus(invoiceDto.getStatus());
        invoice.setUserId(userId);
        
        // Process invoice items
        List<InvoiceItem> invoiceItems = new ArrayList<>();
        for (InvoiceItemDto itemDto : invoiceDto.getItems()) {
            Product product = productRepository.findById(itemDto.getProductId())
                .orElseThrow(() -> new ResourceNotFoundException("Product", "id", itemDto.getProductId()));
            
            if (!product.getUserId().equals(userId)) {
                throw new BadRequestException("Product does not belong to the current user");
            }
            
            // Allow invoice creation even with zero stock - stock validation removed
            // Note: Product can have negative stock after this operation
            
            InvoiceItem item = new InvoiceItem();
            item.setProduct(product);
            item.setQuantity(itemDto.getQuantity());
            item.setUnitPrice(itemDto.getUnitPrice());
            item.setDescription(itemDto.getDescription());
            item.setInvoice(invoice);
            
            invoiceItems.add(item);
            
            // Update product quantity (convert BigDecimal to Integer for storage)
            int newQuantity = BigDecimal.valueOf(product.getQuantity()).subtract(itemDto.getQuantity()).intValue();
            product.setQuantity(newQuantity);
            productRepository.save(product);
        }
        
        invoice.setItems(invoiceItems);
        invoice.calculateTotals();
        
        Invoice savedInvoice = invoiceRepository.save(invoice);
        
        logger.info("Invoice created: {} by user: {}", savedInvoice.getInvoiceNumber(), userId);
        
        return convertToDto(savedInvoice);
    }
    
    public InvoiceDto updateInvoice(Long id, InvoiceDto invoiceDto) {
        Long userId = getCurrentUserId();
        
        Invoice invoice = invoiceRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Invoice", "id", id));
        
        if (!invoice.getUserId().equals(userId)) {
            throw new BadRequestException("You don't have permission to update this invoice");
        }
        
        if (invoice.getStatus() == Invoice.InvoiceStatus.PAID) {
            throw new BadRequestException("Cannot update a paid invoice");
        }
        
        // Restore product quantities from existing items
        for (InvoiceItem existingItem : invoice.getItems()) {
            Product product = existingItem.getProduct();
            int newQuantity = BigDecimal.valueOf(product.getQuantity()).add(existingItem.getQuantity()).intValue();
            product.setQuantity(newQuantity);
            productRepository.save(product);
        }
        
        // Clear existing items
        invoice.getItems().clear();
        
        // Update invoice details
        invoice.setInvoiceDate(invoiceDto.getInvoiceDate());
        invoice.setDueDate(invoiceDto.getDueDate());
        invoice.setCustomerName(invoiceDto.getCustomerName());
        invoice.setCustomerEmail(invoiceDto.getCustomerEmail());
        invoice.setCustomerPhone(invoiceDto.getCustomerPhone());
        invoice.setCustomerAddress(invoiceDto.getCustomerAddress());
        invoice.setTaxAmount(invoiceDto.getTaxAmount());
        invoice.setDiscountAmount(invoiceDto.getDiscountAmount());
        invoice.setNotes(invoiceDto.getNotes());
        invoice.setStatus(invoiceDto.getStatus());
        
        // Process new invoice items
        for (InvoiceItemDto itemDto : invoiceDto.getItems()) {
            Product product = productRepository.findById(itemDto.getProductId())
                .orElseThrow(() -> new ResourceNotFoundException("Product", "id", itemDto.getProductId()));
            
            if (!product.getUserId().equals(userId)) {
                throw new BadRequestException("Product does not belong to the current user");
            }
            
            // Allow invoice update even with zero stock - stock validation removed
            // Note: Product can have negative stock after this operation
            
            InvoiceItem item = new InvoiceItem();
            item.setProduct(product);
            item.setQuantity(itemDto.getQuantity());
            item.setUnitPrice(itemDto.getUnitPrice());
            item.setDescription(itemDto.getDescription());
            item.setInvoice(invoice);
            
            invoice.getItems().add(item);
            
            // Update product quantity (convert BigDecimal to Integer for storage)
            int newQuantity = BigDecimal.valueOf(product.getQuantity()).subtract(itemDto.getQuantity()).intValue();
            product.setQuantity(newQuantity);
            productRepository.save(product);
        }
        
        invoice.calculateTotals();
        
        Invoice updatedInvoice = invoiceRepository.save(invoice);
        
        logger.info("Invoice updated: {} by user: {}", updatedInvoice.getInvoiceNumber(), userId);
        
        return convertToDto(updatedInvoice);
    }
    
    public void deleteInvoice(Long id) {
        Long userId = getCurrentUserId();
        
        Invoice invoice = invoiceRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Invoice", "id", id));
        
        if (!invoice.getUserId().equals(userId)) {
            throw new BadRequestException("You don't have permission to delete this invoice");
        }
        
        if (invoice.getStatus() == Invoice.InvoiceStatus.PAID) {
            throw new BadRequestException("Cannot delete a paid invoice");
        }
        
        // Restore product quantities
        for (InvoiceItem item : invoice.getItems()) {
            Product product = item.getProduct();
            int newQuantity = BigDecimal.valueOf(product.getQuantity()).add(item.getQuantity()).intValue();
            product.setQuantity(newQuantity);
            productRepository.save(product);
        }
        
        invoiceRepository.delete(invoice);
        
        logger.info("Invoice deleted: {} by user: {}", invoice.getInvoiceNumber(), userId);
    }
    
    public InvoiceDto getInvoice(Long id) {
        Long userId = getCurrentUserId();
        
        Invoice invoice = invoiceRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Invoice", "id", id));
        
        if (!invoice.getUserId().equals(userId)) {
            throw new BadRequestException("You don't have permission to access this invoice");
        }
        
        return convertToDto(invoice);
    }
    
    public Page<InvoiceDto> getAllInvoices(Pageable pageable) {
        Long userId = getCurrentUserId();
        
        Page<Invoice> invoices = invoiceRepository.findByUserId(userId, pageable);
        
        return invoices.map(this::convertToDto);
    }
    
    public Page<InvoiceDto> searchInvoices(String keyword, Pageable pageable) {
        Long userId = getCurrentUserId();
        
        Page<Invoice> invoices = invoiceRepository.searchInvoices(userId, keyword, pageable);
        
        return invoices.map(this::convertToDto);
    }
    
    public List<InvoiceDto> getInvoicesByStatus(Invoice.InvoiceStatus status) {
        Long userId = getCurrentUserId();
        
        List<Invoice> invoices = invoiceRepository.findByUserIdAndStatus(userId, status);
        
        return invoices.stream()
            .map(this::convertToDto)
            .collect(Collectors.toList());
    }
    
    public List<InvoiceDto> getOverdueInvoices() {
        Long userId = getCurrentUserId();
        
        List<Invoice> invoices = invoiceRepository.findOverdueInvoices(userId, LocalDate.now());
        
        return invoices.stream()
            .map(this::convertToDto)
            .collect(Collectors.toList());
    }
    
    public InvoiceDto updateInvoiceStatus(Long id, Invoice.InvoiceStatus status) {
        Long userId = getCurrentUserId();
        
        Invoice invoice = invoiceRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Invoice", "id", id));
        
        if (!invoice.getUserId().equals(userId)) {
            throw new BadRequestException("You don't have permission to update this invoice");
        }
        
        invoice.setStatus(status);
        Invoice updatedInvoice = invoiceRepository.save(invoice);
        
        logger.info("Invoice status updated: {} to {} by user: {}", 
                   updatedInvoice.getInvoiceNumber(), status, userId);
        
        return convertToDto(updatedInvoice);
    }
    
    public byte[] generateInvoicePdf(Long invoiceId) {
        Long userId = getCurrentUserId();
        
        Invoice invoice = invoiceRepository.findById(invoiceId)
            .orElseThrow(() -> new ResourceNotFoundException("Invoice", "id", invoiceId));
        
        if (!invoice.getUserId().equals(userId)) {
            throw new BadRequestException("You don't have permission to generate PDF for this invoice");
        }
        
        try {
            return pdfService.generateInvoicePdf(invoice);
        } catch (Exception e) {
            logger.error("Error generating PDF for invoice {}: {}", invoiceId, e.getMessage());
            throw new RuntimeException("Failed to generate PDF: " + e.getMessage(), e);
        }
    }
    
    private String generateInvoiceNumber(Long userId) {
        String year = String.valueOf(Year.now().getValue());
        Long count = invoiceRepository.countByUserId(userId);
        return String.format("INV-%s-%s-%06d", userId, year, count + 1);
    }
    
    private InvoiceDto convertToDto(Invoice invoice) {
        InvoiceDto dto = modelMapper.map(invoice, InvoiceDto.class);
        
        List<InvoiceItemDto> itemDtos = invoice.getItems().stream()
            .map(item -> {
                InvoiceItemDto itemDto = modelMapper.map(item, InvoiceItemDto.class);
                itemDto.setProductId(item.getProduct().getId());
                itemDto.setProductName(item.getProduct().getName());
                itemDto.setProductCode(item.getProduct().getCode());
                itemDto.setProductUnit(item.getProduct().getUnit().name());
                return itemDto;
            })
            .collect(Collectors.toList());
        
        dto.setItems(itemDtos);
        
        return dto;
    }
}
