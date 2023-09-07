package springmockproject.busticketapp.entity;


import lombok.Data;
import springmockproject.busticketapp.container.TicketContainer;

import javax.persistence.*;
import java.util.List;

@Data
@Entity
@Table(name = "Seat")
public class Seat {

    @Id
    @Column(name = "Id")
    private String id;

    @ManyToOne()
    @JoinColumn(name = "BusId")
    private Bus bus;

    @Column(name = "IsBooked")
    private boolean booked;

    @Column(name = "SeatNo")
    private int seatNo;

    @OneToMany(targetEntity = Ticket.class, mappedBy = "seat")
    private List<Ticket> tickets;

    @Transient
    private TicketContainer ticketContainer;

    public TicketContainer getTicketContainer() {
        return new TicketContainer(this, tickets);
    }


}
