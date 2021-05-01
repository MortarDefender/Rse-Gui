package com.rse;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Date;
import objects.UserDTO;

public class Deal  implements Serializable {
    private final boolean action;  // true == Buy || false == Sell
    private final String symbol, time;
    private int amount, revolution;
    private final int rate;
    private User publisher;

    public Deal(String symbol, boolean action, int amount, int rate, User publisher) {  // **user
        this(symbol, action, amount, rate, publisher, new SimpleDateFormat("HH:mm:ss:SSS").format(new Date()));
    }

    public Deal(String symbol, boolean action, int amount, int rate, User publisher, String time) {  // String publisher // **user
        this.time = time;
        this.symbol = symbol;
        this.action = action;
        this.amount = amount;
        this.rate = rate;
        this.publisher = publisher; // **user
        this.revolution = 0;
    }

    public String getSymbol() { return symbol; }
    public boolean getAction() { return action; }
    public String getActionString() { return action ? "Buy" : "Sell"; }
    public int getAmount() { return amount; }
    public String getTime() { return time; }
    public int getRate() { return rate; }
    public int getRevolution() { return revolution; }
    public String getPublisherName() { return publisher.getUsername(); }
    public User getPublisher() { return publisher; }
    public UserDTO getPublisherDTO() { return new UserDTO(publisher.getUsername(), publisher.getTypeString(), publisher.getAccount(), publisher.getStocksDTO(), publisher.getTransactionsDTO(), publisher.getTotalRevolution()); }

    public void setAmount(int amount) {
        if (amount >= 0)
            this.amount = amount;
    }
    public void setRevolution(int revolution) { this.revolution = revolution; }

    public String print() {
        return "Time: " + time + "\tAmount: " + amount + "\tRate: " + rate + "\tPublisher: " + publisher.getUsername() + "\tTotal transaction value: " + revolution;
    }

    @Override
    public String toString() {
        return "Symbol: " + symbol + "\tType: " + (action ? "Buy" : "Sell") + "\tAmount: " + amount +
                "\tRate: " + rate + "\tTime: " + time + "\tPublisher: " + publisher.getUsername() + "\tRevolution: " + revolution;
    }
}
