package objects;

import java.util.List;
import java.util.Map;

public class UserDTO {
    private final int account, revolution;
    private final String username, type;  // stock broker || admin
    private final Map<String, Integer> stocks;
    private final List<TransactionDTO> transactions;

    public UserDTO(String name, String type, int account, Map<String, Integer> stocks, List<TransactionDTO> trans, int revolution) {
        this.type = type;
        this.username = name;
        this.stocks = stocks;
        this.account = account;
        this.transactions = trans;
        this.revolution = revolution;
    }

    public String getType() { return type; }

    public int getAccount() { return account; }

    public String getUsername() { return username; }

    public int getRevolution() { return revolution; }

    public List<TransactionDTO> getTransactions() { return transactions; }

    public int getStockQuantity(String stockName) { return stocks.get(stockName); }
}
