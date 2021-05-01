package com.rse;

import java.io.Serializable;
import java.security.InvalidParameterException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;


public class tempE  implements Serializable {
    public enum Commands { LMT, MKT, FOK, IOC }
    private Map<String, Stock> stocks;                      // stock symbol -> Stock
    private Map<String, Map<String, ArrayList<Deal>>> db;   // stock symbol -> { buy -> [Deals], sell -> [Deals], approved -> [Deals] }

    public tempE() {
        db = new HashMap<>();
        stocks = new HashMap<>();
    }

    private void createDbStockList(String symbol) {
        Map<String, ArrayList<Deal>> item = new HashMap<>(3);
        item.put("Buy", new ArrayList<>());
        item.put("Sell", new ArrayList<>());
        item.put("Approved", new ArrayList<>());
        this.db.put(symbol, item);
    }

    // public ArrayList<Deal> getList(String name, String list) { return this.db.get(name).get(list); }

    public void addStock(String companyName, String symbol, int rate, int quantity) throws InvalidParameterException {
        /* add a stock into the stocks list and create the appropriate list for the deals to come */
        if (this.stocks.containsKey(symbol))
            throw new InvalidParameterException("The symbol '" + symbol + "' is assigned to a different company");
        for (Stock stock : this.stocks.values()) {
            if (stock.getCompanyName().equals(companyName))
                throw new InvalidParameterException("The stock of '" + companyName + "' exist in the list");
        }
        if (this.db.containsKey(symbol))
            throw new InvalidParameterException("The symbol '" + symbol + "' is assigned to a different company");
        this.stocks.put(symbol, new Stock(companyName, symbol, rate, quantity));
        createDbStockList(symbol);
    }

    public void addStockQuantity(String stockName, int quantity) {
        if (!this.stocks.containsKey(stockName))
            throw new InvalidParameterException("");
        this.stocks.get(stockName).addQuantity(quantity);
    }

    public void delAllQuantity() {
        for (Stock stock: this.stocks.values())
            stock.setQuantity(0);
    }

    public void addAllStocks(Map<String, Stock> stocks) {
        for (Stock stock : stocks.values()) {
            try {
                this.addStock(stock.getCompanyName(), stock.getSymbol(), stock.getRate(), stock.getQuantity());
            } catch (InvalidParameterException ignore) { }
        }
    }

    public boolean checkStock(String name) { return this.stocks.containsKey(name); }

    // public Map<String, Stock> getStocks() {return stocks;}

    public Stock getStock(String symbol) { return this.stocks.get(symbol); }

    public int getMaxQuantity(String symbol) { return this.stocks.get(symbol).getQuantity(); }

    @Override
    public String toString() {
        /* return the buy, sell and approved list of all the stocks */
        int revenue;
        String res = "";
        if (this.stocks.size() == 0)
            return "There is no Stocks to show\n";
        for (String stock : this.db.keySet()) {
            res = res.concat("======== " + stock + " ========\n");
            for (String list : this.db.get(stock).keySet()) {
                revenue = 0;
                res = res.concat("List " + list + ":\n");
                if (this.db.get(stock).get(list).size() == 0)
                    res = res.concat("\t" + "There is no deals to show\n");
                for (Deal item : this.db.get(stock).get(list)) {
                    res = res.concat("\t" + item.toString()) + "\n";
                    revenue += item.getAmount() * item.getRate();
                }
                res = res.concat("Total revenue in the list " + list + " is: " + revenue + "\n");
            }
            res = res.concat(stock + " current rate is " + this.stocks.get(stock).getRate() + "\n");
            res = res.concat("=====================\n");
        }
        return res;
    }

    public String print(String name) throws InvalidParameterException {
        /* return the buy, sell and approved list of the stock specified */
        for(Stock stock : this.stocks.values()) {
            if (stock.getCompanyName().equals(name)) {
                System.out.println("======== " + stock + " ========");
                for (String list : this.db.get(stock.getSymbol()).keySet()) {
                    System.out.println("List " + list);
                    for (Deal item : this.db.get(stock.getSymbol()).get(list))
                        return item.toString();
                }
            }
        }
        throw new InvalidParameterException("There is no company with that name in objects.RSE");
    }

    public String printStock() {
        /* return the information about all the stocks  */
        String res = "";
        if (this.stocks.size() == 0)
            return "There is no Stocks to show\n";
        for(Stock stock : this.stocks.values()) {
            res = res.concat(stock.toString() + "\n");
        }
        return res;
    }

    public String printStock(String name) throws InvalidParameterException {
        /* return the information about the stock with the name provided */
        String res = "";
        if (!this.stocks.containsKey(name))
            throw new InvalidParameterException("There is no company with that name in objects.RSE");
        res = res.concat(this.stocks.get(name).toString() + "\nDeals:\n");
        ArrayList<Deal> allDeals = new ArrayList<>();
        for (String key : this.db.get(name).keySet())
            allDeals.addAll(this.db.get(name).get(key));
        allDeals.sort((Deal a, Deal b) -> (a.getTime().compareTo(b.getTime()) < 0) ? 1 : -1);
        for (Deal item : allDeals)
            res = res.concat(item.print() + "\n");
        return res;
    }

    public Map<String, Integer> graph(String stockName) {
        /* creates a graph of time against price */
        Map<String, Integer> graph = new HashMap<>();
        if (!this.stocks.containsKey(stockName))
            throw new InvalidParameterException("There is no stock named '" + stockName + "' in the system");

        for (Deal deal: this.db.get(stockName).get("Approved"))
            graph.put(deal.getTime(), deal.getRate());
        return graph;
    }

    private boolean difference(Deal a, Deal b, boolean key) {
        /* return the difference between the two deals */
        if (key)
            return a.getRate() < b.getRate();
        return a.getRate() > b.getRate();
    }

    private int insertFunction(Deal a, Deal b, boolean key) {
        /* sorting function according to the key */
        if (difference(a, b, key))
            return 1;
        else {
            if (a.getRate() == b.getRate()) {
                if (a.getTime().compareTo(b.getTime()) < 0)
                    return -1;
                else
                    return 1;
            } else
                return -1;
        }
        // this.db.get(deal.getSymbol()).get(key).sort((Deal a, Deal b) -> (a.getRate() < b.getRate()) ? 1 : ((a.getRate() == b.getRate()) ? ((a.getTime().compareTo(b.getTime()) < 0) ? -1 : 1) : -1));  // rewrite !!
    }

    public void insert(Deal deal) {
        /* insert a new deal to the pending list of its kind */
        String key = deal.getAction() ? "Buy" : "Sell";
        this.stocks.get(deal.getSymbol()).setTotalDeals();
        this.db.get(deal.getSymbol()).get(key).add(deal);
        this.db.get(deal.getSymbol()).get(key).sort((Deal a, Deal b) -> insertFunction(a, b, deal.getAction()));
    }

    private void commandHelper(String symbol, boolean action, String time, int rate, int amount, User sender, User receiver, ArrayList<Deal> approved) {  // **user
        Deal new_deal = new Deal(symbol, action, amount, rate, sender, time);  // **user
        new_deal.setRevolution(amount * rate);
        sender.createTransactionEffect(action, true, amount * rate, amount, time, this.stocks.get(symbol));
        receiver.createTransactionEffect(action, false, amount * rate, amount, time, this.stocks.get(symbol));
        this.stocks.get(symbol).addRevolution(amount * rate);
        this.db.get(symbol).get("Approved").add(new_deal);
        approved.add(new_deal);
        this.stocks.get(symbol).setRate(new_deal.getRate());
    }

    public String TradeCommand(Commands cm, String symbol, boolean action, int amount, int rate, User user)  throws InvalidParameterException {  // **user
        // if (this.stocks.get(symbol).getQuantity() < amount)
        //     throw new InvalidParameterException("The amount of stocks is higher than the max amount of the stock\nThe deal has been canceled\n");
        if (cm.equals(Commands.MKT))
            rate = this.stocks.get(symbol).getRate();
        Deal deal = new Deal(symbol, action, amount, rate, user);  // ** user
        String oppositeKey = action ? "Sell" : "Buy";
        ArrayList<Deal> key_temp = new ArrayList<>(this.db.get(symbol).get(oppositeKey));
        ArrayList<Deal> approved_temp = new ArrayList<>(this.db.get(symbol).get("Approved"));
        ArrayList<Deal> new_approved = new ArrayList<>();
        Stock stock_temp = new Stock(this.stocks.get(symbol));
        boolean broke = false;

        while (deal.getAmount() > 0) {
            for (Deal item : this.db.get(symbol).get(oppositeKey)) {
                if (((action) && (item.getRate() <= deal.getRate())) || ((!action) && (deal.getRate() <= item.getRate()))) {
                    if (item.getAmount() <= deal.getAmount()) {
                        commandHelper(symbol, action, deal.getTime(), item.getRate(), item.getAmount(), user, item.getPublisher(), new_approved);  // **user
                        deal.setAmount(deal.getAmount() - item.getAmount());
                        this.db.get(symbol).get(oppositeKey).remove(item);
                    } else { // item.getAmount() > deal.getAmount()
                        commandHelper(symbol, action, deal.getTime(), item.getRate(), deal.getAmount(), user, item.getPublisher(), new_approved);  // **user
                        item.setAmount(item.getAmount() - deal.getAmount());
                        deal.setAmount(0);
                    }
                    broke = true;
                    break;
                }
            }
            if (broke)
                broke = false;
            else {
                if (cm.equals(Commands.FOK) && deal.getAmount() > 0) {
                    this.db.get(symbol).get(oppositeKey).clear();
                    this.db.get(symbol).get(oppositeKey).addAll(key_temp);
                    this.db.get(symbol).get("Approved").clear();
                    this.db.get(symbol).get("Approved").addAll(approved_temp);
                    this.stocks.get(symbol).set(stock_temp);
                    return "The deal has been canceled";
                }
                else {
                    if (!cm.equals(Commands.IOC) )
                        this.insert(deal);
                    if (new_approved.size() == 0)
                        return "The deal is pending";
                    else
                        break;
                }
            }
        }
        String res = "The deal has been approved\n";
        for (Deal item : new_approved)
            res = res.concat("\t" + item.print() + "\n");
        return res;
    }
}
