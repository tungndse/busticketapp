package springmockproject.busticketapp.entity;


import lombok.*;

import javax.persistence.*;

@Data
@Builder
@AllArgsConstructor(access = AccessLevel.PUBLIC)
@NoArgsConstructor(access = AccessLevel.PUBLIC)
@Entity
@Table(name = "CheckoutSeat")
public class CheckoutSeat {

    @Id
    @Column(name = "Id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long Id;

    @ManyToOne()
    @JoinColumn(name = "CheckoutId")
    private Checkout checkout;

    @OneToOne
    @JoinColumn(name = "SeatId")
    private Seat seat;

    @Column(name = "PassengerName")
    private String passengerName;

    @Column(name = "PassengerIdentity")
    private String passengerIdentity;

    @Column(name = "PassengerIsChild")
    private boolean passengerIsChild;


}
