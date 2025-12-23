package com.billing.service;

import com.billing.entity.Invoice;
import com.billing.entity.InvoiceItem;
import com.billing.entity.Product;
import com.itextpdf.text.*;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.format.DateTimeFormatter;

@Service
public class PdfService {
    
    private static final Logger logger = LoggerFactory.getLogger(PdfService.class);
    
    public byte[] generateInvoicePdf(Invoice invoice) throws DocumentException, IOException {
        Document document = new Document(PageSize.A4);
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        
        PdfWriter.getInstance(document, outputStream);
        document.open();
        
        // Company header
        Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 18, BaseColor.BLACK);
        Font headerFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12, BaseColor.BLACK);
        Font normalFont = FontFactory.getFont(FontFactory.HELVETICA, 10, BaseColor.BLACK);
        
        // Title
        Paragraph title = new Paragraph("INVOICE", titleFont);
        title.setAlignment(Element.ALIGN_CENTER);
        document.add(title);
        document.add(new Paragraph(" "));
        
        // Invoice details table
        PdfPTable headerTable = new PdfPTable(2);
        headerTable.setWidthPercentage(100);
        headerTable.setWidths(new float[]{1, 1});
        
        // Left side - Company info
        PdfPCell leftCell = new PdfPCell();
        leftCell.setBorder(Rectangle.NO_BORDER);
        leftCell.addElement(new Paragraph("BEST FOOD AT LOW PRICES", headerFont));
        leftCell.addElement(new Paragraph("Krishna Nagar Main Road", normalFont));
        leftCell.addElement(new Paragraph("Guntur A.P. 522006", normalFont));
        leftCell.addElement(new Paragraph("Phone: +91-9440262688", normalFont));
        leftCell.addElement(new Paragraph("Email: info@billingcompany.com", normalFont));
        
        // Right side - Invoice info
        PdfPCell rightCell = new PdfPCell();
        rightCell.setBorder(Rectangle.NO_BORDER);
        rightCell.setHorizontalAlignment(Element.ALIGN_RIGHT);
        rightCell.addElement(new Paragraph("Invoice #: " + invoice.getInvoiceNumber(), headerFont));
        rightCell.addElement(new Paragraph("Invoice Date: " + invoice.getInvoiceDate().format(DateTimeFormatter.ofPattern("MM/dd/yyyy")), normalFont));
        if (invoice.getDueDate() != null) {
            rightCell.addElement(new Paragraph("Due Date: " + invoice.getDueDate().format(DateTimeFormatter.ofPattern("MM/dd/yyyy")), normalFont));
        }
        rightCell.addElement(new Paragraph("Status: " + invoice.getStatus().toString(), normalFont));
        
        headerTable.addCell(leftCell);
        headerTable.addCell(rightCell);
        document.add(headerTable);
        document.add(new Paragraph(" "));
        
        // Customer information
        document.add(new Paragraph("Bill To:", headerFont));
        document.add(new Paragraph(invoice.getCustomerName(), normalFont));
        if (invoice.getCustomerEmail() != null) {
            document.add(new Paragraph("Email: " + invoice.getCustomerEmail(), normalFont));
        }
        if (invoice.getCustomerPhone() != null) {
            document.add(new Paragraph("Phone: " + invoice.getCustomerPhone(), normalFont));
        }
        if (invoice.getCustomerAddress() != null) {
            document.add(new Paragraph("Address: " + invoice.getCustomerAddress(), normalFont));
        }
        document.add(new Paragraph(" "));
        
        // Items table
        PdfPTable itemsTable = new PdfPTable(6);
        itemsTable.setWidthPercentage(100);
        itemsTable.setWidths(new float[]{3, 1, 1.5f, 1.5f, 2, 2});
        
        // Table headers
        addTableHeader(itemsTable, "Description", headerFont);
        addTableHeader(itemsTable, "Qty", headerFont);
        addTableHeader(itemsTable, "MRP", headerFont);
        addTableHeader(itemsTable, "Price", headerFont);
        addTableHeader(itemsTable, "Total", headerFont);
        addTableHeader(itemsTable, "Savings", headerFont);
        
        // Table items
        BigDecimal totalSavings = BigDecimal.ZERO;
        for (InvoiceItem item : invoice.getItems()) {
            Product product = item.getProduct();
            BigDecimal mrp = (product != null && product.getMrp() != null) ? product.getMrp() : null;
            BigDecimal savings = BigDecimal.ZERO;
            if (mrp != null && mrp.compareTo(item.getUnitPrice()) > 0) {
                savings = mrp.subtract(item.getUnitPrice()).multiply(item.getQuantity());
                totalSavings = totalSavings.add(savings);
            }
            
            itemsTable.addCell(new PdfPCell(new Phrase(item.getDescription(), normalFont)));
            itemsTable.addCell(new PdfPCell(new Phrase(item.getQuantity().toString(), normalFont)));
            itemsTable.addCell(new PdfPCell(new Phrase(mrp != null ? "₹" + mrp.toString() : "-", normalFont)));
            itemsTable.addCell(new PdfPCell(new Phrase("₹" + item.getUnitPrice().toString(), normalFont)));
            itemsTable.addCell(new PdfPCell(new Phrase("₹" + item.getTotalPrice().toString(), normalFont)));
            
            PdfPCell savingsCell = new PdfPCell(new Phrase(
                savings.compareTo(BigDecimal.ZERO) > 0 ? "₹" + savings.setScale(2, BigDecimal.ROUND_HALF_UP).toString() : "-", 
                normalFont
            ));
            if (savings.compareTo(BigDecimal.ZERO) > 0) {
                savingsCell.setBackgroundColor(new BaseColor(230, 255, 230));
            }
            itemsTable.addCell(savingsCell);
        }
        
        document.add(itemsTable);
        document.add(new Paragraph(" "));
        
        // Totals table
        PdfPTable totalsTable = new PdfPTable(2);
        totalsTable.setWidthPercentage(50);
        totalsTable.setHorizontalAlignment(Element.ALIGN_RIGHT);
        
        addTotalRow(totalsTable, "Subtotal:", "₹" + invoice.getSubtotal().toString(), normalFont, headerFont);
        if (totalSavings.compareTo(BigDecimal.ZERO) > 0) {
            Font savingsFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12, BaseColor.GREEN);
            addTotalRow(totalsTable, "You Saved:", "₹" + totalSavings.setScale(2, BigDecimal.ROUND_HALF_UP).toString(), savingsFont, savingsFont);
        }
        if (invoice.getDiscountAmount().compareTo(BigDecimal.ZERO) > 0) {
            addTotalRow(totalsTable, "Discount:", "-₹" + invoice.getDiscountAmount().toString(), normalFont, headerFont);
        }
        if (invoice.getTaxAmount().compareTo(BigDecimal.ZERO) > 0) {
            addTotalRow(totalsTable, "Tax:", "₹" + invoice.getTaxAmount().toString(), normalFont, headerFont);
        }
        addTotalRow(totalsTable, "Total:", "₹" + invoice.getTotalAmount().toString(), headerFont, headerFont);
        
        document.add(totalsTable);
        
        // Notes
        if (invoice.getNotes() != null && !invoice.getNotes().isEmpty()) {
            document.add(new Paragraph(" "));
            document.add(new Paragraph("Notes:", headerFont));
            document.add(new Paragraph(invoice.getNotes(), normalFont));
        }
        
        // Footer
        document.add(new Paragraph(" "));
        document.add(new Paragraph(" "));
        Paragraph footer = new Paragraph("Thank you for your business!", normalFont);
        footer.setAlignment(Element.ALIGN_CENTER);
        document.add(footer);
        
        document.close();
        
        logger.info("PDF generated for invoice: {}", invoice.getInvoiceNumber());
        
        return outputStream.toByteArray();
    }
    
    private void addTableHeader(PdfPTable table, String text, Font font) {
        PdfPCell cell = new PdfPCell(new Phrase(text, font));
        cell.setBackgroundColor(BaseColor.LIGHT_GRAY);
        cell.setHorizontalAlignment(Element.ALIGN_CENTER);
        cell.setPadding(5);
        table.addCell(cell);
    }
    
    private void addTotalRow(PdfPTable table, String label, String value, Font labelFont, Font valueFont) {
        PdfPCell labelCell = new PdfPCell(new Phrase(label, labelFont));
        labelCell.setBorder(Rectangle.NO_BORDER);
        labelCell.setHorizontalAlignment(Element.ALIGN_RIGHT);
        labelCell.setPadding(5);
        
        PdfPCell valueCell = new PdfPCell(new Phrase(value, valueFont));
        valueCell.setBorder(Rectangle.NO_BORDER);
        valueCell.setHorizontalAlignment(Element.ALIGN_RIGHT);
        valueCell.setPadding(5);
        
        table.addCell(labelCell);
        table.addCell(valueCell);
    }
}
