package com.example.capitalguard;

public class SecuritiesActivityObject implements java.io.Serializable {
    private String name;
    private String symbol;
    private String shares;
    private String price;
    private String totalReturn;
    private String equity;

    public SecuritiesActivityObject(String name, String symbol, String shares, String price,
                                    String totalReturn, String equity) {
        this.name = name;
        this.symbol = symbol;
        this.shares = shares;
        this.price = price;
        this.totalReturn = totalReturn;
        this.equity = equity;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSymbol() {
        return symbol;
    }

    public void setSymbol(String symbol) {
        this.symbol = symbol;
    }

    public String getShares() {
        return shares;
    }

    public void setShares(String shares) {
        this.shares = shares;
    }

    public String getPrice() {
        return price;
    }

    public void setPrice(String price) {
        this.price = price;
    }

    public String getTotalReturn() {
        return totalReturn;
    }

    public void setTotalReturn(String totalReturn) {
        this.totalReturn = totalReturn;
    }

    public String getEquity() {
        return equity;
    }

    public void setEquity(String equity) {
        this.equity = equity;
    }
}
