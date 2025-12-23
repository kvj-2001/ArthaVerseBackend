package com.billing.validation;

import com.billing.dto.InvoiceItemDto;
import com.billing.entity.UnitType;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import java.math.BigDecimal;

public class QuantityValidator implements ConstraintValidator<ValidQuantity, InvoiceItemDto> {
    
    @Override
    public void initialize(ValidQuantity constraintAnnotation) {
    }
    
    @Override
    public boolean isValid(InvoiceItemDto invoiceItem, ConstraintValidatorContext context) {
        if (invoiceItem == null || invoiceItem.getQuantity() == null || invoiceItem.getProductUnit() == null) {
            return true; // Let other validators handle null checks
        }
        
        BigDecimal quantity = invoiceItem.getQuantity();
        String unit = invoiceItem.getProductUnit();
        
        try {
            UnitType unitType = UnitType.fromString(unit);
            
            // If unit is PIECES, quantity should be a whole number
            if (!unitType.isAllowsDecimal()) {
                if (quantity.stripTrailingZeros().scale() > 0) {
                    context.disableDefaultConstraintViolation();
                    context.buildConstraintViolationWithTemplate(
                        "Quantity for " + unitType.getDisplayName() + " must be a whole number"
                    ).addPropertyNode("quantity").addConstraintViolation();
                    return false;
                }
            }
            
            return true;
        } catch (Exception e) {
            // If unit type is not recognized, allow validation to pass
            // and let other business logic handle it
            return true;
        }
    }
}