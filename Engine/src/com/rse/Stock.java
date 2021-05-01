package com.rse;

import java.io.Serializable;

public class Stock  implements Serializable {
    private String companyName, symbol;
    private int rate, totalDeals, revolution, quantity;
    // private final int quantity;

    public Stock(Stock s) {
        this(s.companyName, s.symbol, s.rate, s.quantity);
        this.setTotalDeals(s.totalDeals);
        this.setRevolution(s.revolution);
    }

    public Stock(String name,String symbol, int rate, int quantity) {
        this.companyName = name;
        this.symbol = symbol.toUpperCase();  // for now the symbol given is transferred and not checked
        this.rate = rate;
        this.totalDeals = 0;
        this.revolution = 0;
        this.quantity = quantity;
    }

    public String getCompanyName() { return this.companyName; }
    public String getSymbol() { return this.symbol; }
    public int getRate() { return this.rate; }
    public int getTotalDeals() { return this.totalDeals; }
    public int getRevolution() { return this.revolution; }
    public int getQuantity() { return this.quantity; }

    public void setCompanyName(String companyName) { this.companyName = companyName; }
    public void setSymbol(String symbol) { this.symbol = symbol.toUpperCase(); }
    public void setQuantity(int quantity) {
        if (quantity >= 0)
            this.quantity = quantity;
    }
    public void setRate(int rate) {
        if (rate >= 0)
            this.rate = rate;
    }
    public void setTotalDeals() { ++this.totalDeals; }
    public void setTotalDeals(int deals) { this.totalDeals = deals; }
    public void setRevolution(int rev) { this.revolution = rev; }
    public void set(Stock s) {
        this.setCompanyName(s.companyName);
        this.setSymbol(s.symbol);
        this.setRate(s.rate);
        this.setTotalDeals(s.totalDeals);
        this.setRevolution(s.revolution);
    }

    public void addRevolution(int rev) { this.revolution += rev; }
    public void addQuantity(int quantity) { this.quantity += quantity; }

    @Override
    public String toString() { // check time limit of the revolution (for now it is all the time from the stock creation)
        return "Company Name: " + companyName + "\nStock Symbol: " + symbol + "\nRate: " + rate + "\nQuantity: " + quantity
                + "\nTotal Deals Made: " + totalDeals + "\nRevolution: " + revolution + "\n";
    }
}
