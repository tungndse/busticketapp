package springmockproject.busticketapp.repository;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import springmockproject.busticketapp.entity.Bus;
import springmockproject.busticketapp.entity.Checkout;
import springmockproject.busticketapp.entity.User;

import java.util.List;

@Repository
public interface CheckoutRepository extends CrudRepository<Checkout, Long> {

    Checkout getById(long id);

    List<Checkout> getByBookerAndBusAndStatus(User booker, Bus bus, int status);

}
