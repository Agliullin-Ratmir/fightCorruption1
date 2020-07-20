package entities;

public class FileEntity {

    private Ticket ticket;
    private double maxPrice;
    private double minPrice;
    private double avgPrice;
    private String minPriceLink;
    private String maxPriceLink;

    public FileEntity(Ticket ticket, double maxPrice, double minPrice, double avgPrice) {
        this.ticket = ticket;
        this.maxPrice = maxPrice;
        this.minPrice = minPrice;
        this.avgPrice = avgPrice;
    }

    public FileEntity(Ticket ticket, double maxPrice, String maxPriceLink,
                      double minPrice, String minPriceLink,
                      double avgPrice) {
        this(ticket, maxPrice, minPrice, avgPrice);
        this.minPriceLink = minPriceLink;
        this.maxPriceLink = maxPriceLink;
    }

    public Ticket getTicket() {
        return ticket;
    }

    public void setTicket(Ticket ticket) {
        this.ticket = ticket;
    }

    public double getMaxPrice() {
        return maxPrice;
    }

    public void setMaxPrice(double maxPrice) {
        this.maxPrice = maxPrice;
    }

    public double getMinPrice() {
        return minPrice;
    }

    public void setMinPrice(double minPrice) {
        this.minPrice = minPrice;
    }

    public double getAvgPrice() {
        return avgPrice;
    }

    public void setAvgPrice(double avgPrice) {
        this.avgPrice = avgPrice;
    }

    public String getMinPriceLink() {
        return minPriceLink;
    }

    public void setMinPriceLink(String minPriceLink) {
        this.minPriceLink = minPriceLink;
    }

    public String getMaxPriceLink() {
        return maxPriceLink;
    }

    public void setMaxPriceLink(String maxPriceLink) {
        this.maxPriceLink = maxPriceLink;
    }
}
