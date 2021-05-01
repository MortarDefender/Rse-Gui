package objects;

import java.security.InvalidParameterException;
import java.util.List;
import java.util.Map;

public interface RSE {
    Map<String, Integer> graph(String stockName);
    void loadXml(String path) throws Exception;
    void loadXml(String path, String username) throws Exception;

    List<UserDTO> getUsers();
    List<StockDTO> getStocks();
    UserDTO getUser(String username);
    boolean checkUser(String username);
    void addUser(String username, boolean type) throws InvalidParameterException;
    void addAccountCharge(String username, int amount) throws InvalidParameterException;
    void addUserStock(String companyName, String symbol, int rate, int quantity, String username) throws InvalidParameterException;

    int getRate(String symbol);
    List<StockDTO> getStocks(String username);
    List<TransactionDTO> getTransactions(String username);
    List<DealDTO> getAdminList(String stockName, String listName);

    void addStock(String companyName, String symbol, int rate, int quantity) throws InvalidParameterException;
    List<DealDTO> MKT(String symbol, boolean action, int amount, String username) throws InvalidParameterException;
    List<DealDTO> LMT(String symbol, boolean action, int amount, int rate, String username) throws InvalidParameterException;
    List<DealDTO> FOK(String symbol, boolean action, int amount, int rate, String username) throws InvalidParameterException;
    List<DealDTO> IOC(String symbol, boolean action, int amount, int rate, String username) throws InvalidParameterException;
}