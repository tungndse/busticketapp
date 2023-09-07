package springmockproject.busticketapp.repository;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import springmockproject.busticketapp.entity.CheckoutSeat;

@Repository

public interface CheckoutSeatRepository extends CrudRepository<CheckoutSeat, Long> {



}
