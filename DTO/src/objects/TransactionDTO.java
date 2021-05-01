package objects;

public class TransactionDTO {
    private final String symbol, time, actionType;  // Buy || Sell || Self Charge
    private final int sum, accountBefore, accountAfter;

    public TransactionDTO(String actionType, int sum, int accountBefore, String time, String symbol) {
        this.sum = sum;
        this.time = time;
        this.symbol = symbol;
        this.actionType = actionType;
        this.accountBefore = accountBefore;
        this.accountAfter = accountBefore + sum;

    }

    public int getSum() { return sum; }

    public int getAccountBefore() { return accountBefore; }

    public int getAccountAfter() { return accountAfter; }

    public String getActionType() { return actionType; }

    public String getSymbol() { return symbol; }

    public String getTime() { return time; }
}
