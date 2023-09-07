package springmockproject.busticketapp.repository;

import org.hibernate.exception.GenericJDBCException;
import org.hibernate.exception.SQLGrammarException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.InvalidDataAccessResourceUsageException;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.jdbc.UncategorizedSQLException;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.ExceptionHandler;
import springmockproject.busticketapp.entity.Ticket;
import springmockproject.busticketapp.entity.User;

import java.sql.SQLException;
import java.util.List;

@Repository
public interface TicketRepository extends CrudRepository<Ticket, Long> {

    /*@Modifying
    @Query(value = "insert into Ticket(UserId, SeatId) VALUES (:userId,:seatId)", nativeQuery = true)
    @Transactional
    void insertTicket(@Param("userId") String userId, @Param("seatId") String seatId);*/

    List<Ticket> findByUserIdAndStatus(String userId, int status);

    List<Ticket> findByUser(User user);

    List<Ticket> findByUserAndStatus(User user, int status);

    @Modifying
    @Query(value = "update Ticket t set t.status = :status where t.id = :ticketId")
    @Transactional
    void setTicketStatus(@Param("status") int status, @Param("ticketId") long ticketId);

    Ticket findById(long ticketId);

    List<Ticket> findByStatus(int status);

    @Modifying
    @Query(value = "SELECT * FROM Ticket " +
            "INNER JOIN Seat " +
            "ON Ticket.SeatId = Seat.Id " +
            "WHERE Seat.BusId = :busId " +
            "AND Ticket.Status = :status", nativeQuery = true)
    List<Ticket> findByBusIdAndStatus(@Param("busId") String busId, @Param("status") int status);


    /*@Transactional
    @Query(value = "{CALL spCreateTicket(:userId, :seatId, :passengerName, :passengerIdentity, :passengerIsChild)}",
            nativeQuery = true)
    void createTicket(@Param("userId") String userId,
                      @Param("seatId") int seatId,
                      @Param("passengerName") String passengerName,
                      @Param("passengerIdentity") String passengerIdentity,
                      @Param("passengerIsChild") boolean isChild);*/
    @Modifying
    @Query(value = "INSERT INTO Ticket(SeatId, UserId, PassengerName, PassengerIdentity, PassengerIsChild) " +
            "VALUES (:seatId, :userId, :passengerName, :passengerIdentity, :isChild)", nativeQuery = true)
    void createTicket(@Param("seatId") String seatId,@Param("userId") String userId,
                      @Param("passengerName") String passengerName,
                      @Param("passengerIdentity") String passengerIdentity,
                      @Param("isChild") boolean isChild);



    @Query(value = "SELECT COUNT(T.Id) FROM Ticket T " +
            "INNER JOIN Seat S ON T.SeatId = S.Id " +
            "INNER JOIN Bus B ON S.BusId = B.Id " +
            "WHERE T.Status = 1 " +
            "AND B.Id = :busId " +
            "AND T.PassengerIdentity = :passengerIdentity ",
            nativeQuery = true)
    int checkDuplicatedPersonalIDCount (@Param("busId") String busId,
                                        @Param("passengerIdentity") String passengerIdentity);

    @Query(value = "SELECT COUNT(T.Id) FROM Ticket T " +
            "INNER JOIN Seat S ON T.SeatId = S.Id " +
            "INNER JOIN Bus B ON S.BusId = B.Id " +
            "WHERE T.Status = 1 " +
            "AND B.Id = :busId " +
            "And T.UserId = :userId " +
            "AND T.PassengerIdentity = :passengerIdentity " +
            "AND T.PassengerIsChild = 0",
            nativeQuery = true)
    int countCaretakerOnTheSameBusByTheSameBookerAndIsBooking
            (@Param("busId") String busId, @Param("userId") String userId,
             @Param("passengerIdentity") String passengerIdentity);

    @Query(value = "SELECT COUNT(T.Id) FROM Ticket T " +
            "INNER JOIN Seat S ON T.SeatId = S.Id " +
            "INNER JOIN Bus B ON S.BusId = B.Id " +
            "WHERE Status = 1 " +
            "AND B.Id = :busId " +
            "AND T.UserId = :userId " +
            "AND T.PassengerIdentity = :passengerIdentity " +
            "AND T.PassengerIsChild = 1",
            nativeQuery = true)
    int countChildrenAttachedToSpecificPIDOnTheSameBusByTheSameBookerAndIsBooking
            (@Param("busId") String busId, @Param("userId") String userId,
             @Param("passengerIdentity") String passengerIdentity);
}
