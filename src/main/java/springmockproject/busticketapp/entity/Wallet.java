package springmockproject.busticketapp.entity;


import lombok.*;

import javax.persistence.*;

@Data
@Entity
@Builder
@AllArgsConstructor(access = AccessLevel.PUBLIC)
@NoArgsConstructor(access = AccessLevel.PUBLIC)
@Table(name = "Wallet")
public class Wallet {

    @Id
    @Column(name = "Id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @JoinColumn(name = "UserId")
    @OneToOne
    private User user;

    @Column(name = "Balance")
    private double balance;
}
