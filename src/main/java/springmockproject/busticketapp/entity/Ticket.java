package springmockproject.busticketapp.entity;


import lombok.*;

import javax.persistence.*;

@Builder
@Data
@AllArgsConstructor(access = AccessLevel.PUBLIC)
@NoArgsConstructor(access = AccessLevel.PUBLIC)
@Entity
@Table(name = "Ticket")
public class Ticket {

    @Id
    @Column(name = "Id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;


    @ManyToOne()
    @JoinColumn(name = "UserId")
    private User user;

    /*@Column(name = "SeatId")
    private String seatId;*/

    @ManyToOne()
    @JoinColumn(name = "SeatId")
    Seat seat;

    @Column(name = "Status")
    private int status;

    @Column(name = "PassengerName")
    private String passengerName;

    @Column(name = "PassengerIdentity")
    private String passengerIdentity;

    @Column(name = "PassengerIsChild")
    private boolean passengerIsChild;

}
