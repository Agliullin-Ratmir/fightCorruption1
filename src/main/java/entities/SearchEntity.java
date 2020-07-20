package entities;

import java.util.List;

public class SearchEntity {

    private List<Ticket> tickets;
    private boolean isTicketsFilled;

    public SearchEntity(List<Ticket> tickets) {
        this.tickets = tickets;
        isTicketsFilled = true;
    }

    public List<Ticket> getTickets() {
        return tickets;
    }

    public boolean isTicketsFilled() {
        return isTicketsFilled;
    }
}
