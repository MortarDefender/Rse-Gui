package com.rse;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.Serializable;
import java.security.InvalidParameterException;
import java.util.*;

import generated.*;
import objects.*;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

public class Engine  implements Serializable, RSE {
    public enum Commands { LMT, MKT, FOK, IOC }
    private final Map<String, User> users;                        // users name   -> User
    private final Map<String, Stock> stocks;                      // stock symbol -> Stock
    private final Map<String, Map<String, ArrayList<Deal>>> db;   // stock symbol -> { buy -> [Deals], sell -> [Deals], approved -> [Deals] }
    private final static String JAXB_XML_GAME_PACKAGE_NAME = "generated";

    public Engine() {
        db = new HashMap<>();
        users = new HashMap<>();
        stocks = new HashMap<>();
    }

    @Override
    public void loadXml(String path) throws InvalidParameterException {
        /* load an xml file using the path given */
        try {
            InputStream inputStream = new FileInputStream(path);
            Map<String, Map<String, Integer>> usersHoldings = new HashMap<>();
            rseDeserializeFrom(inputStream).ifPresent(
                    r -> {
                        Optional<RseStocks> rseStocks = Optional.ofNullable(r.getRseStocks());
                        Optional<RseUsers> rseUsers = Optional.ofNullable(r.getRseUsers());
                        rseUsers.ifPresent(l -> {
                            checkUsers(l.getRseUser());
                            rseStocks.ifPresent(value -> usersHoldings.putAll(getUsersHoldings(l.getRseUser(), value.getRseStock())));
                        });

                        rseStocks.ifPresent(l -> addAllStocks(loadStocks(l.getRseStock())));
                        usersHoldings.forEach((username, holding) -> {
                            this.users.put(username, new User(username, true));
                            this.addAllUserHoldings(holding, username, false);
                        });

                        this.updateStockQuantity();
                    }
            );
        } catch (JAXBException e) {
            System.out.println(e.getMessage());
            e.printStackTrace();
            throw new InvalidParameterException("the path given '" + path + "' is not in the scheme of the xsd file given.");
        } catch (FileNotFoundException e) {
            throw new InvalidParameterException("the file in the path given '" + path + "' does not exist");
        }
    }

    // @Override
    // public void loadXml(String path) throws Exception { loadXml2(getDoc(path).getDocumentElement()); } // loads an xml using the xml2 scheme

    @Override
    public void loadXml(String path, String username) throws Exception { loadXml3(getDoc(path).getDocumentElement(), username); } // loads an xml using the xml3 scheme

    @Override
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

    @Override
    public void addUser(String username, boolean type) throws InvalidParameterException {
        /* adds a new user with the name and the type given */
        if (this.users.containsKey(username))
            throw new InvalidParameterException("there is a user with the name '" + username + "' already");
        this.users.put(username, new User(username, type));
    }

    @Override
    public void addAccountCharge(String username, int amount) throws InvalidParameterException {
        /* adds a new transaction with the amount given to the user with the given name */
        if (!this.users.containsKey(username))
            throw new InvalidParameterException("The user with the name '" + username + "' does not exist");
        this.users.get(username).addMoneyCharge(amount);
    }

    @Override
    public boolean checkUser(String username) { return this.users.containsKey(username); }  // check if a user exist in the system

    @Override
    public List<StockDTO> getStocks() {
        /* return a list of stock dto objects of all stocks */
        List<StockDTO> l = new ArrayList<>();
        for (Stock stock : this.stocks.values())
            l.add(new StockDTO(stock.getCompanyName(), stock.getSymbol(), stock.getRate(), stock.getQuantity(), stock.getTotalDeals(), stock.getRevolution()));
        return l;
    }

    @Override
    public List<StockDTO> getStocks(String username) {
        /* return a list of stock dto objects of only the stocks of the user with the given username */
        List<StockDTO> l = new ArrayList<>();
        for (Stock stock : this.users.get(username).getStocks())
            l.add(new StockDTO(stock.getCompanyName(), stock.getSymbol(), stock.getRate(), this.users.get(username).getStockQuantity(stock.getSymbol()), stock.getTotalDeals(), stock.getRevolution()));
        return l;
    }

    @Override
    public List<TransactionDTO> getTransactions(String username) { return this.users.get(username).getTransactionsDTO(); }  // return a list of transaction dto object of the user with the given username

    @Override
    public int getRate(String symbol) {
        /* return the rate of a given stock */
        if (this.stocks.containsKey(symbol))
            return this.stocks.get(symbol).getRate();
        throw new InvalidParameterException("There is no stock with the symbol: " + symbol);
    }

    @Override
    public List<UserDTO> getUsers() {
        /* return a list of user dto object of all users */
        List<UserDTO> l = new ArrayList<>();
        for (User user: this.users.values())
            l.add(new UserDTO(user.getUsername(), user.getTypeString(), user.getAccount(), user.getStocksDTO(), user.getTransactionsDTO(), user.getTotalRevolution()));
        return l;
    }

    @Override
    public UserDTO getUser(String username) {
        /* return a user dto object of a user with the given name */
        if (!this.users.containsKey(username))
            throw new InvalidParameterException("There is no user with the username: " + username);
        User user = this.users.get(username);
        return new UserDTO(user.getUsername(), user.getTypeString(), user.getAccount(), user.getStocksDTO(), user.getTransactionsDTO(), user.getTotalRevolution());
    }

    @Override
    public Map<String, Integer> graph(String stockName) {
        /* creates a graph of time against price of a given stock */
        Map<String, Integer> graph = new HashMap<>();
        if (!this.stocks.containsKey(stockName))
            throw new InvalidParameterException("There is no stock named '" + stockName + "' in the system");

        for (Deal deal: this.db.get(stockName).get("Approved"))
            graph.put(deal.getTime(), deal.getRate());
        return graph;
    }

    @Override
    public List<DealDTO> getAdminList(String stockName, String listName) {
        /* return a list of deal dto objects of the internal commands of the stock and the list name  */
        String status = listName.equals("Approved") ? "Approved" : "Pending";
        ArrayList<DealDTO> res = new ArrayList<>();
        ArrayList<Deal> l = this.db.get(stockName).get(listName);
        for (Deal deal : l)
            res.add(new DealDTO(deal.getSymbol(), deal.getActionString(), deal.getAmount(), deal.getRate(), deal.getRevolution(), deal.getPublisherDTO(), deal.getTime(), status));
        return res;
    }

    @Override
    public void addUserStock(String companyName, String symbol, int rate, int quantity, String username) throws InvalidParameterException {
        /* create a new stock with the given data and append it to the user with the given username */
        this.addStock(companyName, symbol, rate, quantity);
        if (!this.stocks.containsKey(symbol))
            throw new InvalidParameterException("The stock '" + symbol + "' is not in the stock db");
        if (!this.users.containsKey(username))
            throw new InvalidParameterException("The user named '" + username + "' is not a user in the system");
        if (quantity <= 0)
            throw new InvalidParameterException("The quantity of the stock the user can hold is greater than zero");
        this.users.get(username).addStock(this.stocks.get(symbol), quantity);
    }

    @Override
    public List<DealDTO> LMT(String symbol, boolean action, int amount, int rate, String username) throws InvalidParameterException {
        /* LIMIT command int objects.RSE */
        commandException(symbol, action, amount, username);
        return TradeCommand(Engine.Commands.LMT, symbol, action, amount, rate, this.users.get(username));
    }

    @Override
    public List<DealDTO> MKT(String symbol, boolean action, int amount, String username) throws InvalidParameterException {
        /* MARKET command in objects.RSE */
        commandException(symbol, action, amount, username);
        return TradeCommand(Engine.Commands.MKT, symbol, action, amount, 0, this.users.get(username));
    }

    @Override
    public List<DealDTO> FOK(String symbol, boolean action, int amount, int rate, String username) throws InvalidParameterException {
        /* Fill Or Kill command in objects.RSE */
        commandException(symbol, action, amount, username);
        return TradeCommand(Engine.Commands.FOK, symbol, action, amount, rate, this.users.get(username));
    }

    @Override
    public List<DealDTO> IOC(String symbol, boolean action, int amount, int rate, String username) throws InvalidParameterException {
        /* Immediate Or Cancel in objects.RSE */
        commandException(symbol, action, amount, username);
        return TradeCommand(Engine.Commands.IOC, symbol, action, amount, rate, this.users.get(username));
    }

    private Document getDoc(String path) throws Exception {
        /* return a Document object of the file with the given path */
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();
        InputStream xmlFileInputStream = new FileInputStream(path);
        return builder.parse(xmlFileInputStream);
    }

    private Map<String, Stock> loadStocks(List<RseStock> l) {
        /* load the stock from the xml file */
        Map<String, Stock> newStocks = new HashMap<>();
        Map<String, String> newStocksNames = new HashMap<>();
        l.forEach(item -> {
            if (newStocks.containsKey(item.getRseSymbol()))
                throw new InvalidParameterException("The symbol '" +  item.getRseSymbol() + "' is assigned to a different company");
            if (newStocksNames.containsKey(item.getRseCompanyName()))
                throw new InvalidParameterException("The stock of '" + item.getRseCompanyName() + "' exist in the list at least twice");
            newStocks.put(item.getRseSymbol(), new Stock(item.getRseCompanyName(), item.getRseSymbol(), item.getRsePrice(), 0));
            newStocksNames.put(item.getRseCompanyName(), item.getRseSymbol());
        });
        return newStocks;
    }

    private Map<String, Stock> loadStocks(Element root) {
        /* loads the stocks from the root element given */
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
        /* loads users holding from the root element and the stocks map given */
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

    private Optional<RizpaStockExchangeDescriptor> rseDeserializeFrom(InputStream in) throws JAXBException {
        JAXBContext jc = JAXBContext.newInstance(JAXB_XML_GAME_PACKAGE_NAME);
        Unmarshaller u = jc.createUnmarshaller();
        return Optional.ofNullable(((RizpaStockExchangeDescriptor) u.unmarshal(in)));
    }

    private void checkUsers(List<RseUser> l) {
        Set<String> usersNames = new HashSet<>();
        l.forEach(u -> {
            if (usersNames.contains(u.getName()))
                throw new InvalidParameterException("The user with the name '" + u.getName() + "' has been entered twice");
            usersNames.add(u.getName());
        });
    }

    private Map<String, Map<String, Integer>> getUsersHoldings(List<RseUser> users, List<RseStock> stocks) {
        Map<String, Map<String, Integer>> holdings = new HashMap<>();
        Set<String> stocksNameSet = new HashSet<>();
        Set<String> stocksCompSet = new HashSet<>();
        stocks.forEach(s -> {
             if (!stocksNameSet.contains(s.getRseSymbol()) && !stocksCompSet.contains(s.getRseCompanyName())) {
                 stocksNameSet.add(s.getRseSymbol());
                 stocksCompSet.add(s.getRseCompanyName());
             } else if(stocksNameSet.contains(s.getRseSymbol()))
                 throw new InvalidParameterException("The symbol '" + s.getRseSymbol() + "' is assigned to a different company");
             else if (stocksCompSet.contains(s.getRseCompanyName()))
                 throw new InvalidParameterException("The stock of '" + s.getRseCompanyName() + "' exist in the list");
        });
        users.forEach(u -> holdings.put(u.getName(), getUserHolding(u.getRseHoldings().getRseItem(), stocksNameSet)));
        return holdings;
    }

    private Map<String, Integer> getUserHolding(List<RseItem> items, Set<String> stocks) {
        Map<String, Integer> holding = new HashMap<>();
        items.forEach( h -> {
            if (holding.containsKey(h.getSymbol()))
                throw new InvalidParameterException("The symbol '" + h.getSymbol() + "' is assigned to a different company");
            if (!stocks.contains(h.getSymbol()))
                throw new InvalidParameterException("The stock '" + h.getSymbol() + "' is not in the stock db");
            if (h.getQuantity() <= 0)
                throw new InvalidParameterException("quantity of a stock is above zero");
            holding.put(h.getSymbol(), h.getQuantity());
        });
        return holding;
    }

    @Deprecated
    private void loadXml2(Element root) {
        /* loads an xml using xml2 scheme */
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

        this.addAllStocks(stocks);
        for (String username: allUserHolding.keySet()) {
            this.users.put(username, new User(username, true));
            this.addAllUserHoldings(allUserHolding.get(username), username, true);
        }
        this.updateStockQuantity();
    }

    private void loadXml3(Element root, String username) {
        /* loads an xml using xml3 scheme */
        Map<String, Stock> stocks = loadStocks(root);
        if (!this.users.containsKey(username))
            throw new InvalidParameterException("The user with the name '" + username + "' does not exist");
        Map<String, Integer> userHolding = loadUsersHoldings(root, stocks);
        this.addAllStocks(stocks);
        this.addAllUserHoldings(userHolding, username, false);
        this.updateStockQuantity();
    }

    private void addAllUserHoldings(Map<String, Integer> holdings, String username, boolean flag) {
        /* append thee holdings to the user with the given username */
        Map<Stock, Integer> allStocksHolding = new HashMap<>();
        for (String symbol: holdings.keySet())
            allStocksHolding.put(this.stocks.get(symbol), holdings.get(symbol));
        this.users.get(username).addAllStocks(allStocksHolding, flag);
    }

    private void updateStockQuantity() {
        /* update all stock quantity according to the xml file */
        this.delAllQuantity();
        for (User user: this.users.values()) {
            for (Stock stock: user.getStocks())
                this.addStockQuantity(stock.getSymbol(), user.quantityOfStock(stock.getSymbol()));
        }
    }

    private void createDbStockList(String symbol) {
        /* creates the lists of deals for the stock with the given symbol */
        Map<String, ArrayList<Deal>> item = new HashMap<>(3);
        item.put("Buy", new ArrayList<>());
        item.put("Sell", new ArrayList<>());
        item.put("Approved", new ArrayList<>());
        this.db.put(symbol, item);
    }

    private void addStockQuantity(String stockName, int quantity) {
        /* increase or decrease the stock quantity with the given amount and symbol */
        if (!this.stocks.containsKey(stockName))
            throw new InvalidParameterException("");
        this.stocks.get(stockName).addQuantity(quantity);
    }

    private void delAllQuantity() {
        /* clear the quantities of all stocks */
        for (Stock stock: this.stocks.values())
            stock.setQuantity(0);
    }

    private void addAllStocks(Map<String, Stock> stocks) {
        /* add all the stocks to the db */
        for (Stock stock : stocks.values()) {
            try {
                this.addStock(stock.getCompanyName(), stock.getSymbol(), stock.getRate(), stock.getQuantity());
            } catch (InvalidParameterException ignore) { }
        }
    }

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

    public String print(String name) throws InvalidParameterException {  // debug
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

    public String printStock() {  // debug
        /* return the information about all the stocks  */
        String res = "";
        if (this.stocks.size() == 0)
            return "There is no Stocks to show\n";
        for(Stock stock : this.stocks.values()) {
            res = res.concat(stock.toString() + "\n");
        }
        return res;
    }

    @Deprecated
    public String printStock(String name) throws InvalidParameterException {  // debug
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

    private void insert(Deal deal) {
        /* insert a new deal to the pending list of its kind */
        String key = deal.getAction() ? "Buy" : "Sell";
        this.stocks.get(deal.getSymbol()).setTotalDeals();
        this.db.get(deal.getSymbol()).get(key).add(deal);
        this.db.get(deal.getSymbol()).get(key).sort((Deal a, Deal b) -> insertFunction(a, b, deal.getAction()));
    }

    private void commandException(String symbol, boolean action, int amount, String username) {
        /* check exception before executing the command */
        if (amount <= 0)
            throw new InvalidParameterException("the amount of stocks most be positive");
        if (!this.stocks.containsKey(symbol))
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
        }*/
    }

    private void commandHelper(String symbol, boolean action, String time, int rate, int amount, User sender, User receiver, ArrayList<Deal> approved, List<DealDTO> result) {
        /* create the new deal and creating the transaction effect */
        Deal new_deal = new Deal(symbol, action, amount, rate, sender, time);
        new_deal.setRevolution(amount * rate);
        sender.createTransactionEffect(action, true, amount * rate, amount, time, this.stocks.get(symbol));
        receiver.createTransactionEffect(action, false, amount * rate, amount, time, this.stocks.get(symbol));
        this.stocks.get(symbol).addRevolution(amount * rate);
        this.db.get(symbol).get("Approved").add(new_deal);
        approved.add(new_deal);
        result.add(new DealDTO(new_deal.getSymbol(), new_deal.getActionString(), new_deal.getAmount(), new_deal.getRate(), new_deal.getRevolution(), new_deal.getPublisherDTO(), new_deal.getTime(),"Approved"));
        this.stocks.get(symbol).setRate(new_deal.getRate());
    }

    private List<DealDTO> TradeCommand(Commands cm, String symbol, boolean action, int amount, int rate, User user)  throws InvalidParameterException {
        /* activate the command according to cm with the given data */
        // if (this.stocks.get(symbol).getQuantity() < amount)
        //     throw new InvalidParameterException("The amount of stocks is higher than the max amount of the stock\nThe deal has been canceled\n");
        if (cm.equals(Commands.MKT))
            rate = this.stocks.get(symbol).getRate();
        Deal deal = new Deal(symbol, action, amount, rate, user);
        DealDTO dealOrg = new DealDTO(symbol, deal.getActionString(), deal.getAmount(), deal.getRate(), deal.getRevolution(), deal.getPublisherDTO(), deal.getTime(), "Canceled");
        String oppositeKey = action ? "Sell" : "Buy";
        ArrayList<Deal> key_temp = new ArrayList<>(this.db.get(symbol).get(oppositeKey));
        ArrayList<Deal> approved_temp = new ArrayList<>(this.db.get(symbol).get("Approved"));
        ArrayList<Deal> new_approved = new ArrayList<>();
        Stock stock_temp = new Stock(this.stocks.get(symbol));
        List<DealDTO> result = new ArrayList<>();
        boolean broke = false;

        while (deal.getAmount() > 0) {
            for (Deal item : this.db.get(symbol).get(oppositeKey)) {
                if (((action) && (item.getRate() <= deal.getRate())) || ((!action) && (deal.getRate() <= item.getRate()))) {
                    if (item.getAmount() <= deal.getAmount()) {
                        commandHelper(symbol, action, deal.getTime(), item.getRate(), item.getAmount(), user, item.getPublisher(), new_approved, result);  // **user
                        deal.setAmount(deal.getAmount() - item.getAmount());
                        this.db.get(symbol).get(oppositeKey).remove(item);
                    } else { // item.getAmount() > deal.getAmount()
                        commandHelper(symbol, action, deal.getTime(), item.getRate(), deal.getAmount(), user, item.getPublisher(), new_approved, result);  // **user
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
                    result.clear();
                    result.add(dealOrg);
                    return result;  // "The deal has been canceled"
                }
                else {
                    if (!cm.equals(Commands.IOC)) {
                        result.add(new DealDTO(deal.getSymbol(), deal.getActionString(), deal.getAmount(), deal.getRate(), deal.getRevolution(), deal.getPublisherDTO(), deal.getTime(), "Pending"));
                        this.insert(deal);
                    }
                    else
                        result.add(new DealDTO(deal.getSymbol(), deal.getActionString(), deal.getAmount(), deal.getRate(), deal.getRevolution(), deal.getPublisherDTO(), deal.getTime(), "Canceled"));
                    break;  // if (new_approved.size() == 0) // "The deal is pending"
                }
            }
        }
        return result; //  "The deal has been approved
    }
}
