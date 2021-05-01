package com.rse;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.Serializable;
import java.security.InvalidParameterException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

// public class tempR  implements Serializable {}  // Rizpa Stock Exchange
/*    private Engine eng;
    private Map<String, User> users;

    public tempR() {
        eng = new Engine();
        users = new HashMap<>();
    }

    private Document getDoc(String path) throws Exception {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();
        InputStream xmlFileInputStream = new FileInputStream(path);
        return builder.parse(xmlFileInputStream);
    }

    public void loadXml(String path) throws Exception { loadXml2(getDoc(path).getDocumentElement()); }

    public void loadXml(String path, String username) throws Exception { loadXml3(getDoc(path).getDocumentElement(), username); }

    private void loadXml1(Element root) {
        this.eng.addAllStocks(loadStocks(root));
    }

    private Map<String, Stock> loadStocks(Element root) {
        Map<String, Stock> newStocks = new HashMap<>();
        Map<String, String> newStocksNames = new HashMap<>();
        NodeList stocks = root.getElementsByTagName("rse-stock");
        for (int i = 0; i < stocks.getLength(); i++) {
            Node stock = stocks.item(i);
            if (stock.getNodeType() == Node.ELEMENT_NODE) {
                Element item = (Element) stock;
                String symbol = item.getElementsByTagName("rse-symbol").item(0).getTextContent();
                String companyName = item.getElementsByTagName("rse-company-name").item(0).getTextContent();
                int price = Integer.parseInt(item.getElementsByTagName("rse-price").item(0).getTextContent());
                int amount = 0;  // quantity
                if (newStocks.containsKey(symbol))
                    throw new InvalidParameterException("The symbol '" + symbol + "' is assigned to a different company");
                if (newStocksNames.containsKey(companyName))
                    throw new InvalidParameterException("The stock of '" + companyName + "' exist in the list");

                newStocks.put(symbol, new Stock(companyName, symbol, price, amount));
                newStocksNames.put(companyName, symbol);
            }
        }
        return newStocks;
    }

    private Map<String, Integer> loadUsersHoldings(Element root, Map<String, Stock> stock) {
        Map<String, Integer> userHoldings = new HashMap<>();
        NodeList holdings = root.getElementsByTagName("rse-item");
        for (int j = 0; j < holdings.getLength(); j++) {
            Node attr = holdings.item(j);
            if (attr.getNodeType() == Node.ELEMENT_NODE) {
                Element holdItem = (Element) attr;
                String symbol = holdItem.getAttribute("symbol");
                int quantity = Integer.parseInt(holdItem.getAttribute("quantity"));
                if (userHoldings.containsKey(symbol))
                    throw new InvalidParameterException("The symbol '" + symbol + "' is assigned to a different company");
                if (!stock.containsKey(symbol))
                    throw new InvalidParameterException("The stock '" + symbol + "' is not in the stock db");
                if (quantity <= 0)
                    throw new InvalidParameterException("quantity of a stock is above zero");
                userHoldings.put(symbol, quantity);
            }
        }
        return userHoldings;
    }

    private void loadXml2(Element root) {
        Map<String, Stock> stocks = loadStocks(root);
        Map<String, Map<String, Integer>> allUserHolding = new HashMap<>();
        NodeList xmlUsers = root.getElementsByTagName("rse-user");
        for (int i = 0; i < xmlUsers.getLength(); i++) {
            Node user = xmlUsers.item(i);
            if (user.getNodeType() == Node.ELEMENT_NODE) {
                Element item = (Element) user;
                String username = item.getAttribute("name");
                if (allUserHolding.containsKey(username))
                    throw new InvalidParameterException("The user with the name '" + username + "' has been entered twice");
                allUserHolding.put(username, loadUsersHoldings(item, stocks));
            }
        }

        this.eng.addAllStocks(stocks);
        for (String username: allUserHolding.keySet()) {
            this.users.put(username, new User(username, true));
            this.addAllUserHoldings(allUserHolding.get(username), username, true);
        }
        this.updateStockQuantity();
    }

    private void loadXml3(Element root, String username) {
        Map<String, Stock> stocks = loadStocks(root);
        if (!this.users.containsKey(username))
            throw new InvalidParameterException("The user with the name '" + username + "' does not exist");
        Map<String, Integer> userHolding = loadUsersHoldings(root, stocks);
        this.eng.addAllStocks(stocks);
        this.addAllUserHoldings(userHolding, username, false);
        this.updateStockQuantity();
    }

    private void updateStockQuantity() {
        this.eng.delAllQuantity();
        for (User user: this.users.values()) {
            for (Stock stock: user.getStocks())
                this.eng.addStockQuantity(stock.getSymbol(), user.quantityOfStock(stock.getSymbol()));
        }
    }

    public void addUser(String username, boolean type) throws InvalidParameterException {
        if (this.users.containsKey(username))
            throw new InvalidParameterException("there is a user with the name '" + username + "' already");
        this.users.put(username, new User(username, type));
    }

    public void accountCharge(String username, int amount) throws InvalidParameterException {
        if (!this.users.containsKey(username))
            throw new InvalidParameterException("The user with the name '" + username + "' does not exist");
        this.users.get(username).addMoneyCharge(amount);
    }

    public boolean checkUser(String username) { return this.users.containsKey(username); }

    public void addStock(String companyName, String symbol, int rate, int quantity) throws InvalidParameterException {
        /* add a stock
        this.eng.addStock(companyName, symbol, rate, quantity);
    }

    public void userCreatedStock(String username, String companyName, String symbol, int rate, int quantity) throws InvalidParameterException {
        this.eng.addStock(companyName, symbol, rate, quantity);
        this.addStockToUser(symbol, username);
    }

    public void addAllUserHoldings(Map<String, Integer> holdings, String username, boolean flag) {
        Map<Stock, Integer> allStocksHolding = new HashMap<>();
        for (String symbol: holdings.keySet())
            allStocksHolding.put(this.eng.getStock(symbol), holdings.get(symbol));
        this.users.get(username).addAllStocks(allStocksHolding, flag);
    }

    public void addStockToUser(String symbol, String username, int quantity) {
        if (!this.eng.checkStock(symbol))
            throw new InvalidParameterException("The stock '" + symbol + "' is not in the stock db");
        if (!this.users.containsKey(username))
            throw new InvalidParameterException("The user named '" + username + "' is not a user in the system");
        if (quantity <= 0)
            throw new InvalidParameterException("The quantity of the stock the user can hold is greater than zero");
        this.users.get(username).addStock(this.eng.getStock(symbol), quantity);
    }

    public void addStockToUser(String symbol, String username) {
        if (!this.eng.checkStock(symbol))
            throw new InvalidParameterException("");
        addStockToUser(symbol, username, this.eng.getStock(symbol).getQuantity());
    }

    private void commandException(String symbol, boolean action, int amount, String username) {
        if (amount <= 0)
            throw new InvalidParameterException("the amount of stocks most be positive");
        if (!eng.checkStock(symbol))
            throw new InvalidParameterException("there is no stock with that symbol");
        if (!this.users.containsKey(username))
            throw new InvalidParameterException("there is no user with that username");
        if (!action) {
            if (this.users.get(username).checkStock(symbol)) {
                if (this.users.get(username).quantityOfStock(symbol) < amount)
                    throw new InvalidParameterException(username + " does not have enough stock of " + symbol);
            } else
                throw new InvalidParameterException(username + " does not have any stock of " + symbol);
        }
        /*else {
            if (this.users.get(username).quantityOfStock(symbol) + amount > this.eng.getMaxQuantity(symbol))
                throw new InvalidParameterException(username + " can not buy more stock than the quantity of the company");
        }
    }

    // public String LMT(String symbol, boolean action, int amount, int rate, String username) throws InvalidParameterException {  // **user
        /* LIMIT command int objects.RSE
        commandException(symbol, action, amount, username);
        return eng.TradeCommand(Engine.Commands.LMT, symbol, action, amount, rate, this.users.get(username)) + "\n";  // **user
        //String res = ||  return res.concat(eng.toString());
    }

    public String MKT(String symbol, boolean action, int amount, String username) throws InvalidParameterException {  // **user
        /* MARKET command in objects.RSE
        commandException(symbol, action, amount, username);
        return eng.TradeCommand(Engine.Commands.MKT, symbol, action, amount, 0, this.users.get(username)) + "\n";  // **user
        //String res = ||  return res.concat(eng.toString());
    }

    public String FOK(String symbol, boolean action, int amount, int rate, String username) throws InvalidParameterException {  // **user
        /* Fill Or Kill command in objects.RSE
        commandException(symbol, action, amount, username);
        return eng.TradeCommand(Engine.Commands.FOK, symbol, action, amount, rate, this.users.get(username)) + "\n";  // **user
        //String res = ||  return res.concat(eng.toString());
    }

    public String IOC(String symbol, boolean action, int amount, int rate, String username) throws InvalidParameterException {  // **user
        /* Immediate Or Cancel in objects.RSE
        commandException(symbol, action, amount, username);
        return eng.TradeCommand(Engine.Commands.IOC, symbol, action, amount, rate, this.users.get(username)) + "\n";  // **user
        //String res = ||  return res.concat(eng.toString());
    }

    public String print() {
        /* print all lists for all the stocks
        return eng.toString();
    }

    public String printUser(String username) {
        if (!this.users.containsKey(username))
            throw new InvalidParameterException("There is no user with the name '" + username + "'");
        return this.users.get(username).toString();
    }

    public void add(String symbol, boolean action, int amount, int rate, String username) {  // **user
        /* debug only add without a dedicated command of objects.RSE
        eng.insert(new Deal(symbol, action, amount, rate, this.users.get(username)));  // **user
    }

    public String investigateStock(String name) {
        /* print the information only on the stock with the given name
        return eng.printStock(name);
    }

    public String allStocks() {
        /* print the information of all stocks
        return eng.printStock();
    }

    public String allUsers() {
        String res = "";
        for(User user: this.users.values())
            res = res.concat(user.toString() + "\n");
        return res;
    }

    public ArrayList<Stock> getStocks() {
        Map<String, Stock> s = eng.getStocks();
        return new ArrayList<>(s.values());
    }

    public String getRate(String symbol) {
        Map<String, Stock> s = eng.getStocks();
        return String.valueOf(s.get(symbol).getRate());
    }

    public ArrayList<User> getUsers() { return new ArrayList<>(users.values()); }

    public Map<String, User> getUsersMap() { return this.users; }

    public User getUser(String username) { return this.users.get(username); }

    public Map<String, Integer> graph(String stockName) { return this.eng.graph(stockName); }

    public ArrayList<Deal> getList(String stockName, String list) { return this.eng.getList(stockName, list); }
}
*/