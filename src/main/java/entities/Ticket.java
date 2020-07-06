package entities;

import java.sql.Timestamp;
import java.util.Map;

public class Ticket {

    // id  заявки = номер заявки на сайте
    private String id;
    // дата проведения заявки
    private String aktdat;
    // начальная сумма торгов
    private double totalSum;
    // итоговая сумма торгов. Пока что ноль. Возможно потом уберу или доделаю.
    private double finishTotalSum;
    // позиции заявки в виде наименование : цена за штуку
    private Map<String, Double> positions;
    // организация - покупатель
    private String customer;
    // регион организации
    private String region;
    // ссылка на заявку на сайте госзакупок
    private String link;
    // наименование заявки
    private String title;

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public Ticket(String id) {
        this.id = id;
    }

    public Ticket(String id, String customer) {
        this.id = id;
        this.customer = customer;
    }

    public String getId() {
        return id;
    }

    public String getAktdat() {
        return aktdat;
    }

    public void setAktdat(String aktdat) {
        this.aktdat = aktdat;
    }

    public double getTotalSum() {
        return totalSum;
    }

    public void setTotalSum(double totalSum) {
        this.totalSum = totalSum;
    }

    public Map<String, Double> getPositions() {
        return positions;
    }

    public void setPositions(Map<String, Double> positions) {
        this.positions = positions;
    }

    public String getCustomer() {
        return customer;
    }

    public void setCustomer(String customer) {
        this.customer = customer;
    }

    public String getRegion() {
        return region;
    }

    public void setRegion(String region) {
        this.region = region;
    }

    public String getLink() {
        return link;
    }

    public void setLink(String link) {
        this.link = link;
    }

    public double getFinishTotalSum() {
        return finishTotalSum;
    }

    public void setFinishTotalSum(double finishTotalSum) {
        this.finishTotalSum = finishTotalSum;
    }

    @Override
    public String toString() {
        return "Ticket{" +
                "id='" + id + '\'' +
                ", aktdat='" + aktdat + '\'' +
                ", totalSum=" + String.valueOf(totalSum) +
                ", finishTotalSum=" + String.valueOf(finishTotalSum) +
                ", positions=" + positions +
                ", customer='" + customer + '\'' +
                ", region='" + region + '\'' +
                ", link='" + link + '\'' +
                ", title='" + title + '\'' +
                '}';
    }
}
