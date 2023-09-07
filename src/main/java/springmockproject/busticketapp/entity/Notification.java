package springmockproject.busticketapp.entity;

import lombok.*;

import javax.persistence.*;
import java.util.Date;

@Data
@Builder
@AllArgsConstructor(access = AccessLevel.PUBLIC)
@NoArgsConstructor(access = AccessLevel.PUBLIC)
@Entity
@Table(name = "Notification")
public class Notification {

    @Id
    @Column(name = "Id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @Column(name = "TimeCreated")
    private Date timeCreated;

    @Column(name = "Message")
    private String message;

    @Column(name = "Type")
    private int type;

    @OneToOne(cascade = CascadeType.ALL)
    @JoinTable(name = "Notification_Checkout",
            joinColumns =
                    { @JoinColumn(name = "NotifiId", referencedColumnName = "Id") },
            inverseJoinColumns =
                    { @JoinColumn(name = "CheckoutId", referencedColumnName = "Id") })
    private Checkout checkout;
}
