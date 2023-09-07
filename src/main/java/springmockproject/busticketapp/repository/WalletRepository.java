package springmockproject.busticketapp.repository;

import org.springframework.data.repository.CrudRepository;
import springmockproject.busticketapp.entity.User;
import springmockproject.busticketapp.entity.Wallet;

public interface WalletRepository extends CrudRepository<Wallet, Long> {

    Wallet getByUserId(String userId);

    Wallet getByUser(User user);

    Wallet findById(long id);



}
