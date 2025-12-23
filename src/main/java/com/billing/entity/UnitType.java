package com.billing.entity;

public enum UnitType {
    PIECES("pcs", "Pieces", false),
    KILOGRAMS("kg", "Kilograms", true),
    GRAMS("g", "Grams", true),
    LITERS("L", "Liters", true);
    
    private final String code;
    private final String displayName;
    private final boolean allowsDecimal;
    
    UnitType(String code, String displayName, boolean allowsDecimal) {
        this.code = code;
        this.displayName = displayName;
        this.allowsDecimal = allowsDecimal;
    }
    
    public String getCode() {
        return code;
    }
    
    public String getDisplayName() {
        return displayName;
    }
    
    public boolean isAllowsDecimal() {
        return allowsDecimal;
    }
    
    public static UnitType fromString(String unit) {
        for (UnitType unitType : UnitType.values()) {
            if (unitType.name().equalsIgnoreCase(unit) || 
                unitType.code.equalsIgnoreCase(unit) || 
                unitType.displayName.equalsIgnoreCase(unit)) {
                return unitType;
            }
        }
        return PIECES; // Default fallback
    }
    
    public static boolean allowsDecimalQuantity(String unit) {
        return fromString(unit).isAllowsDecimal();
    }
}