package springmockproject.busticketapp.entity;

import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;
import springmockproject.busticketapp.container.SeatContainer;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Data
@Entity
@Table(name = "Bus")
public class Bus {

    @Id
    @Column(name = "Id")
    private String id;

    @ManyToOne()
    @JoinColumn(name = "FromId")
    private Location fromLoc;

    @ManyToOne()
    @JoinColumn(name = "ToId")
    private Location toLoc;

    @Column(name = "DateRun")
    private Date dateRun;

    @Column(name = "SeatCount")
    private int seatCount;

    @Column(name = "Price")
    private float price;

    @OneToMany(targetEntity = Seat.class, mappedBy = "bus")
    private List<Seat> seats = new ArrayList<>();

    @Transient
    private SeatContainer seatContainer;

    public SeatContainer getSeatContainer() {
        return new SeatContainer(seats);
    }
}
