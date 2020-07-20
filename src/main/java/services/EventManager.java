package services;

import entities.FileEntity;
import entities.Steps;
import entities.Ticket;
import org.springframework.context.ApplicationEvent;
import org.springframework.stereotype.Component;

import java.util.List;

public class EventManager extends ApplicationEvent {

    private List<Ticket> collectedTickets;
    private List<FileEntity> fileEntities;
    private String step;

    /**
     * Create a new {@code ApplicationEvent}.
     *
     * @param source the object on which the event initially occurred or with
     *               which the event is associated (never {@code null})
     */
    public EventManager(Object source, String step) {
        super(source);
        this.step = step;
    }

    public String getStep() {
        return step;
    }

    public List<Ticket> getCollectedTickets() {
        return collectedTickets;
    }

    public void setCollectedTickets(List<Ticket> collectedTickets) {
        this.collectedTickets = collectedTickets;
    }

    public List<FileEntity> getFileEntities() {
        return fileEntities;
    }

    public void setFileEntities(List<FileEntity> fileEntities) {
        this.fileEntities = fileEntities;
    }
}
