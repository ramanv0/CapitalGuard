package com.example.capitalguard;

public class SecurityType implements java.io.Serializable {
    private String type;
    private String quantity;
    private String value;

    public SecurityType(String type, String quantity, String value) {
        this.type = type;
        this.quantity = quantity;
        this.value = value;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getQuantity() {
        return quantity;
    }

    public void setQuantity(String quantity) {
        this.quantity = quantity;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }
}
