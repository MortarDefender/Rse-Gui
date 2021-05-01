package objects;

public class DealDTO {
    private final UserDTO publisher;  // String only ??
    private final String symbol, time, action, status, publisherName;  // true == Buy || false == Sell
    private final int amount, rate, revolution;

    public DealDTO(String symbol, String action, int amount, int rate, int revolution, UserDTO publisher, String time) {
        this(symbol, action, amount, rate, revolution, publisher, time, "Pending");
    }

    public DealDTO(String symbol, String action, int amount, int rate, int revolution, UserDTO publisher, String time, String status) {
        this.time = time;
        this.rate = rate;
        this.status = status;
        this.symbol = symbol;
        this.action = action;
        this.amount = amount;
        this.publisher = publisher;
        this.revolution = revolution;
        this.publisherName = publisher.getUsername();
    }

    public String getSymbol() { return symbol; }

    public String getTime() { return time; }

    public String getAction() { return action; }

    public int getAmount() { return amount; }

    public int getRevolution() { return revolution; }

    public int getRate() { return rate; }

    public UserDTO getPublisher() { return publisher; }

    public String getStatus() { return status; }

    public String getPublisherName() { return publisherName; }
}
