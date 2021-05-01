package com.rse;

import objects.TransactionDTO;

import java.io.Serializable;
import java.security.InvalidParameterException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class User implements Serializable {
    private static class Tuple implements Serializable {
        private Stock stock;
        private int stockQuantity;

        public Tuple(Stock stock, int amount) {
            this.stock = stock;
            this.stockQuantity = amount;
        }

        public Stock getStock() { return stock; }
        public String getSymbol() {return stock.getSymbol();}
        public int getAmount() { return stockQuantity; }

        public void setStock(Stock stock) { this.stock = stock; }
        public void setAmount(int amount) {
            if (amount > 0)
                this.stockQuantity = amount;
        }

        public void addAmount(int amount) { this.stockQuantity += amount; }
        public void subAmount(int amount) throws InvalidParameterException{
            if (this.stockQuantity - amount <= 0)
                throw new InvalidParameterException("there can be only a positive amount of stocks that a user can hold");
            this.stockQuantity -= amount;
        }
    }

    private final String username;
    private final boolean type;    // stock broker == true || admin == false
    private int account;
    private Map<String, Tuple> stocks; // tuples ??
    private final ArrayList<Transaction> transactions;

    public User(String name, boolean type) {
        this(name, type, 0);
    }

    public User(String name, boolean type, int account) {
        this.username = name;
        this.type = type;
        this.account = account;
        this.stocks = new HashMap<>();
        this.transactions = new ArrayList<>();
    }

    public String getUsername() { return username; }
    public boolean getType() { return type; }
    public String getTypeString() { return type ? "Stock Broker" : "Admin"; }
    public int getAccount() { return account; }

   public int getTotalRevolution() {
        int rev = 0;
        for (User.Tuple t : this.stocks.values())
            rev += t.getAmount() * t.getStock().getRate();
        return rev;
   }

   public ArrayList<Stock> getStocks() {
        ArrayList<Stock> s = new ArrayList<>();
        for(User.Tuple t : this.stocks.values())
            s.add(t.getStock());
        return s;
   }

   public Map<String, Integer> getStocksDTO() {
        Map<String, Integer> l = new HashMap<>();
        for (User.Tuple t : this.stocks.values())
            l.put(t.getSymbol(), t.getAmount());
        return l;
   }

   public int getStockQuantity(String symbol) { return this.stocks.get(symbol).getAmount(); }

   public ArrayList<Stock> getUserStock() {
        ArrayList<Stock> s = new ArrayList<>();
        for(User.Tuple t : this.stocks.values())
            s.add(new Stock(t.getStock().getCompanyName(), t.getStock().getSymbol(), t.getStock().getRate(), t.getAmount()));
        return s;
   }

   public ArrayList<Transaction> getTransactions() { return this.transactions; }

   public List<TransactionDTO> getTransactionsDTO() {
        List<TransactionDTO> l = new ArrayList<>();
        for(Transaction transaction: this.transactions)
            l.add(new TransactionDTO(transaction.getAction(), transaction.getSum(), transaction.getAccount(), transaction.getTime(), transaction.getSymbol()));
        return l;
   }

   public void increaseAccount(int amount) { this.account += amount; }

   public void decreaseAccount(int amount) { this.account -= amount; }

   public void addTransaction(Transaction transaction) { this.transactions.add(transaction); }

   public void addAllStocks(Map<Stock, Integer> stocks, boolean flag) {
       Map<String, Tuple> stocksBackup = new HashMap<>(this.stocks);
       if (flag)
           this.stocks.clear();
       for (Stock stock : stocks.keySet()) {
           try {
               this.addStock(stock, stocks.get(stock));
           } catch (InvalidParameterException e) {
               if (flag) {
                   this.stocks = stocksBackup;
                   throw e;
               }
           }
        }
   }

   public void addStock(Stock stock, int amount) {
        if (this.stocks.containsKey(stock.getSymbol()))
            throw new InvalidParameterException("The user '" + this.username + "' has the stock named '" + stock + "' already");  // .getSymbol()
        this.stocks.put(stock.getSymbol(), new Tuple(stock, amount));
   }

   public void increaseStockAmount(Stock stock, int amount) {
        if (this.stocks.containsKey(stock.getSymbol()))
            this.stocks.get(stock.getSymbol()).addAmount(amount);
        else
            this.addStock(stock, amount);
   }

   public void decreaseStockAmount(Stock stock, int amount) {
        if (this.stocks.containsKey(stock.getSymbol()))
            this.stocks.get(stock.getSymbol()).subAmount(amount);
        else
            throw new InvalidParameterException(username + " does not have any stock of " + stock.getSymbol());
   }

   public void createTransactionEffect(boolean action, boolean sender, int sum, int amount, String time, Stock stock) {
        if ((action && sender) || (!action && !sender)) {
            addTransaction(new Transaction(0, -sum, account, time, stock.getSymbol()));
            decreaseAccount(sum);
            increaseStockAmount(stock, amount);
        } else {
            addTransaction(new Transaction(1, sum, account, time, stock.getSymbol()));
            increaseAccount(sum);
            decreaseStockAmount(stock, amount);
        }
   }

   public void addMoneyCharge(int amount) {  // amount needs to be positive ??
        this.transactions.add(new Transaction(amount, account));
        this.account += amount;
   }

   public boolean checkStock(String stockName) { return this.stocks.containsKey(stockName); }

   public int quantityOfStock(String stockName) throws InvalidParameterException {
        if (this.stocks.containsKey(stockName))
            return this.stocks.get(stockName).getAmount();
        return 0;
   }

   @Override
   public String toString() {
        String res = "Username: " + username + "\nUser Type: " + (type ? "Dealer" : "Admin") + "\nAccount Balance: " + account + "\nStocks:\n";
        if (this.stocks.size() == 0)
            res = res.concat("\tThere is no stocks to show\n");
        for (String key: this.stocks.keySet())
            res = res.concat("\tStock Name: " + key + "\tAmount: " + this.stocks.get(key).getAmount() + "\n");
        res = res.concat("Transactions: \n");
        if (this.transactions.size() == 0)
            res = res.concat("\tThere is no transactions to show\n");
        for (Transaction transaction : this.transactions)
            res = res.concat("\t" + transaction);
        return res;
   }
}
