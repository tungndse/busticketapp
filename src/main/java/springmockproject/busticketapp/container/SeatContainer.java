package springmockproject.busticketapp.container;

import lombok.Data;
import springmockproject.busticketapp.entity.Seat;

import java.util.List;


@Data
public class SeatContainer {

    private List<Seat> seats;

    private int availableCount;

    public SeatContainer(List<Seat> seats) {
        this.seats = seats;
    }

    public int getAvailableCount() {
        if (seats != null) {
            int count = 0;
            for (Seat seat :
                    seats) {
                if (!seat.isBooked()) {
                    count++;
                }
            }

            return count;
        } else {
            return 0;
        }
    }
}
