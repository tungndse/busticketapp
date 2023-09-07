package springmockproject.busticketapp.entity;


import lombok.*;

import javax.persistence.*;
import java.util.List;

@Data
@Builder
@AllArgsConstructor(access = AccessLevel.PUBLIC)
@NoArgsConstructor(access = AccessLevel.PUBLIC)
@Entity
@Table(name = "Checkout")
public class Checkout {

    @Id
    @Column(name = "Id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @OneToOne
    @JoinColumn(name = "UserId")
    private User booker;

    @ManyToOne()
    @JoinColumn(name = "BusId")
    private Bus bus;

    @Column(name = "Status")
    private int status;

    @OneToMany(targetEntity = CheckoutSeat.class, mappedBy = "checkout")
    private List<CheckoutSeat> checkoutSeats;




}
