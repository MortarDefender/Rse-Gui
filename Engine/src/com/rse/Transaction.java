package com.rse;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Date;

public class Transaction implements Serializable {
    private final int transactionFee, accountBalance, actionType;      // 0 == Buy || 1 == Sell || 2 = Self Charge
    private final String symbol, time;

    public Transaction(int transactionFee, int accountBalance) {
        this(2, transactionFee, accountBalance, new SimpleDateFormat("HH:mm:ss:SSS").format(new Date()), "--");
    }

    public Transaction(int actionType, int transactionFee, int accountBalance, String time, String symbol) {
        this.actionType = actionType;
        this.transactionFee = transactionFee;
        this.accountBalance = accountBalance;
        this.time = time;
        this.symbol = symbol;
    }

    public String getAction() {
        if (this.actionType == 0)
            return "Buy";
        if (this.actionType == 1)
            return "Sell";
        return "Self Charge";
    }

    public int getSum() { return this.transactionFee; }

    public int getAccount() { return this.accountBalance; }

    public String getSymbol() { return this.symbol; }

    public String getTime() { return this.time; }

    @Override
    public String toString() {
        String res = "Time: " + time;
        if (actionType == 2)
            res = res.concat("\tAction Type: Charge");
        else
            res = res.concat( "\tAction Type: " + (actionType == 0 ? "Buy" : "Sell") + "\tStock Symbol: " + symbol);
        return res.concat("\tTransaction value: " + transactionFee + "\tAccount Balance Before: " + accountBalance + "\tAccount Balance After: " + (accountBalance + transactionFee));
    }
}
