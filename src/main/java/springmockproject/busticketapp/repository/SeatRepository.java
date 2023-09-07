package springmockproject.busticketapp.repository;

import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import springmockproject.busticketapp.entity.Bus;
import springmockproject.busticketapp.entity.Seat;

import java.util.List;


@Repository
public interface SeatRepository extends CrudRepository<Seat, String> {

    List<Seat> findByBus(Bus bus);

    @Modifying
    @Query(value = "update Seat s set s.booked = :status where s.id = :id")
    @Transactional
    void changeSeatStatus(@Param("status") boolean isBooked, @Param("id") String seatId);

    List<Seat> findSeatByBooked(boolean status);

    List<Seat> findSeatByBookedAndBus(boolean status, Bus bus);

    Seat getById(String id);


}
